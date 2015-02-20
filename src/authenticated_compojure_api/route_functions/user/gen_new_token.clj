(ns authenticated-compojure-api.route-functions.user.gen-new-token
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [authenticated-compojure-api.general-functions.user.create-token :refer [create-token]]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn create-new-tokens [user]
  (let [new-refresh-token (str (java.util.UUID/randomUUID))
        _ (query/update-registered-user-refresh-token<! {:refresh_token new-refresh-token :id (:id user)})]
    {:token (create-token user) :refresh-token new-refresh-token}))

(defn gen-new-token-response [refresh-token]
  (let [user (first (query/get-registered-user-by-reset-token {:refresh_token refresh-token}))]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          (create-new-tokens user)))))
