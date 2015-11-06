(ns groupify.core
  (:require [compojure.core :refer :all]
            [clostache.parser :refer :all]
            [compojure.route :as route]
            [immutant.web :as web]
            [immutant.web.async :as async]
            [immutant.web.middleware :as web-middleware]
            [clojure.data.json :as json]
            [ring.util.response :refer [resource-response response redirect]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:gen-class))

(defn generate-response [action data] {:action action :data data})

(defn handle-ping [data] (generate-response "pong" data))
(defn handle-pong [data] (generate-response "ping" data))
(defn handle-default [action data] (generate-response "error" (render "\"{{action}}\" is not a valid action." {:action action})))

(defn get-ws-response-for [ch m]
  (let [message (json/read-str m)
        data (get message "data")
        action (get message "action")]
    (case action
      "ping" (handle-ping data)
      "pong" (handle-pong data)
      (handle-default action data))
    ))

(def websocket-callbacks
  {:on-open (fn [channel]
              (async/send! channel (json/write-str (generate-response "status" "connected"))))
   :on-close (fn [channel {:keys [code reason]}]
               (println "close code:" code "reason:" reason))
   :on-message (fn [ch m]
                 (async/send! ch (json/write-str (get-ws-response-for ch m))))})


(defroutes app-routes
           #_(GET "/" [] (resource-response "index.html"
                                          {:root "public"}))
           (GET "/" {c :context} (redirect (str c "/index.html")))
           (GET "/widgets" [] (response
                                [{:name "Widget 1"}
                                 {:name "Widget 2"}]))
           (route/resources "/")
           (route/not-found "Not Found"))

(defn -main [& {:as args}]
  (web/run
    (-> app-routes
        (web-middleware/wrap-session {:timeout 20})
        (web-middleware/wrap-websocket websocket-callbacks))
    (merge {"host" "0.0.0.0"
            "port" 4545}
           args)))

#_(def app
  (-> app-routes
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))