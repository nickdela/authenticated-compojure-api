(ns authenticated-compojure-api.middleware.authenticated
  (:require [buddy.auth :refer [authenticated?]]
            [ring.util.http-response :refer [unauthorized]]))

(defn authenticated-mw
  "authenticated? Buddy auth middleware"
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (unauthorized {:error "Not authorized"}))))

