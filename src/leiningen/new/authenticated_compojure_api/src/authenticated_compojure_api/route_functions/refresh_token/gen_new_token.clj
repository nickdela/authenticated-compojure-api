(ns {{ns-name}}.route-functions.refresh-token.gen-new-token
  (:require [{{ns-name}}.general-functions.user.create-token :refer [create-token]]
            [{{ns-name}}.queries.query-defs :as query]
            [ring.util.http-response :as respond]))

(defn create-new-tokens
  "Create a new user token"
  [user]
  (let [new-refresh-token (str (java.util.UUID/randomUUID))
        _ (query/update-registered-user-refresh-token! query/db {:refresh_token new-refresh-token :id (:id user)})]
    {:token (create-token user) :refreshToken new-refresh-token}))

(defn gen-new-token-response
  "Generate response for user token creation"
  [refresh-token]
  (let [user (query/get-registered-user-details-by-refresh-token query/db {:refresh_token refresh-token})]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          (create-new-tokens user)))))
