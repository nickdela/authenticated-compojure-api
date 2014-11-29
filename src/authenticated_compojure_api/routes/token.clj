(ns authenticated-compojure-api.routes.token
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.sign.generic :as bs]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]))

;; ============================================
;; Schema(s)
;; ============================================
(s/defschema Auth-Credentials {:userid Long :token String :refresh-token String})

;; ============================================
;; Routes
;; ============================================
(defroutes* token-routes
  (context "/api" []

  (wrap-authentication (GET* "/token" {:as request}
                         :return Auth-Credentials
                         :summary "Returns authentication information given a
                                   valid username and password in the 'Authorization' header"
                         :notes   "The authorization header expects the string
                                   'Basic username:password' where the username:password portion is base64 encoded."
                         :header-params [authorization :- String]
                         :middlewares   [basic-auth-mw]
                         (ok (let [user    (:identity request)
                                   id      (:userid user)
                                   token   (bs/dumps user auth-key)
                                   refresh (:refresh-token user)]
                               {:userid id :token token :refresh-token refresh})))
                       basic-backend)))
