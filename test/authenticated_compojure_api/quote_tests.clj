(ns authenticated-compojure-api.quote-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body token-auth-header]]
            [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.auth-resources.token-auth-backend :refer [token-backend]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [buddy.sign.generic :as s]
            [buddy.auth.backends.token :refer [signed-token-backend]]
            [cheshire.core :as ch]))

(def basic-user {:email "every@man.com"     :username "Everyman"      :password "password2" :refresh_token "1HN05Az5P0zUhDDRzdcg" :permissions "basic"})
(def admin-user {:email "jarrod@taylor.com" :username "JarrodCTaylor" :password "password1" :refresh_token "zeRqCTZLoNR8j0irosN9" :permissions "basic,admin"})

(defn add-quotes []
  (query/insert-quote<! {:author "Jarrod" :quote "Hello"})
  (query/insert-quote<! {:author "Jrock" :quote "Good bye"}))

(defn setup-teardown [f]
  (try
    (query/create-quotes-table-if-not-exists!)
    (add-quotes)
    (f)
    (finally (query/drop-quotes-table!))))

(use-fixtures :each setup-teardown)

(deftest retrive-quote-no-authentication-required

  (testing "Test GET request to /api/quote returns all existing quotes"
    (let [response (app (-> (mock/request :get "/api/quote")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (count body)       2))
      (is (= (first body)       {:id 1 :quote "Hello" :author "Jarrod"}))
      (is (= (second body)      {:id 2 :quote "Good bye" :author "Jrock"}))))

  (testing "Test GET request to /api/quote/{id} returns the expected quote"
    (let [response (app (-> (mock/request :get "/api/quote/1")))
          body     (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (count body)       3))
      (is (= body               {:id 1 :quote "Hello" :author "Jarrod"}))))

  (testing "Test GET request to /api/quote/{id} to a nonexistent id returns 404"
    (let [response  (app (-> (mock/request :get "/api/quote/4")))
          body      (parse-body (:body response))]
      (is (= (:status response) 404))
      (is (= (:error body)      "Not Found")))))

(deftest creating-deleteing-and-updating-quotes-requires-authentication

  (testing "Test POST request to /api/quote with a valid token and quote creates new quote"
    (let [the-token (s/dumps basic-user auth-key)
          response  (app (-> (mock/request :post "/api/quote" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                             (mock/content-type "application/json")
                             (token-auth-header the-token)))
          body      (parse-body (:body response))]
      (is (= 200 (:status response)))
      (is (= 3   (count body)))))

  (testing "Test POST request to /api/quote with an invalid token return not 401"
    (let [response  (app (-> (mock/request :post "/api/quote" (ch/generate-string {:author "Jarrod" :quote-string "A test"}))
                             (mock/content-type "application/json")
                             (token-auth-header "bad-token")))
          body      (parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))))

  (testing "Test POST request to /api/quote with a valid token and an invalid quote returns an error"
    (let [the-token (s/dumps basic-user auth-key)
          response  (app (-> (mock/request :post "/api/quote" (ch/generate-string {:wrong "Bad" :thing "Fail"}))
                             (mock/content-type "application/json")
                             (token-auth-header the-token)))
          body      (parse-body (:body response))]
      (is (= 400                    (:status response)))
      (is (= "missing-required-key" (:author (:errors body))))
      (is (= "missing-required-key" (:quote-string (:errors body))))))

  (testing "Test DELETE to /api/quote/{id} from a user with 'admin' auth deletes expected quote"
    (let [the-token (s/dumps admin-user auth-key)
          response  (app (-> (mock/request :delete "/api/quote/2")
                             (token-auth-header the-token)))
          body      (parse-body (:body response))]
      (is (= 200                               (:status response)))
      (is (= "Quote id 2 successfully removed" (:message body)))))

  (testing "Test DELETE to /api/quote/{id} from a user without 'admin' auth returns 401"
    (let [the-token (s/dumps basic-user auth-key)
          response  (app (-> (mock/request :delete "/api/quote/2")
                             (token-auth-header the-token)))
          body      (parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))))

  (testing "Test PUT to /api/quote/{id} with a valid token for existing quote correctly updates"
    (let [the-token (s/dumps basic-user auth-key)
          response  (app (-> (mock/request :put "/api/quote/1" (ch/generate-string {:author "Big Daddy J"}))
                             (mock/content-type "application/json")
                             (token-auth-header the-token)))
          body      (parse-body (:body response))]
      (is (= 200                   (:status response)))
      (is (= {:author "Big Daddy J"
              :quote  "Hello"
              :id     1}            body))))

  (testing "Test PUT to /api/quote/{id} with an invalid token returns 401"
    (let [response (app (-> (mock/request :put "/api/quote/1" (ch/generate-string {:author "Big Daddy J"}))
                            (mock/content-type "application/json")
                            (token-auth-header "bad-token")))
          body     (parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))))

  (testing "Test an expired valid token will not pass authentication"
    (with-redefs [token-backend (signed-token-backend {:privkey auth-key :max-age -15})]
      (let [the-token (s/dumps basic-user auth-key)
            response  (app (-> (mock/request :post "/api/quote" (ch/generate-string {:author "X" :quote-string "Y"}))
                               (mock/content-type "application/json")
                               (token-auth-header the-token)))
            body      (parse-body (:body response))]
        (is (= 401              (:status response)))
        (is (= "Not authorized" (:error body)))))))
