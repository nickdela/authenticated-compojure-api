(ns authenticated-compojure-api.user.user-tests
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

(deftest user-tests

  (testing "Can successfully create a new user"
    (let [response (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "Jarrod@Taylor.com" :username "Jarrod" :password "the-password"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (is (= 201      (:status response)))
      (is (= 1        (count (query/all-registered-users))))
      (is (= "Jarrod" (:username body)))))

  (testing "Can not create a user if username already exists"
    (let [response-1 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "Jarrod@Talor.com" :username "Jarrod" :password "the-password"}))
                              (mock/content-type "application/json")))
          response-2 (app (-> (mock/request :post "/api/user" (ch/generate-string {:email "Jarrod@Taylor.com" :username "Jarrod" :password "other-password"}))
                              (mock/content-type "application/json")))
          body     (parse-body (:body response-2))]
      (is (= 409                       (:status response-2)))
      (is (= 1                         (count (query/all-registered-users))))
      (is (= "Username already exists" (:error body))))))

; Can not create a user if email already exists
; Can not create a user if email with different case exists
