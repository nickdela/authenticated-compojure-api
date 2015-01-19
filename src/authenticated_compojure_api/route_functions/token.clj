(ns authenticated-compojure-api.route-functions.token
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :refer [bad-request ok]]))

(defn auth-credentials-response [request]
  (ok (let [user    (:identity request)
            id      (:id user)
            token   (bs/dumps user auth-key)
            refresh (:refresh_token user)]
        {:id id :token token :refresh_token refresh})))

(defn gen-new-token-response [refresh_token]
  (let [user (query/get-user-by-reset-token {:refresh_token refresh_token})]
    (if (empty? user)
      (bad-request {:error "Bad Request"})
      (ok {:token (bs/dumps user auth-key)}))))
