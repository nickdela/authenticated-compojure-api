(ns authenticated-compojure-api.token-tests
  (:use midje.sweet)
  (:require [ring.mock.request :as mock]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated_compojure_api.test_utils :refer [parse-body]]))


(facts "Token tests"

  (fact "User cannot retrieve token with invalid credentials"
    (let [response (app (mock/request :get "/api/token/placeholder"))
          body     (parse-body (:body response))]
      (:status response) => 200
      (:message body) => "Place holder...")))

