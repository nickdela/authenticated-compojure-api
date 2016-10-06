(ns {{ns-name}}.permission.permission-deletion-tests
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

(deftest can-delete-user-permission-with-valid-token-and-admin-permissions
  (testing "Can delete user permission with valid token and admin permissions"
    (let [user-id-1         (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          user-id-2         (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          _                 (query/insert-permission-for-user! query/db {:userid user-id-1 :permission "admin"})
          _                 (query/insert-permission-for-user! query/db {:userid user-id-2 :permission "other"})
          _                 (is (= "basic,other" (:permissions (query/get-permissions-for-userid query/db {:userid user-id-2}))))
          response          (app (-> (mock/request :delete (str "/api/v1/permission/user/" user-id-2) (ch/generate-string {:permission "other"}))
                                     (mock/content-type "application/json")
                                     (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body              (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id-2 " successfully removed")]
      (is (= 200               (:status response)))
      (is (= "basic"           (helper/get-permissions-for-user user-id-2)))
      (is (= expected-response (:message body))))))

(deftest can-not-delete-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not delete user permission with valid token and no admin permissions"
    (let [user-id-1  (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          _          (query/insert-permission-for-user! query/db {:userid user-id-1 :permission "admin"})
          _          (is (= "basic,admin" (:permissions (query/get-permissions-for-userid query/db {:userid user-id-1}))))
          response   (app (-> (mock/request :delete (str "/api/v1/permission/user/" user-id-1) (ch/generate-string {:permission "other"}))
                              (mock/content-type "application/json")
                              (helper/get-token-auth-header-for-user "Everyman:pass")))
          body       (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic,admin"    (helper/get-permissions-for-user user-id-1))))))
