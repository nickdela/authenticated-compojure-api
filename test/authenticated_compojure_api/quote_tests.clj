(ns authenticated-compojure-api.quote-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body]]
            [authenticated-compojure-api.test-utils :refer [token-auth-header]]
            [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [buddy.sign.generic :as s]
            [buddy.auth.backends.token :refer [signed-token-backend]]
            [cheshire.core :as ch]))

(def basic-user {:userid 2 :access "User"  :username "Everyman"      :password "password2" :refresh_token "1HN05Az5P0zUhDDRzdcg"})
(def admin-user {:userid 1 :access "Admin" :username "JarrodCTaylor" :password "password1" :refresh_token "zeRqCTZLoNR8j0irosN9"})

(defn add-users []
  (map query/insert-user<! [basic-user admin-user]))

(defn add-quotes []
  (query/insert-quote<! {:author "Jarrod" :quote "Hello"})
  (query/insert-quote<! {:author "Jrock" :quote "Good bye"}))

(facts "Retrive quote(s) no authentication required"
  (with-state-changes [(before :facts (do
                                        (query/create-quotes-table-if-not-exists!)
                                        (add-quotes)))
                       (after  :facts (query/drop-quotes-table!))]

  (fact "Test GET request to /api/quotes returns all existing quotes"
    (let [response (app (-> (mock/request :get "/api/quotes")))
          body     (parse-body (:body response))]
      (:status response) => 200
      (count body)       => 2
      (first body)       => {:id 1 :quote "Hello" :author "Jarrod"}
      (second body)      => {:id 2 :quote "Good bye" :author "Jrock"}))

  (fact "Test GET request to /api/quotes/{id} returns the expected quote"
    (let [response (app (-> (mock/request :get "/api/quotes/1")))
          body     (parse-body (:body response))]
      (:status response) => 200
      (count body)       => 3
      body               => {:id 1 :quote "Hello" :author "Jarrod"}))

  (fact "Test GET request to /api/quotes/{id} to a nonexistent id returns 404"
    (let [response  (app (-> (mock/request :get "/api/quotes/4")))
          body      (parse-body (:body response))]
      (:status response) => 404
      (:error body)      => "Not Found"))))

(facts "Creating, deleteing and updating quote(s) requires authentication"
  (with-state-changes [(before :facts (do
                                        (query/create-quotes-table-if-not-exists!)
                                        (query/create-users-table-if-not-exists!)
                                        (add-quotes)
                                        (add-users)))
                       (after  :facts (do
                                        (query/drop-quotes-table!)
                                        (query/drop-users-table!)))]

 (fact "Test POST request to /api/quotes with a valid token and quote creates new quote"
   (let [the-token (s/dumps basic-user auth-key)
         response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                            (mock/content-type "application/json")
                            (token-auth-header the-token)))
         body      (parse-body (:body response))]
     (:status response)     => 200
     (count body)           => 3))

 (fact "Test POST request to /api/quotes with an invalid token return not 401"
   (let [response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                            (mock/content-type "application/json")
                            (token-auth-header "bad-token")))
         body      (parse-body (:body response))]
     (:status response) => 401
     (:error body)      => "Not authorized."))

 (fact "Test POST request to /api/quotes with a valid token and an invalid quote returns an error"
   (let [the-token (s/dumps basic-user auth-key)
         response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:wrong "Bad" :thing "Fail"}))
                            (mock/content-type "application/json")
                            (token-auth-header the-token)))
         body      (parse-body (:body response))]
     (:status response)             => 400
     (:author (:errors body))       => "missing-required-key"
     (:quote-string (:errors body)) => "missing-required-key"))

 (fact "Test DELETE to /api/quotes/{id} from a user with 'Admin' auth deletes expected quote"
   (let [the-token (s/dumps admin-user auth-key)
         response  (app (-> (mock/request :delete "/api/quotes/2")
                            (token-auth-header the-token)))
         body      (parse-body (:body response))]
     (:status response) => 200
     (:message body)    => "Quote id 2 successfully removed"))

 (fact "Test DELETE to /api/quotes/{id} from a user without 'Admin' auth returns 401"
   (let [the-token (s/dumps basic-user auth-key)
         response  (app (-> (mock/request :delete "/api/quotes/2")
                            (token-auth-header the-token)))
         body      (parse-body (:body response))]
     (:status response) => 401
     (:error body)      => "Not authorized."))

 (fact "Test PUT to /api/quotes/{id} with a valid token for existing quote correctly updates"
   (let [the-token (s/dumps basic-user auth-key)
         response  (app (-> (mock/request :put "/api/quotes/1" (ch/generate-string {:author "Big Daddy J"}))
                            (mock/content-type "application/json")
                            (token-auth-header the-token)))
         body      (parse-body (:body response))]
     (:status response) => 200
     body               => {:author "Big Daddy J" :quote "Hello" :id 1}))

  (fact "Test PUT to /api/quotes/{id} with an invalid token returns 401"
     (let [response (app (-> (mock/request :put "/api/quotes/1" (ch/generate-string {:author "Big Daddy J"}))
                             (mock/content-type "application/json")
                             (token-auth-header "bad-token")))
           body     (parse-body (:body response))]
       (:status response) => 401
       (:error body)      => "Not authorized."))

  (fact "Test an expired valid token will not pass authentication"
    (with-redefs [token-backend (signed-token-backend {:privkey auth-key :max-age -15})]
     (let [the-token (s/dumps basic-user auth-key)
           response  (app (-> (mock/request :post "/api/quotes" (ch/generate-string {:author "X" :quote-string "Y"}))
                              (mock/content-type "application/json")
                              (token-auth-header the-token)))
           body      (parse-body (:body response))]
       (:status response)     => 401
       (:error body)          => "Not authorized.")))))
