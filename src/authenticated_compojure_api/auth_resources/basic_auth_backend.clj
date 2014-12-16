(ns authenticated-compojure-api.auth-resources.basic-auth-backend
  (:require [authenticated-compojure-api.queries.users :refer [get-user-by-password]]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]))

;; ============================================================================
;  This function will delagate determining if we have the correct username and
;  password to authroize a user. The return value will be added to the request
;  with the keyword of :identity
;; ============================================================================
(defn basic-auth
  [request, auth-data]
  (let [username (:username auth-data)
        password (:password auth-data)]
    (get-user-by-password username password)))

;; ============================================================================
;  Create authentication backend
;; ============================================================================
(def basic-backend (http-basic-backend {:authfn basic-auth}))
