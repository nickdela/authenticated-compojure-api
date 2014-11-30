(ns authenticated-compojure-api.route-functions.token
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.queries.users :refer :all]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :refer [bad-request ok]]))

(defn auth-credentials-response [request]
  (ok (let [user    (:identity request)
            id      (:userid user)
            token   (bs/dumps user auth-key)
            refresh (:refresh-token user)]
        {:userid id :token token :refresh-token refresh})))

(defn gen-new-token-response [refresh-token]
  (let [user (get-user-by-keyword :refresh-token refresh-token)]
    (if (empty? user)
      (bad-request {:error "Bad Request"})
      (ok {:token (bs/dumps user auth-key)}))))
