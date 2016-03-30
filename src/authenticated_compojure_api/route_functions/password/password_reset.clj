(ns authenticated-compojure-api.route-functions.password.password-reset
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.hashers :as hashers]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [ring.util.http-response :as respond]))

(defn update-password
  "Update user's password"
  [reset-key key-row new-password]
  (let [user-id         (:user_id key-row)
        hashed-password (hashers/encrypt new-password)]
    (query/invalidate-reset-key! query/db {:reset_key reset-key})
    (query/update-registered-user-password! query/db {:id user-id :password hashed-password})
    (respond/ok {:message "Password successfully reset"})))

(defn password-reset-response
  "Generate response for password update"
  [reset-key new-password]
  (let [key-row          (query/get-reset-row-by-reset-key query/db {:reset_key reset-key})
        key-exists?      (empty? key-row)
        key-valid-until  (c/from-sql-time (:valid_until key-row))
        key-valid?       (t/before? (t/now) key-valid-until)]
    (cond
      key-exists?             (respond/not-found {:error "Reset key does not exist"})
      (:already_used key-row) (respond/not-found {:error "Reset key already used"})
      key-valid?              (update-password reset-key key-row new-password)
      :else                   (respond/not-found {:error "Reset key has expired"}))))
