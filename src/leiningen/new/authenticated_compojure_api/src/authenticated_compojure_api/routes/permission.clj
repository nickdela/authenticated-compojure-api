(ns {{ns-name}}.routes.permission
  (:require
    [clojure.spec.alpha :as s]
    [compojure.api.sweet :refer [context POST DELETE]]
    [{{ns-name}}.specs :as specs]
    [{{ns-name}}.middleware.cors :refer [cors-mw]]
    [{{ns-name}}.middleware.token-auth :refer [token-auth-mw]]
    [{{ns-name}}.middleware.authenticated :refer [authenticated-mw]]
    [{{ns-name}}.route-functions.permission.add-user-permission :refer [add-user-permission-response]]
    [{{ns-name}}.route-functions.permission.delete-user-permission :refer [delete-user-permission-response]]))

(s/def ::auth-header string?)

(def permission-routes
  (context "/api/v1/permission/user/:id" []
    :tags ["Permission"]
    :coercion :spec
    :path-params [id :- ::specs/id]
    :body-params [permission :- ::specs/permissions]
    :header-params [authorization :- ::auth-header]
    :return {:message ::specs/message}
    :middleware [token-auth-mw cors-mw authenticated-mw]
    :description "Authorization header expects the following format 'Token {token}'"

    (POST "/" request
      :summary "Adds the specified permission for the specified user. Requires token to have `admin` auth."
      (add-user-permission-response request id permission))

    (DELETE "/" request
      :summary "Deletes the specified permission for the specified user. Requires token to have `admin` auth."
      (delete-user-permission-response request id permission))))
