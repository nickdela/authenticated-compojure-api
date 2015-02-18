(ns authenticated-compojure-api.auth-resources.basic-auth-backend
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
            [buddy.hashers.bcrypt :as hs]))

;; ============================================================================
;  This function will delegate determining if we have the correct username and
;  password to authorize a user. The return value will be added to the request
;  with the keyword of :identity
;; ============================================================================
(defn basic-auth [request, auth-data]
  (let [username             (:username auth-data)
        password             (:password auth-data)
        reg-user-with-citext (first (query/get-registered-user-details-by-username {:username username}))
        reg-user-with-strs   (-> reg-user-with-citext
                                 (assoc-in [:username] (str (:username reg-user-with-citext)))
                                 (assoc-in [:email]    (str (:email reg-user-with-citext)))
                                 (dissoc   :password))
        pass-match           (hs/check-password password (:password reg-user-with-citext))]
    (if pass-match reg-user-with-strs false)))

;; ============================================================================
;  Create authentication backend
;; ============================================================================
(def basic-backend (http-basic-backend {:authfn basic-auth}))
