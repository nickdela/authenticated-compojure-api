(ns authenticated-compojure-api.middleware.token-auth
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]))

(defn token-auth-mw [handler]
  (fn [request]
    (-> handler
      (wrap-authentication token-backend))))
