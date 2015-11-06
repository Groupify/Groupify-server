(ns groupify.handler
  (:require [compojure.core :refer :all]
            [clostache.parser :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn template-response [path, data] (render-resource path data))

(defroutes app-routes
           (GET "/" [] (resource-response "index.html"
                                          {:root "public"}))
           (GET "/widgets" [] (response
                                [{:name "Widget 1"}
                                 {:name "Widget 2"}]))
           (route/resources "/")
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))