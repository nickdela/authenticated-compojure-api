(ns {{ns-name}}.handler
  (:require
    [compojure.api.sweet :refer [api]]
    [ring.util.http-response :refer :all]
    [schema.core :as s]
    [{{ns-name}}.routes.user :refer :all]
    [{{ns-name}}.routes.preflight :refer :all]
    [{{ns-name}}.routes.permission :refer :all]
    [{{ns-name}}.routes.refresh-token :refer :all]
    [{{ns-name}}.routes.auth :refer :all]
    [{{ns-name}}.routes.password :refer :all]
    [{{ns-name}}.middleware.basic-auth :refer [basic-auth-mw]]
    [{{ns-name}}.middleware.token-auth :refer [token-auth-mw]]
    [{{ns-name}}.middleware.cors :refer [cors-mw]]))

(def app
  (api
    {:swagger
     {:ui   "/api-docs"
      :spec "/swagger.json"
      :data {:info {:title "authenticated-refresh-for-real"
                    :version "0.0.1"}
             :tags [{:name "Preflight"     :description "Return successful response for all preflight requests"}
                    {:name "User"          :description "Create, delete and update user details"}
                    {:name "Permission"    :description "Add and remove permissions tied to specific users"}
                    {:name "Refresh-Token" :description "Get and delete refresh-tokens"}
                    {:name "Auth"          :description "Get auth information for a user"}
                    {:name "Password"      :description "Request and confirm password resets"}]}}}
    preflight-route
    user-routes
    permission-routes
    refresh-token-routes
    auth-routes
    password-routes))
