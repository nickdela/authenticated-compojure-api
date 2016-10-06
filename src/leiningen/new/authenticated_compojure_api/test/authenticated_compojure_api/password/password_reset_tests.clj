(ns {{ns-name}}.password.password-reset-tests
  (:require [clojure.test :refer :all]
            [{{ns-name}}.handler :refer :all]
            [{{ns-name}}.test-utils :as helper]
            [{{ns-name}}.queries.query-defs :as query]
            [buddy.hashers :as hashers]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn setup-teardown [f]
  (try
    (query/insert-permission! query/db {:permission "basic"})
    (helper/add-users)
    (f)
    (finally (query/truncate-all-tables-in-database! query/db))))

(use-fixtures :once helper/create-tables)
(use-fixtures :each setup-teardown)

(deftest test-password-is-reset-with-valid-reset-key
  (testing "Test password is reset with valid resetKey"
    (let [user-id-1    (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          _            (query/insert-password-reset-key-with-default-valid-until! query/db {:reset_key "123" :user_id user-id-1})
          response     (app (-> (mock/request :post "/api/v1/password/reset-confirm" (ch/generate-string {:resetKey "123" :newPassword "new-pass"}))
                                (mock/content-type "application/json")))
          body         (helper/parse-body (:body response))
          updated-user (query/get-registered-user-by-id query/db {:id user-id-1})]
      (is (= 200 (:status response)))
      (is (= true (hashers/check "new-pass" (:password updated-user))))
      (is (= "Password successfully reset" (:message body))))))

(deftest not-found-404-is-returned-when-invlid-reset-key-id-used
  (testing "Not found 404 is returned when invalid reset key is used"
    (let [response (app (-> (mock/request :post "/api/v1/password/reset-confirm" (ch/generate-string {:resetKey "123" :newPassword "new-pass"}))
                            (mock/content-type "application/json")))
          body     (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Reset key does not exist" (:error body))))))

(deftest not-found-404-is-returned-when-valid-reset-key-has-expired
  (testing "Not found 404 is returned when valid reset key has expired"
    (let [user-id-1    (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          _            (query/insert-password-reset-key-with-valid-until-date! query/db {:reset_key "123" :user_id user-id-1 :valid_until (c/to-sql-time (t/minus (t/now) (t/hours 24)))})
          response     (app (-> (mock/request :post "/api/v1/password/reset-confirm" (ch/generate-string {:resetKey "123" :newPassword "new-pass"}))
                                (mock/content-type "application/json")))
          body         (helper/parse-body (:body response))
          updated-user (query/get-registered-user-by-id query/db {:id user-id-1})]
      (is (= 404 (:status response)))
      (is (= "Reset key has expired" (:error body))))))

(deftest password-is-not-reset-if-reset-key-has-already-been-used
  (testing "Password is not reset if reset key has already been used"
    (let [user-id-1        (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          _                (query/insert-password-reset-key-with-default-valid-until! query/db {:reset_key "123" :user_id user-id-1})
          initial-response (app (-> (mock/request :post "/api/v1/password/reset-confirm" (ch/generate-string {:resetKey "123" :newPassword "new-pass"}))
                                    (mock/content-type "application/json")))
          second-response  (app (-> (mock/request :post "/api/v1/password/reset-confirm" (ch/generate-string {:resetKey "123" :newPassword "nono"}))
                                    (mock/content-type "application/json")))
          body             (helper/parse-body (:body second-response))
          updated-user (query/get-registered-user-by-id query/db {:id user-id-1})]
      (is (= 200 (:status initial-response)))
      (is (= 404 (:status second-response)))
      (is (= true (hashers/check "new-pass" (:password updated-user))))
      (is (= "Reset key already used" (:error body))))))
