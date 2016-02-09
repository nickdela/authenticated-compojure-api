(ns authenticated-compojure-api.middleware.basic-auth
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]))

(defn basic-auth-mw
  "Middleware used on routes requiring basic authentication"
  [handler]
  (fn [request]
    (-> handler
      (wrap-authentication basic-backend))))
