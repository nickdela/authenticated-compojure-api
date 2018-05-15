(ns {{ns-name}}.permission.permission-creation-tests
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

(deftest test-permission-creation

  (testing "Can add user permission with valid token and admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (:id (query/get-registered-user-by-username {:username "Everyman"}))
          response (helper/authenticated-post (str "/api/v1/permission/user/" user-id) {:permission "other"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id " successfully added")]
      (is (= 200 (:status response)))
      (is (= expected-response (:message body)))
      (is (= "basic,other" (helper/get-permissions-for-user user-id))))))

(deftest attempting-to-add-a-permission-that-does-not-exist-returns-404
  (testing "Attempting to add a permission that does not exist returns 404"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (:id (query/get-registered-user-by-username {:username "Everyman"}))
          response (helper/authenticated-post (str "/api/v1/permission/user/" user-id) {:permission "stranger"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Permission 'stranger' does not exist" (:error body)))
      (is (= "basic" (helper/get-permissions-for-user user-id))))))

(deftest can-not-add-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not add user permission with valid token and no admin permissions"
    (let [user-id  (:id (query/get-registered-user-by-username {:username "Everyman"}))
          response (helper/authenticated-post (str "/api/v1/permission/user/" user-id) {:permission "other"} "Everyman:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic" (helper/get-permissions-for-user user-id))))))
