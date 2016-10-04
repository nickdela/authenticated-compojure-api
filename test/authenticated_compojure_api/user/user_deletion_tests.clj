(ns authenticated-compojure-api.user.user-deletion-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :as helper]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(defn setup-teardown [f]
  (try
    (query/insert-permission! query/db {:permission "basic"})
    (helper/add-users)
    (f)
    (finally (query/truncate-all-tables-in-database! query/db))))

(use-fixtures :once helper/create-tables)
(use-fixtures :each setup-teardown)

(deftest can-delete-user-who-is-not-self-and-associated-permissions-with-valid-token-and-admin-permissions
  (testing "Can delete user who is not self and associated permissions with valid token and admin permissions"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          user-id-2         (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          _                 (is (= 2 (count (query/all-registered-users query/db))))
          _                 (is (= "basic" (helper/get-permissions-for-user user-id-2)))
          _                 (query/insert-permission! query/db {:permission "admin"})
          _                 (query/insert-permission-for-user! query/db {:userid user-id-1 :permission "admin"})
          response          (app (-> (mock/request :delete (str "/api/v1/user/" user-id-2))
                                     (mock/content-type "application/json")
                                     (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body              (helper/parse-body (:body response))
          expected-response (str "User id " user-id-2 " successfully removed")]
      (is (= 200               (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1                 (count (query/all-registered-users query/db))))
      (is (= nil (helper/get-permissions-for-user user-id-2))))))

(deftest can-delete-self-and-associated-permissions-with-valid-token-and-basic-permissions
  (testing "Can delete self and associated permissions with valid token and basic permissions"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          _                 (is (= "basic" (helper/get-permissions-for-user user-id-1)))
          _                 (is (= 2 (count (query/all-registered-users query/db))))
          response          (app (-> (mock/request :delete (str "/api/v1/user/" user-id-1))
                                     (mock/content-type "application/json")
                                     (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body              (helper/parse-body (:body response))
          expected-response (str "User id " user-id-1 " successfully removed")]
      (is (= 200               (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1                 (count (query/all-registered-users query/db))))
      (is (= nil               (helper/get-permissions-for-user user-id-1))))))

(deftest can-not-delete-user-who-is-not-self-with-valid-token-and-basic-permissions
  (testing "Can not delete user who is not self with valid token and basic permissions"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          user-id-2         (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          _                 (is (= 2 (count (query/all-registered-users query/db))))
          response (app (-> (mock/request :delete (str "/api/v1/user/" user-id-2))
                            (mock/content-type "application/json")
                            (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= 2                (count (query/all-registered-users query/db)))))))

(deftest return-404-when-trying-to-delete-a-user-that-does-not-exists
  (testing "Return 404 when trying to delete a user that does not exists"
    (let [user-id-1  (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          _          (query/insert-permission! query/db {:permission "admin"})
          _          (query/insert-permission-for-user! query/db {:userid user-id-1 :permission "admin"})
          response   (app (-> (mock/request :delete "/api/v1/user/83b811-edf0-48ec-84-5a142e2c3a75")
                              (mock/content-type "application/json")
                              (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body       (helper/parse-body (:body response))]
      (is (= 404                     (:status response)))
      (is (= "Userid does not exist" (:error body))))))
