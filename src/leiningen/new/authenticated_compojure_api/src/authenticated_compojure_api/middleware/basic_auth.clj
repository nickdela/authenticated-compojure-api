(ns {{ns-name}}.middleware.basic-auth
  (:require [buddy.auth.middleware :refer [wrap-authentication]]
            [{{ns-name}}.auth-resources.basic-auth-backend :refer [basic-backend]]))

(defn basic-auth-mw
  "Middleware used on routes requiring basic authentication"
  [handler]
  (wrap-authentication handler basic-backend))
