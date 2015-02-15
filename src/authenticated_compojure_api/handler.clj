(ns authenticated-compojure-api.handler
  (:require [compojure.api.sweet :refer :all]
            [authenticated-compojure-api.routes.user :refer :all]
            [authenticated-compojure-api.routes.quotes :refer :all]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn init []
  (query/create-registered-user-table-if-not-exists!)
  (query/create-quotes-table-if-not-exists!)
  (query/create-permission-table-if-not-exists!)
  (query/create-user-permission-table-if-not-exists!)
  (query/create-password-reset-key-table-if-not-exists!))

(defapi app
  (swagger-ui)
  (swagger-docs
   :title "Authenticated-compojure-api"
   :apiVersion "0.0.1")

  (swaggered "User"
             :description "Create and reset user details"
             user-routes)

  (swaggered "Quote"
             :description "Create Read Update and Delete quotes"
             quote-routes))
