(ns authenticated-compojure-api.routes.auth
  (:require [authenticated-compojure-api.middleware.basic-auth :refer [basic-auth-mw]]
            [authenticated-compojure-api.middleware.authenticated :refer [authenticated-mw]]
            [authenticated-compojure-api.middleware.cors :refer [cors-mw]]
            [authenticated-compojure-api.route-functions.auth.get-auth-credentials :refer [auth-credentials-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))

(def auth-routes
  "Specify routes for Authentication functions"
  (context "/api/auth" []

     (GET "/"            {:as request}
           :tags          ["Auth"]
           :return        {:id s/Uuid :username String :permissions String :token String :refreshToken String}
           :header-params [authorization :- String]
           :middleware    [basic-auth-mw cors-mw authenticated-mw]
           :summary       "Returns auth info given a username and password in the '`Authorization`' header."
           :description   "Authorization header expects '`Basic username:password`' where `username:password`
                           is base64 encoded. To adhere to basic auth standards we have to use a field called
                           `username` however we will accept a valid username or email as a value for this key."
           (auth-credentials-response request))))
