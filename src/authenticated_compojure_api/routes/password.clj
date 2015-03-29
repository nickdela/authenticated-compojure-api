(ns authenticated-compojure-api.routes.password
  (:require [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.password.request-password-reset :refer [request-password-reset-response]]
            [authenticated-compojure-api.route-functions.password.password-reset :refer [password-reset-response]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]))

(defroutes* password-routes
  (context "/api" []

    (POST* "/password/reset-request" []
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

    (POST* "/password/reset-confirm" []
     :return      {:message String}
     :body-params [resetKey    :- String
                   newPassword :- String]
     :middlewares [cors-mw]
     :summary     "Replace an existing user password with the newPassowrd given a valid resetKey"
     (password-reset-response resetKey newPassword))))
