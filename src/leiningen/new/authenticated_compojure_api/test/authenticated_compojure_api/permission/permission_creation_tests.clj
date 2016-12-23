(ns {{ns-name}}.permission.permission-creation-tests
  (:require [clojure.test :refer :all]
            [{{ns-name}}.handler :refer :all]
            [{{ns-name}}.test-utils :as helper]
            [{{ns-name}}.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(defn setup-teardown [f]
  (try
    (query/insert-permission! query/db {:permission "basic"})
    (query/insert-permission! query/db {:permission "admin"})
    (query/insert-permission! query/db {:permission "other"})
    (helper/add-users)
    (f)
    (finally (query/truncate-all-tables-in-database! query/db))))

(use-fixtures :once helper/create-tables)
(use-fixtures :each setup-teardown)

(deftest can-add-user-permission-with-valid-token-and-admin-permissions
  (testing "Can add user permission with valid token and admin permissions"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          user-id-2         (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          _                 (is (= "basic" (:permissions (query/get-permissions-for-userid query/db {:userid user-id-1}))))
          _                 (query/insert-permission-for-user! query/db {:userid user-id-1 :permission "admin"})
          response          (app (-> (mock/request :post (str "/api/v1/permission/user/" user-id-2) (ch/generate-string {:permission "other"}))
                                     (mock/content-type "application/json")
                                     (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body              (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id-2 " successfully added")]
      (is (= 200               (:status response)))
      (is (= expected-response (:message body)))
      (is (= "basic,other"     (helper/get-permissions-for-user user-id-2))))))

(deftest attempting-to-add-a-permission-that-does-not-exist-returns-404
  (testing "Attempting to add a permission that does not exist returns 404"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          user-id-2         (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          _                 (query/insert-permission-for-user! query/db {:userid user-id-1 :permission "admin"})
          _                 (is (= "basic" (:permissions (query/get-permissions-for-userid query/db {:userid user-id-2}))))
          response (app (-> (mock/request :post (str "/api/v1/permission/user/" user-id-2))
                            (mock/content-type "application/json")
                            (mock/body (ch/generate-string {:permission "stranger"}))
                            (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body     (helper/parse-body (:body response))]
      (is (= 404                                    (:status response)))
      (is (= "Permission 'stranger' does not exist" (:error body)))
      (is (= "basic"                                (helper/get-permissions-for-user user-id-2))))))

(deftest can-not-add-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not add user permission with valid token and no admin permissions"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          _                 (is (= "basic" (:permissions (query/get-permissions-for-userid query/db {:userid user-id-1}))))
          response (app (-> (mock/request :post (str "/api/v1/permission/user/" user-id-1))
                            (mock/content-type "application/json")
                            (mock/body (ch/generate-string {:permission "other"}))
                            (helper/get-token-auth-header-for-user "Everyman:pass")))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic"          (helper/get-permissions-for-user user-id-1))))))
