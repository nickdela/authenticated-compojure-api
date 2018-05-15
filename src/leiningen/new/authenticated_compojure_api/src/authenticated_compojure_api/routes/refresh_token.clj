(ns {{ns-name}}.routes.refresh-token
  (:require
    [compojure.api.sweet :refer [context GET DELETE]]
    [{{ns-name}}.specs :as specs]
    [{{ns-name}}.general-functions.validations :as validations]
    [{{ns-name}}.middleware.cors :refer [cors-mw]]
    [{{ns-name}}.route-functions.refresh-token.delete-refresh-token :refer [remove-refresh-token-response]]
    [{{ns-name}}.route-functions.refresh-token.gen-new-token :refer [gen-new-token-response]]))

(def refresh-token-routes
  (context "/api/v1/refresh-token/:refresh-token" []
           :tags ["Refresh-Token"]
           :coercion validations/spec
           :middleware [cors-mw]
           :path-params [refresh-token :- ::specs/refresh-token]

           (GET "/" request
                :return ::specs/refresh-token-response
                :summary "Get a fresh token and new refresh-token with a valid refresh-token."
                (gen-new-token-response refresh-token))

           (DELETE "/" request
                   :return {:message ::specs/message}
                   :summary "Delete the specific refresh-token"
                   (remove-refresh-token-response refresh-token))))
