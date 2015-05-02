(ns authenticated-compojure-api.handler
  (:require [compojure.api.sweet :refer :all]
            [authenticated-compojure-api.routes.user :refer :all]
            [authenticated-compojure-api.routes.preflight :refer :all]
            [authenticated-compojure-api.routes.permission :refer :all]
            [authenticated-compojure-api.routes.refresh-token :refer :all]
            [authenticated-compojure-api.routes.auth :refer :all]
            [authenticated-compojure-api.routes.password :refer :all]
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
    {:info {:title "authenticated-compojure-api"
            :version "0.0.1"}
     :tags [{:name "Preflight"     :description "Return successful response for all preflight requests"}
            {:name "User"          :description "Create, delete and update user details"}
            {:name "Permission"    :description "Add and remove permissions tied to specific users"}
            {:name "Refresh-Token" :description "Get and delete refresh-tokens"}
            {:name "Auth"          :description "Get auth information for a user"}
            {:name "Password"      :description "Request and confirm password resets"}]})
  preflight-route
  user-routes
  permission-routes
  refresh-token-routes
  auth-routes
  password-routes)
