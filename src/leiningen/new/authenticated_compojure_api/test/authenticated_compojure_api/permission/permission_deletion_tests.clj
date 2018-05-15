(ns {{ns-name}}.permission.permission-deletion-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [taoensso.timbre :as timbre]
    [mount.core :as mount]
    [{{ns-name}}.test-utils :as helper]
    [{{ns-name}}.query-defs :as query]))

(use-fixtures :once (fn [f]
                      (timbre/merge-config! {:level :warn})
                      (mount/start)
                      (f)))

(use-fixtures :each (fn [f]
                      (try
                        (query/insert-permission! {:permission "basic"})
                        (query/insert-permission! {:permission "admin"})
                        (query/insert-permission! {:permission "other"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database!)))))

(deftest can-delete-user-permission-with-valid-token-and-admin-permissions
  (testing "Can delete user permission with valid token and admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (helper/add-permission-for-username "Everyman" "other")
    (let [user-id (:id (query/get-registered-user-by-username {:username "Everyman"}))
          response (helper/authenticated-delete (str "/api/v1/permission/user/" user-id) {:permission "other"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id " successfully removed")]
      (is (= 200 (:status response)))
      (is (= "basic" (helper/get-permissions-for-user user-id)))
      (is (= expected-response (:message body))))))

(deftest can-not-delete-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not delete user permission with valid token and no admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (:id (query/get-registered-user-by-username {:username "JarrodCTaylor"}))
          response (helper/authenticated-delete (str "/api/v1/permission/user/" user-id) {:permission "other"} "Everyman:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic,admin" (helper/get-permissions-for-user user-id))))))
