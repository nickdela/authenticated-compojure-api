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
    preflight-route
    user-routes
    permission-routes
    refresh-token-routes
    auth-routes
    password-routes))
