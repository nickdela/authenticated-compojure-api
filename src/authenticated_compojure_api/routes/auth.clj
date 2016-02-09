(ns authenticated-compojure-api.routes.auth
  (:require [authenticated-compojure-api.middleware.authenticated :refer [authenticated-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.auth.get-auth-credentials :refer [auth-credentials-response]]
            [authenticated-compojure-api.auth-resources.basic-auth-backend :refer [basic-backend]]
            [schema.core :as s]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [compojure.api.sweet :refer :all]))

(def auth-routes
  "Specify routes for Authentication functions"
  (context "/api/auth" []

    (wrap-authentication
      (GET "/"            {:as request}
       :tags          ["Auth"]
       :return        {:id s/Uuid :username String :permissions String :token String :refreshToken String}
       :header-params [authorization :- String]
       :middleware    [cors-mw authenticated-mw]
       :summary       "Returns auth info given a username and password in the '`Authorization`' header."
       :description   "Authorization header expects '`Basic username:password`' where `username:password`
                      is base64 encoded. To adhere to basic auth standards we have to use a field called
                      `username` however we will accept a valid username or email as a value for this key."
       (auth-credentials-response request))
      basic-backend)))
