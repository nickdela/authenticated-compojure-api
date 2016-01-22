(ns authenticated-compojure-api.routes.permission
  (:require [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.authenticated :refer [authenticated-mw]]
            [authenticated-compojure-api.route-functions.permission.add-user-permission :refer [add-user-permission-response]]
            [authenticated-compojure-api.route-functions.permission.delete-user-permission :refer [delete-user-permission-response]]
            [compojure.api.sweet :refer :all]))


(def permission-routes
  (context "/api/permission/user" []

    (POST "/:id"         {:as request}
           :tags          ["Permission"]
           :path-params   [id :- Long]
           :body-params   [permission :- String]
           :header-params [authorization :- String]
           :return        {:message String}
           :middlewares   [token-auth-mw cors-mw authenticated-mw]
           :summary       "Adds the specified permission for the specified user. Requires token to have `admin` auth."
           :description   "Authorization header expects the following format 'Token {token}'"
           (add-user-permission-response request id permission))

    (DELETE "/:id"          {:as request}
             :tags           ["Permission"]
             :path-params    [id :- Long]
             :body-params    [permission :- String]
             :header-params  [authorization :- String]
             :return         {:message String}
             :middlewares    [token-auth-mw cors-mw authenticated-mw]
             :summary        "Deletes the specified permission for the specified user. Requires token to have `admin` auth."
             :description    "Authorization header expects the following format 'Token {token}'"
             (delete-user-permission-response request id permission))))
