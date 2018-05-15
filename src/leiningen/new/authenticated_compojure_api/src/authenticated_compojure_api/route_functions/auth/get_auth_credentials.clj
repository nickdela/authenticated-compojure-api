(ns {{ns-name}}.route-functions.auth.get-auth-credentials
  (:require
    [ring.util.http-response :as respond]
    [{{ns-name}}.general-functions.user.create-token :refer [create-token]]
    [{{ns-name}}.query-defs :as query]))

(defn auth-credentials-response
  "Route requires basic authentication and will generate a new
   refresh-token."
  [request]
  (let [user (:identity request)
        refresh-token (.toString (java.util.UUID/randomUUID))]
    (query/update-registered-user-refresh-token! {:refresh_token refresh-token :id (:id user)})
    (respond/ok {:id (.toString (:id user))
                 :username (:username user)
                 :permissions (:permissions user)
                 :token (create-token user)
                 :refresh-token refresh-token})))
