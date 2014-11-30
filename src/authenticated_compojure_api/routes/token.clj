(ns authenticated-compojure-api.routes.token
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [ring.util.http-response :refer [bad-request]]
            [authenticated-compojure-api.queries.users :refer :all]
            [buddy.sign.generic :as bs]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]))

;; ============================================
;; Schema(s)
;; ============================================
(s/defschema Credentials {:userid Long :token String :refresh-token String})
(s/defschema Token {:token String})

;; ============================================
;; Routes
;; ============================================
(defroutes* token-routes
  (context "/api" []

    (wrap-authentication
      (GET* "/token" {:as request}
        :return Credentials
        :summary "Returns auth info given a username and password in the 'Authorization' header"
        :notes   "Authorization header expects 'Basic username:password' where username:password is base64 encoded."
        :header-params [authorization :- String]
        :middlewares   [basic-auth-mw]
        (ok (let [user    (:identity request)
                  id      (:userid user)
                  token   (bs/dumps user auth-key)
                  refresh (:refresh-token user)]
              {:userid id :token token :refresh-token refresh})))
      basic-backend)

    (GET* "/token/refresh/:refresh-token" []
          :return Token
          :summary "Get a fresh token with a valid re-fresh token"
          :path-params [refresh-token :- String]
          (let [user (get-user-by-keyword :refresh-token refresh-token)]
            (if (empty? user)
              (bad-request {:error "Bad Request"})
              (ok {:token (bs/dumps user auth-key)}))))))
