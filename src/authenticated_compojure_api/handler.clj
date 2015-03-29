(ns authenticated-compojure-api.handler
  (:require [compojure.api.sweet :refer :all]
            [authenticated-compojure-api.routes.user :refer :all]
            [authenticated-compojure-api.routes.preflight :refer :all]
            [authenticated-compojure-api.routes.permission :refer :all]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn init []
  (query/create-registered-user-table-if-not-exists!)
  (query/create-permission-table-if-not-exists!)
  (query/create-user-permission-table-if-not-exists!)
  (query/create-password-reset-key-table-if-not-exists!))

(defapi app
  (swagger-ui)
  (swagger-docs
   :title "Authenticated-compojure-api"
   :apiVersion "0.0.1")

  (swaggered "Preflight"
             :description "Return successful response for all preflight requests"
             preflight-route)

  (swaggered "User"
             :description "Create and reset user details"
             user-routes)

  (swaggered "Permission"
             :description "Add and remove permissions tied to specific users"
             permission-routes))
