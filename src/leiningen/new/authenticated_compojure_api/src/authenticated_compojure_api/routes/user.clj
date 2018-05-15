(ns {{ns-name}}.routes.user
  (:require
    [clojure.spec.alpha :as s]
    [compojure.api.sweet :refer [context POST DELETE PATCH]]
    [{{ns-name}}.specs :as specs]
    [{{ns-name}}.general-functions.validations :as validations]
    [{{ns-name}}.middleware.cors :refer [cors-mw]]
    [{{ns-name}}.middleware.token-auth :refer [token-auth-mw]]
    [{{ns-name}}.middleware.authenticated :refer [authenticated-mw]]
    [{{ns-name}}.route-functions.user.create-user :refer [create-user-response]]
    [{{ns-name}}.route-functions.user.delete-user :refer [delete-user-response]]
    [{{ns-name}}.route-functions.user.modify-user :refer [modify-user-response]]))

(s/def ::auth-header string?)

(def user-routes
  (context "/api/v1/user" []
           :tags ["User"]
           :coercion validations/spec

    (POST "/" {:as request}
           :return ::specs/register-response
           :middleware [cors-mw]
           :body-params [email :- ::specs/email
                         username :- ::specs/username
                         password :- ::specs/password]
           :summary "Create a new user with provided username, email and password."
           (create-user-response email username password))

    (DELETE "/:id" {:as request}
             :path-params [id :- ::specs/id]
             :return {:message ::specs/message}
             :header-params [authorization :- ::auth-header]
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Deletes the specified user. Requires token to have `admin` auth or self ID."
             :description "Authorization header expects the following format 'Token {token}'"
             (delete-user-response request id))

    (PATCH  "/:id" {:as request}
             :path-params [id :- ::specs/id]
             :body-params [{username :- ::specs/username ""}
                           {password :- ::specs/password ""}
                           {email :- ::specs/email ""}]
             :header-params [authorization :- ::auth-header]
             :return ::specs/patch-pass-response
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Update some or all fields of a specified user. Requires token to have `admin` auth or self ID."
             :description "Authorization header expects the following format 'Token {token}'"
             (modify-user-response request id username password email))))
