(ns authenticated-compojure-api.routes.refresh-token
  (:require [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.token-auth :refer [token-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.refresh-token.gen-new-token :refer [gen-new-token-response]]
            [authenticated-compojure-api.route-functions.refresh-token.delete-refresh-token :refer [remove-refresh-token-response]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]))


(defroutes* refresh-token-routes
  (context "/api" []

    (GET* "/refresh-token/:refreshToken" []
           :return          {:token String :refreshToken String}
           :path-params     [refreshToken :- String]
           :middlewares     [cors-mw]
           :summary         "Get a fresh token and new refresh-token with a valid refresh-token."
           (gen-new-token-response refreshToken))

    (DELETE* "/refresh-token/:refreshToken" []
             :return          {:message String}
             :path-params     [refreshToken :- String]
             :middlewares     [cors-mw]
             :summary         "Delete the specific refresh-token"
             (remove-refresh-token-response refreshToken))))
