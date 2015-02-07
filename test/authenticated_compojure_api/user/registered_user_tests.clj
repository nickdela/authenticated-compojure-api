(ns authenticated-compojure-api.user.registered-user-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(defn setup-teardown [f]
  (query/create-registered-user-table-if-not-exists!)
  (query/create-permission-table-if-not-exists!)
  (query/create-user-permission-table-if-not-exists!)
  (query/insert-permission<! {:permission "basic"})
  (f)
  (query/drop-user-permission-table!)
  (query/drop-permission-table!)
  (query/drop-registered-user-table!))

(use-fixtures :each setup-teardown)

(deftest can-successfully-create-a-new-user-and-user-is-given-basic-permission-as-default
  (testing "Can successfully create a new user and user is given basic permission as default"
    (is (= 0 (count (query/all-registered-users))))
    (is (= 0 (count (query/all-registered-users))))
    (let [response (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "hello@bye.com" :username "Jarrod" :password "the-password"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))
          new-registered-user (first (query/get-user-details-by-username {:username (:username body)}))]
      (is (= 201      (:status response)))
      (is (= 1        (count (query/all-registered-users))))
      (is (= "Jarrod" (:username body)))
      (is (= "Jarrod" (:username new-registered-user)))
      (is (= "basic"  (:permissions new-registered-user))))))

(deftest can-not-create-a-user-if-username-already-exists
  (testing "Can not create a user if username already exists"
    (let [response-1 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "Jrock@Taylor.com" :username "Adam" :password "the-password"}))
                              (mock/content-type "application/json")))
          response-2 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "Jarrod@Taylor.com" :username "Adam" :password "other-password"}))
                              (mock/content-type "application/json")))
          body     (parse-body (:body response-2))]
      (is (= 409                       (:status response-2)))
      (is (= 1                         (count (query/all-registered-users))))
      (is (= "Username already exists" (:error body))))))

(deftest can-not-create-a-user-if-email-already-exists-email-address-use-the-same-case
  (testing "Can not create a user if email already exists email address use the same case"
    (let [response-1 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "jarrod@taylor.com" :username "Burt" :password "the-first-pass"}))
                              (mock/content-type "application/json")))
          response-2 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "jarrod@taylor.com" :username "Steve" :password "the-second-pass"}))
                              (mock/content-type "application/json")))
          body     (parse-body (:body response-2))]
      (is (= 409                       (:status response-2)))
      (is (= 1                         (count (query/all-registered-users))))
      (is (= "Username already exists" (:error body))))))

(deftest can-not-create-a-user-if-email-already-exists-email-address-use-different-case
  (testing "Can not create a user if email already exists email address use the same case use different case"
    (let [response-1 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "wOnkY@email.com" :username "Burt" :password "burt-password"}))
                              (mock/content-type "application/json")))
          response-2 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "WonKy@email.com" :username "Steve" :password "steve-password"}))
                              (mock/content-type "application/json")))
          body     (parse-body (:body response-2))]
      (is (= 409                       (:status response-2)))
      (is (= 1                         (count (query/all-registered-users))))
      (is (= "Username already exists" (:error body))))))

; Case insensitive username
