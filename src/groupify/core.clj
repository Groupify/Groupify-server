(ns groupify.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [immutant.web :as web]
            [immutant.web.async :as async]
            [immutant.web.middleware :as web-middleware]
            [ring.util.response :refer [resource-response response redirect]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clostache.parser :refer :all]
            [clojure.data.json :as json]
            [groupify.ws-actions :as ws-actions]
            [groupify.log :refer :all])
  (:gen-class))




(defn check-if-host [channel m]
  (let [message (json/read-str m)
        ident (get message "identity")]
    (when (= ident "host")
      (reset! ws-actions/host_channel channel)
      (log "Set host channel"))))

; Define callbacks for key web socket events (open, close, message received)
(def websocket-callbacks
  {:on-open (fn [channel]
              (log "Connection opened")
              (async/send! channel (json/write-str (ws-actions/generate-response "status" "connected"))))
   :on-close (fn [channel {:keys [code reason]}]
               (log "Connection closed"))
   ; A message has been received to parse.
   :on-message (fn [channel message]
                 (log "Received:\n " message)
                 (check-if-host channel message)
                 (if-let [response (ws-actions/get-ws-response-for channel message)]
                   (do (async/send! channel (json/write-str response))
                       (log "Sent:\n " (json/write-str response) "\n\n"))
                   nil))})

(defn error-response [error]
  "Returns an HTML error response with the given message"
  (render-resource "public/error.html" {:message error}))

; Routes exposed by the server
(defroutes app-routes
           (GET "/" {c :context} (redirect (str c "/index.html")))
           (route/resources "/")
           (route/not-found (error-response "Page not found.")))

; Entry point.  Run the server with immutant/web.
(defn -main [& {:as args}]
  (web/run
    (-> app-routes
        (web-middleware/wrap-session {:timeout 20})
        (web-middleware/wrap-websocket websocket-callbacks))
    (merge {"host" "0.0.0.0"
            "port" 4545}
           args)))