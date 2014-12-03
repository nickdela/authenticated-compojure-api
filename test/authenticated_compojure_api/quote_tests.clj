(ns authenticated-compojure-api.quote-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body]]
            [authenticated-compojure-api.queries.quotes :refer [quotes]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(def test-quotes (atom [{:quoteid 1 :author "Jarrod" :quote "Hello"}
                        {:quoteid 2 :author "Jrock" :quote "Good bye"}]))

(facts "Quote api tests"

  (fact "Test GET request to /api/quotes returns all existing quotes"
    (with-redefs [quotes test-quotes]
      (let [response (app (-> (mock/request :get "/api/quotes")))
            body     (parse-body (:body response))]
        (:status response) => 200
        (count body)       => 2
        (first body)       => {:quoteid 1 :quote "Hello" :author "Jarrod"}
        (second body)      => {:quoteid 2 :quote "Good bye" :author "Jrock"})))

  (fact "Test GET request to /api/quotes/{quoteid} returns the expected quote"
    (with-redefs [quotes test-quotes]
      (let [response (app (-> (mock/request :get "/api/quotes/1")))
            body     (parse-body (:body response))]
        (:status response) => 200
        (count body)       => 3
        body               => {:quoteid 1 :quote "Hello" :author "Jarrod"})))

  (fact "Test GET request to /api/quotes/{quoteid} with nonexistent id returns 404"
    (with-redefs [quotes test-quotes]
      (let [response (app (-> (mock/request :get "/api/quotes/4")))
            body     (parse-body (:body response))]
        (:status response) => 404
        (:error body)      => "Not Found")))

 (fact "Test POST request to /api/quotes with a valid quote creates new quote"
    (with-redefs [quotes test-quotes]
    (let [response (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (:status response)     => 200
      (count body)           => 3)))

 (fact "Test POST request to /api/quotes with an invalid quote returns an error"
    (with-redefs [quotes test-quotes]
    (let [response (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:wrong "Bad" :thing "Fail"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (:status response)             => 400
      (:author (:errors body))       => "missing-required-key"
      (:quote-string (:errors body)) => "missing-required-key"))))
