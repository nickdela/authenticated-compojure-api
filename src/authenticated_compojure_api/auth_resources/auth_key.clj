(ns authenticated-compojure-api.auth-resources.auth-key)

;; ============================================================================
;  In a real situation manage this with an environment variable
;  This will be needed in all application that expect to use the tokens created
;  here.
;; ============================================================================
(def auth-key "crazy-secret-phrase")
