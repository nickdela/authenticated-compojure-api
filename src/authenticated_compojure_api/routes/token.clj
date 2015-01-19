(ns authenticated-compojure-api.routes.token
  (:require [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.token :refer [auth-credentials-response
                                                                       gen-new-token-response]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

;; ============================================
;; Schema(s)
;; ============================================
(s/defschema Credentials {:id Long :token String :refresh_token String})
(s/defschema Token {:token String})

;; ============================================
;; Routes
;; ============================================
(defroutes* token-routes
  (context "/api" []

    (wrap-authentication
      (GET* "/token" {:as request}
        :return        Credentials
        :header-params [authorization :- String]
        :middlewares   [cors-mw basic-auth-mw]
        :summary       "Returns auth info given a username and password in the 'Authorization' header"
        :notes         "Authorization header expects 'Basic username:password' where username:password is base64 encoded."
        (auth-credentials-response request))
      basic-backend)

    (GET* "/token/refresh/:refresh-token" []
      :return      Token
      :summary     "Get a fresh token with a valid re-fresh token"
      :middlewares   [cors-mw]
      :path-params [refresh-token :- String]
      (gen-new-token-response refresh-token))))
