(ns authenticated-compojure-api.quote-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body]]
            [authenticated-compojure-api.queries.quotes :refer [quotes]]
            [authenticated-compojure-api.test-utils :refer [token-auth-header]]
            [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [buddy.sign.generic :as s]
            [buddy.auth.backends.token :refer [signed-token-backend]]
            [cheshire.core :as ch]))

(def test-auth-user  {:userid 1 :access "Admin" :username "JarrodCTaylor" :password "password1" :refresh-token "zeRqCTZLoNR8j0irosN9"})
(def test-basic-user {:userid 2 :access "User"  :username "Everyman"      :password "password2" :refresh-token "1HN05Az5P0zUhDDRzdcg"})


(defn make-test-quotes []
  (let [test-quotes (atom [{:quoteid 1 :author "Jarrod" :quote "Hello"}
                           {:quoteid 2 :author "Jrock" :quote "Good bye"}])]
    test-quotes))

(facts "Quote api tests"

  (fact "Test GET request to /api/quotes returns all existing quotes"
    (with-redefs [quotes (make-test-quotes)]
      (let [response (app (-> (mock/request :get "/api/quotes")))
            body     (parse-body (:body response))]
        (:status response) => 200
        (count body)       => 2
        (first body)       => {:quoteid 1 :quote "Hello" :author "Jarrod"}
        (second body)      => {:quoteid 2 :quote "Good bye" :author "Jrock"})))

  (fact "Test GET request to /api/quotes/{quoteid} returns the expected quote"
    (with-redefs [quotes (make-test-quotes)]
      (let [response (app (-> (mock/request :get "/api/quotes/1")))
            body     (parse-body (:body response))]
        (:status response) => 200
        (count body)       => 3
        body               => {:quoteid 1 :quote "Hello" :author "Jarrod"})))

  (fact "Test GET request to /api/quotes/{quoteid} to a nonexistent id returns 404"
    (with-redefs [quotes (make-test-quotes)]
      (let [response  (app (-> (mock/request :get "/api/quotes/4")))
            body      (parse-body (:body response))]
        (:status response) => 404
        (:error body)      => "Not Found")))

 (fact "Test POST request to /api/quotes with a valid token and quote creates new quote"
   (with-redefs [quotes (make-test-quotes)]
     (let [the-token (s/dumps test-basic-user auth-key)
           response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                              (mock/content-type "application/json")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response)     => 200
       (count body)           => 3)))

 (fact "Test POST request to /api/quotes with an invalid token return not 401"
   (with-redefs [quotes (make-test-quotes)]
     (let [response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                              (mock/content-type "application/json")
                              (token-auth-header "bad-token")))
           body      (parse-body (:body response))]
       (:status response) => 401
       (:error body)      => "Not authorized.")))

 (fact "Test POST request to /api/quotes with a valid token and an invalid quote returns an error"
   (with-redefs [quotes (make-test-quotes)]
     (let [the-token (s/dumps test-basic-user auth-key)
           response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:wrong "Bad" :thing "Fail"}))
                              (mock/content-type "application/json")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response)             => 400
       (:author (:errors body))       => "missing-required-key"
       (:quote-string (:errors body)) => "missing-required-key")))

 (fact "Test DELETE to /api/quotes/{quoteid} from a user with 'Admin' auth deletes expected quote"
   (with-redefs [quotes (make-test-quotes)]
     (let [the-token (s/dumps test-auth-user auth-key)
           response  (app (-> (mock/request :delete "/api/quotes/2")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response) => 200
       (:message body)    => "Quote id 2 successfully removed")))

 (fact "Test DELETE to /api/quotes/{quoteid} from a user without 'Admin' auth returns 401"
   (with-redefs [quotes (make-test-quotes)]
     (let [the-token (s/dumps test-basic-user auth-key)
           response  (app (-> (mock/request :delete "/api/quotes/2")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response) => 401
       (:error body)      => "Not authorized.")))

 (fact "Test PUT to /api/quotes/{quoteid} with a valid token for existing quote correctly updates"
   (with-redefs [quotes (make-test-quotes)]
     (let [the-token (s/dumps test-basic-user auth-key)
           response  (app (-> (mock/request :put "/api/quotes/1" (ch/generate-string {:author "Big Daddy J"}))
                              (mock/content-type "application/json")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response) => 200
       body               => {:author "Big Daddy J" :quote "Hello" :quoteid 1})))

  (fact "Test PUT to /api/quotes/{quoteid} with an invalid token returns 401"
   (with-redefs [quotes (make-test-quotes)]
     (let [response (app (-> (mock/request :put "/api/quotes/1" (ch/generate-string {:author "Big Daddy J"}))
                             (mock/content-type "application/json")
                             (token-auth-header "bad-token")))
           body     (parse-body (:body response))]
       (:status response) => 401
       (:error body)      => "Not authorized.")))

  (fact "Test an expired valid token will not pass authentication"
    (with-redefs [token-backend (signed-token-backend {:privkey auth-key :max-age -15})]
     (let [the-token (s/dumps test-basic-user auth-key)
           response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "X" :quote-string "Y"}))
                              (mock/content-type "application/json")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response)     => 401
       (:error body)          => "Not authorized."))))
