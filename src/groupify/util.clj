(ns groupify.util)

(defn atom-append [list-atom item] (swap! list-atom #(conj % item)))