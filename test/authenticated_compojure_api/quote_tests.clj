(ns authenticated-compojure-api.quote-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated_compojure_api.test_utils :refer [parse-body]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(facts "Quote api tests"

  (fact "Test GET request to /api/quotes returns all existing quotes"
    (let [response (app (-> (mock/request :get "/api/quotes")))
          body     (parse-body (:body response))]
      (:status response)       => 200
      (count body)             => 2
      (:quoteid (first body))  => 1
      (:author (first body))   => "Oscar Wilde"
      (:quoteid (second body)) => 2
      (:author (second body))  => "Plutarch"))

  (fact "Test GET request to /api/quotes/{quoteid} returns the expected quote"
    (let [response (app (-> (mock/request :get "/api/quotes/1")))
          body     (parse-body (:body response))]
      (:status response) => 200
      (count body)       => 3
      (:quoteid body)    => 1
      (:author  body)    => "Oscar Wilde"))

  (fact "Test GET request to /api/quotes/{quoteid} with nonexistent id returns 400"
    (let [response (app (-> (mock/request :get "/api/quotes/4")))
          body     (parse-body (:body response))]
      (:status response) => 400
      (:error body)      => "Bad Request")))
