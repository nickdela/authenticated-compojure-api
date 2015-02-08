(ns authenticated-compojure-api.user.creation-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(defn setup-teardown [f]
  (try
    (query/create-registered-user-table-if-not-exists!)
    (query/create-permission-table-if-not-exists!)
    (query/create-user-permission-table-if-not-exists!)
    (query/insert-permission<! {:permission "basic"})
    (f)
    (finally
      (query/drop-user-permission-table!)
      (query/drop-permission-table!)
      (query/drop-registered-user-table!))))

(use-fixtures :each setup-teardown)

(deftest can-successfully-create-a-new-user-who-is-given-basic-permission-as-default
  (testing "Can successfully create a new user who is given basic permission as default"
    (is (= 0 (count (query/all-registered-users))))
    (let [user-info           {:email "J@Taylor.com" :username "Jarrod" :password "pass"}
          response            (app (-> (mock/request :post "/api/user" (ch/generate-string user-info))
                                       (mock/content-type "application/json")))
          body                (parse-body (:body response))
          new-registered-user (first (query/get-user-details-by-username {:username (:username body)}))]
      (is (= 201      (:status response)))
      (is (= 1        (count (query/all-registered-users))))
      (is (= "Jarrod" (:username body)))
      (is (= "Jarrod" (str (:username new-registered-user))))
      (is (= "basic"  (:permissions new-registered-user))))))

(deftest can-not-create-a-user-if-username-already-exists-using-the-same-case
  (testing "Can not create a user if username already exists using the same case"
    (let [user-info-1 {:email "Jrock@Taylor.com" :username "Jarrod" :password "pass"}
          user-info-2 {:email "J@Taylor.com"     :username "Jarrod" :password "pass"}
          response-1  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-1))
                               (mock/content-type "application/json")))
          response-2  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-2))
                               (mock/content-type "application/json")))
          body        (parse-body (:body response-2))]
      (is (= 409                       (:status response-2)))
      (is (= 1                         (count (query/all-registered-users))))
      (is (= "Username already exists" (:error body))))))

(deftest can-not-create-a-user-if-username-already-exists-using-mixed-case
  (testing "Can not create a user if username already exists using mixed case"
    (let [user-info-1 {:email "Jrock@Taylor.com" :username "jarrod" :password "pass"}
          user-info-2 {:email "J@Taylor.com"     :username "Jarrod" :password "pass"}
          response-1  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-1))
                               (mock/content-type "application/json")))
          response-2  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-2))
                               (mock/content-type "application/json")))
          body        (parse-body (:body response-2))]
      (is (= 409                       (:status response-2)))
      (is (= 1                         (count (query/all-registered-users))))
      (is (= "Username already exists" (:error body))))))

(deftest can-not-create-a-user-if-email-already-exists-using-the-same-case
  (testing "Can not create a user if email already exists using the same case"
    (let [user-info-1 {:email "jarrod@taylor.com" :username "Burt" :password "the-first-pass"}
          user-info-2 {:email "jarrod@taylor.com" :username "Steve" :password "the-second-pass"}
          response-1  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-1))
                               (mock/content-type "application/json")))
          response-2  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-2))
                               (mock/content-type "application/json")))
          body        (parse-body (:body response-2))]
      (is (= 409                    (:status response-2)))
      (is (= 1                      (count (query/all-registered-users))))
      (is (= "Email already exists" (:error body))))))

(deftest can-not-create-a-user-if-email-already-exists-using-mixed-case
  (testing "Can not create a user if email already exists using mixed case"
    (let [user-info-1 {:email "wOnkY@email.com" :username "Jarrod" :password "Pass"}
          user-info-2 {:email "WonKy@email.com" :username "Jrock"  :password "Pass"}
          response-1  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-1))
                               (mock/content-type "application/json")))
          response-2  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-2))
                               (mock/content-type "application/json")))
          body        (parse-body (:body response-2))]
      (is (= 409                    (:status response-2)))
      (is (= 1                      (count (query/all-registered-users))))
      (is (= "Email already exists" (:error body))))))

(deftest can-not-create-a-user-if-username-and-email-already-exist-using-same-and-mixed-case
  (testing "Can not create a user if username and email already exist using same and mixed case"
    (let [user-info-1 {:email "wOnkY@email.com" :username "jarrod" :password "pass"}
          user-info-2 {:email "WonKy@email.com" :username "jarrod" :password "pass"}
          response-1  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-1))
                               (mock/content-type "application/json")))
          response-2  (app (-> (mock/request :post "/api/user" (ch/generate-string user-info-2))
                               (mock/content-type "application/json")))
          body        (parse-body (:body response-2))]
      (is (= 409                                (:status response-2)))
      (is (= 1                                  (count (query/all-registered-users))))
      (is (= "Username and Email already exist" (:error body))))))
