(ns {{ns-name}}.routes.refresh-token
  (:require [{{ns-name}}.middleware.cors :refer [cors-mw]]
            [{{ns-name}}.route-functions.refresh-token.delete-refresh-token :refer [remove-refresh-token-response]]
            [{{ns-name}}.route-functions.refresh-token.gen-new-token :refer [gen-new-token-response]]
            [compojure.api.sweet :refer :all]))

(def refresh-token-routes
  "Specify routes for Refresh-Token functions"
  (context "/api/v1/refresh-token/:refreshToken" []
           :tags        ["Refresh-Token"]
           :middleware  [cors-mw]
           :path-params [refreshToken :- String]

    (GET "/" request
          :return         {:token String :refreshToken String}
          :summary        "Get a fresh token and new refresh-token with a valid refresh-token."
          (gen-new-token-response refreshToken))

    (DELETE "/" request
            :return         {:message String}
            :summary        "Delete the specific refresh-token"
            (remove-refresh-token-response refreshToken))))
