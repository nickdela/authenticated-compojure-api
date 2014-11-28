(ns authenticated-compojure-api.handler
  (:require [compojure.api.sweet :refer :all]
            [authenticated-compojure-api.routes.token :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn init []
  (println "Application starting up...."))

(defapi app
  (swagger-ui)
  (swagger-docs
    :title "Authenticated-compojure-api"
    :apiVersion "0.0.1" )

  (swaggered "Tokens"
    :description "Get and refresh tokens"
    token-routes))
