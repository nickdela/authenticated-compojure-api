(ns authenticated-compojure-api.routes.token
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Message {:message String})

(defroutes* token-routes
  (context "/api" []

  (GET* "/token/placeholder" []
        :return Message
        :summary "A placeholder"
        (ok {:message "Place holder..."}))))
