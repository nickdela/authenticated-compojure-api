(ns authenticated-compojure-api.routes.user
  (:require [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.user :refer [auth-credentials-response
                                                                      gen-new-token-response
                                                                      create-user-response]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

;; ============================================
;; Routes
;; ============================================
(defroutes* user-routes
  (context "/api" []

    (POST* "/user"  {:as request}
           :return       {:username String}
           :body-params  [email :- String username :- String password :- String]
           :summary      "Create a new user with provided username and password."
           (create-user-response email username password))

    (wrap-authentication
     (GET* "/user/token" {:as request}
           :return           {:username String :token String :refresh_token String}
           :header-params    [authorization :- String]
           :middlewares      [cors-mw basic-auth-mw]
           :summary          "Returns auth info given a username and password in the 'Authorization' header"
           :notes            "Authorization header expects 'Basic username:password' where username:password is base64 encoded."
           (auth-credentials-response request))
     basic-backend)

    (POST* "/user/token/refresh" []
           :return       {:token String}
           :summary      "Get a fresh token with a valid re-fresh token"
           :middlewares  [cors-mw]
           :body-params  [refresh-token :- String]
           (gen-new-token-response refresh-token))))
