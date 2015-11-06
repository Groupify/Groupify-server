(ns groupify.ws-actions
  (:require [clostache.parser :refer :all]
            [clojure.data.json :as json]))

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