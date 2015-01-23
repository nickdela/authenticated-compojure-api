(ns authenticated-compojure-api.route-functions.user
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [buddy.hashers.bcrypt :as hasher]
            [ring.util.http-response :refer [bad-request ok created conflict]]))

(defn auth-credentials-response [request]
  (ok (let [user    (:identity request)
            token   (bs/dumps user auth-key)
            refresh (:refresh_token user)]
        {:username (:username user) :token token :refresh_token refresh})))

(defn gen-new-token-response [refresh_token]
  (let [user (query/get-user-by-reset-token {:refresh_token refresh_token})]
    (if (empty? user)
      (bad-request {:error "Bad Request"})
      (ok {:token (bs/dumps user auth-key)}))))

(defn create-new-user [username password]
  (let [refresh-token   (str (java.util.UUID/randomUUID))
        hashed-password (hasher/make-password password)
        new-user        (query/insert-user<! {:username username
                                              :password hashed-password
                                              :access "Basic"
                                              :refresh_token refresh-token})]
    (created {:username (:username new-user)})))

(defn create-user-response [username password]
  (if (empty? (query/get-user-by-username {:username username}))
    (create-new-user username password)
    (conflict {:error "Username already exists"})))
