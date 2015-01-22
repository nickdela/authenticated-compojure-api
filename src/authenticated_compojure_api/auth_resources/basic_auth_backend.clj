(ns authenticated-compojure-api.auth-resources.basic-auth-backend
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]
            [buddy.hashers.bcrypt :as hs]))

;; ============================================================================
;  This function will delagate determining if we have the correct username and
;  password to authroize a user. The return value will be added to the request
;  with the keyword of :identity
;; ============================================================================
(defn basic-auth
  [request, auth-data]
  (let [username   (:username auth-data)
        password   (:password auth-data)
        db-user    (query/get-user-by-username {:username username})
        pass-match (hs/check-password password (:password (first db-user)))]
    (if pass-match (first db-user) false)))

;; ============================================================================
;  Create authentication backend
;; ============================================================================
(def basic-backend (http-basic-backend {:authfn basic-auth}))
