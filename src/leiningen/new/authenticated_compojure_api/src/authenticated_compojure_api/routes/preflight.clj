(ns {{ns-name}}.routes.preflight
  (:require
    [compojure.api.sweet :refer [context OPTIONS]]
    [ring.util.http-response :as respond]
    [{{ns-name}}.middleware.cors :refer [cors-mw]]))

(def preflight-route
  (context "/api" []

    (OPTIONS "*" {:as request}
              :tags ["Preflight"]
              :return {}
              :middleware [cors-mw]
              :summary "This will catch all OPTIONS preflight requests from the
                        browser. It will always return a success for the purpose
                        of the browser retrieving the response headers to validate CORS
                        requests. Does not work in the swagger UI."
              (respond/ok  {}))))
