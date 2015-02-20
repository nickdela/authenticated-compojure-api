(ns authenticated-compojure-api.route-functions.user.get-auth-credentials
  (:require [authenticated-compojure-api.general-functions.user.create-token :refer [create-token]]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn auth-credentials-response [request]
  (let [user    (:identity request)
        refresh (:refresh_token user)]
    (respond/ok {:username (:username user) :token (create-token user) :refresh_token refresh})))
