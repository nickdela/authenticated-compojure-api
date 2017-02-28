(ns {{ns-name}}.route-functions.refresh-token.delete-refresh-token
  (:require [{{ns-name}}.queries.query-defs :as query]
            [ring.util.http-response :as respond]))

(defn remove-refresh-token-response
  "Remove refresh token (error if doesn't exist)"
  [refresh-token]
  (let [null-refresh-token (query/delete-refresh-token! query/db {:refresh_token refresh-token})]
    (if (zero? null-refresh-token)
      (respond/not-found  {:error "The refresh token does not exist"})
      (respond/ok         {:message "Refresh token successfully deleted"}))))
