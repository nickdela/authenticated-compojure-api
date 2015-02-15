(ns authenticated-compojure-api.routes.user
  (:require [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.user :as respond-with]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]))


(defroutes* user-routes
  (context "/api" []

    (POST* "/user"      {:as request}
           :return      {:username String}
           :body-params [email :- String username :- String password :- String]
           :summary     "Create a new user with provided username and password."
           (respond-with/create-user-response email username password))

    (wrap-authentication
      (DELETE* "/user/:id"  {:as request}
               :path-params [id :- Long]
               :return      {:message String}
               :middlewares [token-auth-mw]
               :summary     "Deletes the specified user. Requires token to have `admin` auth."
               :notes       "Authorization header expects the following format 'Token {token}'"
               (respond-with/delete-user-response request id))
      token-backend)

    (wrap-authentication
      (PUT* "/user/:id"      {:as request}
               :path-params  [id :- Long]
               :body-params  [{username :- String ""} {password :- String ""} {email :- String ""}]
               :return       {:id Long :email String :username String}
               :middlewares  [token-auth-mw]
               :summary      "Update some or all fields of a specified user. Requires token to have `admin` auth."
               :notes        "Authorization header expects the following format 'Token {token}'"
               (respond-with/modify-user-response request id username password email))
      token-backend)

    (wrap-authentication
      (POST* "/user/:id/permission/:permission" {:as request}
               :path-params                     [id :- Long permission :- String]
               :return                          {:message String}
               :middlewares                     [token-auth-mw]
               :summary                         "Adds the specified permission for the specified user. Requires token to have `admin` auth."
               :notes                           "Authorization header expects the following format 'Token {token}'"
               (respond-with/add-user-permission-response request id permission))
      token-backend)

    (wrap-authentication
      (DELETE* "/user/:id/permission/:permission" {:as request}
               :path-params                       [id :- Long permission :- String]
               :return                            {:message String}
               :middlewares                       [token-auth-mw]
               :summary                           "Deletes the specified permission for the specified user. Requires token to have `admin` auth."
               :notes                             "Authorization header expects the following format 'Token {token}'"
               (respond-with/delete-user-permission-response request id permission))
      token-backend)

    (wrap-authentication
     (GET* "/user/token"  {:as request}
           :return        {:username String :token String :refresh_token String}
           :header-params [authorization :- String]
           :middlewares   [cors-mw basic-auth-mw]
           :summary       "Returns auth info given a username and password in the 'Authorization' header."
           :notes         "Authorization header expects 'Basic username:password' where username:password is base64 encoded."
           (respond-with/auth-credentials-response request))
     basic-backend)

    (POST* "/user/token/refresh" []
           :return               {:token String}
           :body-params          [refresh-token :- String]
           :middlewares          [cors-mw]
           :summary              "Get a fresh token with a valid re-fresh token."
           (respond-with/gen-new-token-response refresh-token))

    (POST* "/user/password/request-reset" []
           :return      {:message String}
           :body-params [user-email :- String
                         from-email :- String
                         subject :- String
                         {email-body-html :- String  ""}
                         {email-body-plain :- String ""}
                         response-base-link :- String]
           :middlewares [cors-mw]
           :summary     "Request a password reset for the registered user with the matching email"
           :notes       "An email with a link to the password reset endpoint will be sent to the registered email"
           (respond-with/request-password-reset-response user-email from-email subject email-body-plain email-body-html response-base-link))))
