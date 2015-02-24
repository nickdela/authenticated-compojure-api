(ns authenticated-compojure-api.auth-resources.basic-auth-backend
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
            [buddy.hashers.bcrypt :as hs]))

;; ============================================================================
;; The username and email fields are stored in citext fields in Postgres thus
;; the need to convert them to strings for future use.
;; ============================================================================
(defn get-user-info [username]
  (let [registered-user (first (query/get-registered-user-details-by-username {:username username}))]
    (if (nil? registered-user)
      nil
      {:user-data (-> registered-user
                      (assoc-in [:username] (str (:username registered-user)))
                      (assoc-in [:email]    (str (:email registered-user)))
                      (dissoc   :password))
       :password  (:password registered-user)})))

;; ============================================================================
;  This function will delegate determining if we have the correct username and
;  password to authorize a user. The return value will be added to the request
;  with the keyword of :identity
;; ============================================================================
(defn basic-auth [request, auth-data]
  (let [username  (:username auth-data)
        password  (:password auth-data)
        user-info (get-user-info username)]
    (if (and user-info (hs/check-password password (:password user-info)))
      (:user-data user-info)
      false)))

;; ============================================================================
;  Create authentication backend
;; ============================================================================
(def basic-backend (http-basic-backend {:authfn basic-auth}))
