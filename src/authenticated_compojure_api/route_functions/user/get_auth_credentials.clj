(ns authenticated-compojure-api.route-functions.user.get-auth-credentials
  (:require [environ.core :refer [env]]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn auth-credentials-response [request]
  (let [user    (:identity request)
        token   (bs/dumps (dissoc user :refresh_token) (env :auth-key))
        refresh (:refresh_token user)]
    (respond/ok {:username (:username user) :token token :refresh_token refresh})))
