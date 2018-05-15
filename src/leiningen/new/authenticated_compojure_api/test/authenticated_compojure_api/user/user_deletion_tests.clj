(ns {{ns-name}}.user.user-deletion-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [taoensso.timbre :as timbre]
    [mount.core :as mount]
    [{{ns-name}}.specs :as specs]
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
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database!)))))

(deftest can-delete-user-who-is-not-self-and-associated-permissions-with-valid-token-and-admin-permissions
  (testing "Can delete user who is not self and associated permissions with valid token and admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (is (= 2 (count (query/all-registered-users))))
    (let [user-id (:id (query/get-registered-user-by-username {:username "Everyman"}))
          response (helper/authenticated-delete (str "/api/v1/user/" user-id) {} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "User id " user-id " successfully removed")]
      (is (= 200 (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1 (count (query/all-registered-users))))
      (is (= nil (helper/get-permissions-for-user user-id))))))

(deftest can-delete-self-and-associated-permissions-with-valid-token-and-basic-permissions
  (testing "Can delete self and associated permissions with valid token and basic permissions"
    (is (= 2 (count (query/all-registered-users))))
    (let [user-id (:id (query/get-registered-user-by-username {:username "JarrodCTaylor"}))
          response (helper/authenticated-delete (str "/api/v1/user/" user-id) {} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "User id " user-id " successfully removed")]
      (is (= 200 (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1 (count (query/all-registered-users))))
      (is (= nil (helper/get-permissions-for-user user-id))))))

(deftest can-not-delete-user-who-is-not-self-with-valid-token-and-basic-permissions
  (testing "Can not delete user who is not self with valid token and basic permissions"
    (is (= 2 (count (query/all-registered-users))))
    (let [user-id (:id (query/get-registered-user-by-username {:username "Everyman"}))
          response (helper/authenticated-delete (str "/api/v1/user/" user-id) {} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= 2 (count (query/all-registered-users)))))))

(deftest return-404-when-trying-to-delete-a-user-that-does-not-exists
  (testing "Return 404 when trying to delete a user that does not exists"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [response (helper/authenticated-delete (str "/api/v1/user/" (gen/generate (s/gen ::specs/id))) {} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Userid does not exist" (:error body))))))
