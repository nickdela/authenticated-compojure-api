(ns authenticated-compojure-api.route-functions.user.gen-new-token
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [authenticated-compojure-api.general-functions.user.create-token :refer [create-token]]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn gen-new-token-response [refresh_token]
  (let [user (first (query/get-registered-user-by-reset-token {:refresh_token refresh_token}))]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          {:token (create-token user)}))))
