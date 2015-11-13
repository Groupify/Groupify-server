(ns groupify.ws-actions
  (:require [clostache.parser :refer :all]
            [clojure.data.json :as json]
            [immutant.web.async :as async]
            [groupify.player :as player]))
(def no-response nil)
(def host_channel (atom nil))

(def dummy-data (slurp "resources/test_immediate_song_data.json"))

(defn generate-response [action data] {:action action :data data :identity "server"})

(defn handle-ping [data] (generate-response "pong" data))
(defn handle-pong [data] (generate-response "ping" data))

(def state-map {"paused" :paused
                "playing" :playing
                "stopped" :stopped})
(defn handle-set-state [data] (if-let [state (get state-map data nil)]
                                (do (reset! player/player-state state)
                                    no-response)
                                (generate-response "error" (render "invalid state: \"{{state}}\"" {:state data}))))
(defn handle-get-state [data] (generate-response "tell-state" @player/player-state))

(defn handle-hello [data] (generate-response "hello" "hello"))


(defn handle-send-debug-data [data]
  (async/send! @host_channel dummy-data)
  nil)



(defn handle-default [action data]
  "Error handling for invalid actions"
  (generate-response "error"
                     (render "\"{{action}}\" is not a valid action for identity." {:action action})))

; Implement Client-Server interactions next week
(defn handle-client [action data]
  (case action
    "send-debug-data" (handle-send-debug-data data)
    (handle-default action data)))

(defn get-ws-response-for [ch m]
  "Given a channel and a message, return a map containing the response data"
  (let [message (json/read-str m)
        data (get message "data")
        action (get message "action")
        ident (get message "identity")]
    (if (= ident "host")
      (case action
        "ping" (handle-ping data)
        "pong" (handle-pong data)

        "set-state" (handle-set-state data)
        "get-state" (handle-get-state data)

        "hello" (handle-hello data)

        (handle-default action data))
      (handle-client action data))))