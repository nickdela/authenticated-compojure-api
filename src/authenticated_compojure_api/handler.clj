(ns authenticated-compojure-api.handler
  (:require [compojure.api.sweet :refer :all]
            [authenticated-compojure-api.routes.token :refer :all]
            [authenticated-compojure-api.routes.quotes :refer :all]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn init []
  (query/create-users-table-if-not-exists!)
  (query/create-quotes-table-if-not-exists!))

(defapi app
  (swagger-ui)
  (swagger-docs
    :title "Authenticated-compojure-api"
    :apiVersion "0.0.1" )

  (swaggered "Token"
    :description "Get and refresh tokens"
    token-routes)

  (swaggered "Quotes"
    :description "Create Read Update and Delete quotes"
    quote-routes))
