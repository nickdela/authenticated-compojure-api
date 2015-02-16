(ns authenticated-compojure-api.route-functions.user.gen-new-token
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn gen-new-token-response [refresh_token]
  (let [user (query/get-registered-user-by-reset-token {:refresh_token refresh_token})]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          {:token (bs/dumps user auth-key)}))))
