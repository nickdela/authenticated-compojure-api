(ns authenticated-compojure-api.route-functions.user.get-auth-credentials
  (:require [authenticated-compojure-api.general-functions.user.create-token :refer [create-token]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [ring.util.http-response :as respond]))

(defn auth-credentials-response [request]
  (let [user          (:identity request)
        refresh-token (str (java.util.UUID/randomUUID))
        _ (query/update-registered-user-refresh-token<! {:refresh_token refresh-token :id (:id user)})]
    (respond/ok {:username (:username user) :token (create-token user) :refresh-token refresh-token})))
