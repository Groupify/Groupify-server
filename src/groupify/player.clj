(ns groupify.player)

(def connected-to-host (atom true))
(def start-time (atom 0))
(def player-state (atom :stopped))