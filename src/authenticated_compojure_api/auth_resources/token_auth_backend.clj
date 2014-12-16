(ns authenticated-compojure-api.auth-resources.token-auth-backend
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [buddy.auth.backends.token :refer [signed-token-backend]]))

;; ============================================================================
;  Tokens are valid for fifteen minutes after creation. If token is valid the
;  decoded contents of the token will be added to the request with the keyword
;  of :identity
;; ============================================================================
(def token-max-age (* 15 60))
(def token-backend (signed-token-backend {:privkey auth-key :max-age token-max-age}))
