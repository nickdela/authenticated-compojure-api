(ns {{ns-name}}.user.user-modify-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [taoensso.timbre :as timbre]
    [mount.core :as mount]
    [buddy.hashers :as hashers]
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

(deftest can-modify-a-users-username-with-valid-token-and-admin-permissions
  (testing "Can modify a users username with valid token and admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (helper/get-id-for-user "JarrodCTaylor")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:username "Newman"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))]
      (is (= 200 (:status response)))
      (is (= "Newman" (:username body)))
      (is (= "j@man.com" (:email body))))))

(deftest can-modify-a-users-email-with-valid-token-and-admin-permissions
  (testing "Can modify a users email with valid token and admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (helper/get-id-for-user "JarrodCTaylor")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:email "new@email.com"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))
          updated-user (query/get-registered-user-by-id {:id user-id})]
      (is (= 200 (:status response)))
      (is (= "JarrodCTaylor" (:username body)))
      (is (= "new@email.com" (:email body)))
      (is (= "new@email.com" (str (:email updated-user)))))))

(deftest can-modify-a-users-password-with-valid-token-and-admin-permissions
  (testing "Can modify a users password with valid token and admin permissions"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (helper/get-id-for-user "JarrodCTaylor")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:password "newPasss"} "JarrodCTaylor:passwords")
          updated-user (query/get-registered-user-by-id {:id user-id})]
      (is (= 200 (:status response)))
      (is (= true (hashers/check "newPasss" (:password updated-user)))))))

(deftest can-modify-your-own-password-with-valid-token-and-no-admin-permissions
  (testing "Can modify your own password with valid token and no admin permissions"
    (let [user-id (helper/get-id-for-user "JarrodCTaylor")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:password "newPasss"} "JarrodCTaylor:passwords")
          updated-user (query/get-registered-user-by-id {:id user-id})]
      (is (= 200 (:status response)))
      (is (= true (hashers/check "newPasss" (:password updated-user)))))))

(deftest can-not-modify-a-user-with-valid-token-and-no-admin-permissions
  (testing "Can not modify a user with valid token and no admin permissions"
    (let [user-id (helper/get-id-for-user "Everyman")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:email "bad@mail.com"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))
          non-updated-user (query/get-registered-user-by-id {:id user-id})]
      (is (= 401 (:status response)))
      (is (= "e@man.com" (str (:email non-updated-user))))
      (is (= "Not authorized" (:error body))))))

(deftest trying-to-modify-a-user-that-does-not-exist-return-a-404
  (testing "Trying to modify a user that does not exist returns a 404"
    (helper/add-permission-for-username "JarrodCTaylor" "admin")
    (let [user-id (gen/generate (s/gen ::specs/id))
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:email "not@real.com"} "JarrodCTaylor:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Userid does not exist" (:error body))))))
