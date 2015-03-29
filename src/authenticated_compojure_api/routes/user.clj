(ns authenticated-compojure-api.routes.user
  (:require [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.user.create-user :refer [create-user-response]]
            [authenticated-compojure-api.route-functions.user.delete-user :refer [delete-user-response]]
            [authenticated-compojure-api.route-functions.user.modify-user :refer [modify-user-response]]
            [authenticated-compojure-api.route-functions.user.get-auth-credentials :refer [auth-credentials-response]]
            [authenticated-compojure-api.route-functions.user.gen-new-token :refer [gen-new-token-response]]
            [authenticated-compojure-api.route-functions.user.delete-refresh-token :refer [remove-refresh-token-response]]
            [authenticated-compojure-api.route-functions.user.request-password-reset :refer [request-password-reset-response]]
            [authenticated-compojure-api.route-functions.user.password-reset :refer [password-reset-response]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]))


(defroutes* user-routes
  (context "/api" []

    (POST* "/user"      {:as request}
           :return      {:username String}
           :middlewares [cors-mw]
           :body-params [email :- String username :- String password :- String]
           :summary     "Create a new user with provided username and password."
           (create-user-response email username password))

    (wrap-authentication
      (DELETE* "/user"      {:as request}
               :body-params [id :- Long]
               :return      {:message String}
               :middlewares [cors-mw token-auth-mw]
               :summary     "Deletes the specified user. Requires token to have `admin` auth or self ID."
               :notes       "Authorization header expects the following format 'Token {token}'"
               (delete-user-response request id))
      token-backend)

    (wrap-authentication
      (PATCH*  "/user"       {:as request}
               :body-params   [id :- Long {username :- String ""} {password :- String ""} {email :- String ""}]
               :header-params [authorization :- String]
               :return        {:id Long :email String :username String}
               :middlewares   [cors-mw token-auth-mw]
               :summary       "Update some or all fields of a specified user. Requires token to have `admin` auth or self ID."
               :notes         "Authorization header expects the following format 'Token {token}'"
               (modify-user-response request id username password email))
      token-backend)

    (wrap-authentication
     (GET* "/user/token"  {:as request}
           :return        {:id Integer :username String :permissions String :token String :refreshToken String}
           :header-params [authorization :- String]
           :middlewares   [cors-mw basic-auth-mw]
           :summary       "Returns auth info given a username and password in the 'Authorization' header."
           :notes         "Authorization header expects 'Basic username:password' where username:password
                           is base64 encoded. To adhere to basic auth standards we have to use a field called
                           `username` however we will accept a valid username or email as a value for this key."
           (auth-credentials-response request))
     basic-backend)

    (POST* "/user/refresh-token" []
           :return               {:token String :refreshToken String}
           :body-params          [refreshToken :- String]
           :middlewares          [cors-mw]
           :summary              "Get a fresh token with a valid re-fresh token."
           (gen-new-token-response refreshToken))

    (DELETE* "/user/refresh-token" []
             :body-params          [token :- String]
             :return               {:message String}
             :middlewares          [cors-mw]
             :summary              "Delete the specific refresh token"
             (remove-refresh-token-response token))

    (POST* "/user/password/request-reset" []
           :return      {:message String}
           :body-params [userEmail         :- String
                         fromEmail         :- String
                         subject           :- String
                         {emailBodyHtml    :- String ""}
                         {emailBodyPlain   :- String ""}
                         responseBaseLink  :- String]
           :middlewares [cors-mw]
           :summary     "Request a password reset for the registered user with the matching email"
           :notes       "The `respose-base-link` will get a reset key appended to it and then the
                         link itself will be appended to the email body. The reset key will be valid
                         for 24 hours after creation."
           (request-password-reset-response userEmail fromEmail subject emailBodyPlain emailBodyHtml responseBaseLink))

    (POST* "/user/password/reset" []
     :return      {:message String}
     :body-params [resetKey    :- String
                   newPassword :- String]
     :middlewares [cors-mw]
     :summary     "Replace an existing user password with the newPassowrd given a valid resetKey"
     (password-reset-response resetKey newPassword))))
