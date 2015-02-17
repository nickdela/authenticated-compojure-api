(ns authenticated-compojure-api.route-functions.user.password-reset
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.hashers.bcrypt :as hasher]
            [ring.util.http-response :as respond]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn update-password [reset-key key-row new-password]
  (let [user-id         (:user_id key-row)
        hashed-password (hasher/make-password new-password)]
    (query/invalidate-reset-key<! {:reset_key reset-key})
    (query/update-registered-user-password<! {:id user-id :password hashed-password})
    (respond/ok {:message "Password successfully reset"})))

(defn password-reset-response [reset-key new-password]
  (let [key-row-query    (query/get-reset-row-by-reset-key {:reset_key reset-key})
        key-row          (first key-row-query)
        key-exists?      (empty? key-row)
        key-valid-until  (c/from-sql-time (:valid_until key-row))
        key-valid?       (t/before? (t/now) key-valid-until)]
    (cond
      key-exists? (respond/not-found {:error "Reset key does not exist"})
      key-valid?  (update-password reset-key key-row new-password)
      :else       (respond/not-found {:error "Reset key has expired"}))))
