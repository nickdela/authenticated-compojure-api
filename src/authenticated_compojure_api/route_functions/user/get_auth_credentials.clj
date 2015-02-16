(ns authenticated-compojure-api.route-functions.user.get-auth-credentials
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn auth-credentials-response [request]
  (let [user    (:identity request)
        token   (bs/dumps user auth-key)
        refresh (:refresh_token user)]
    (respond/ok {:username (:username user) :token token :refresh_token refresh})))
