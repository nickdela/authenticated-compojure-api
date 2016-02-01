(ns authenticated-compojure-api.middleware.token-auth
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]))

(defn token-auth-mw
  "Middleware used on routes requiring token authentication"
  [handler]
  (fn [request]
    (-> handler
      (wrap-authentication token-backend))))
