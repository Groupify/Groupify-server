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

; Define callbacks for key web socket events (open, close, message received)
(def websocket-callbacks
  {:on-open (fn [channel]
              (async/send! channel (json/write-str (ws-actions/generate-response "status" "connected"))))
   :on-close (fn [channel {:keys [code reason]}]
               (log "close code:" code "reason:" reason))
   ; A message has been received to parse.
   :on-message (fn [ch m]
                 (async/send! ch (json/write-str (ws-actions/get-ws-response-for ch m))))})

(defn error-response [error]
  "Returns an HTML error response with the given message"
  (render-resource "public/error.html" {:message error}))

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