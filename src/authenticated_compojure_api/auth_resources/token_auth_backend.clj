(ns authenticated-compojure-api.auth-resources.token-auth-backend
  (:require [buddy.auth.backends.token :refer [signed-token-backend]]
            [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]))

;; ============================================
;; Tokens are valied for fifteen minutes after creation
;; ============================================
(def token-max-age (* 15 60))
(def token-backend (signed-token-backend {:privkey auth-key :max-age token-max-age}))
