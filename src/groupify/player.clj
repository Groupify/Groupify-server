(ns groupify.player
  (:require [clostache.parser :refer :all]
            [groupify.util :as util]))

(def connected-to-host (atom true))
(def start-time (atom 0))
(def player-state (atom :stopped))

(def client-channels (atom []))

; Map channels to users
(def usernames (atom {}))

; Map channels to users' queues
(def queues (atom {}))

