(ns authenticated-compojure-api.test-utils
  (:require [cheshire.core :as cheshire]
            [ring.mock.request :as mock]
            [buddy.core.codecs :refer [str->base64]]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn basic-auth-header
  [request original]
  (mock/header request "Authorization" (str "Basic " (str->base64 original))))

(defn token-auth-header
  [request token]
  (mock/header request "Authorization" (str "Token " token)))
