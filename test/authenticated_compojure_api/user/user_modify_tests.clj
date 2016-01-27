(ns authenticated-compojure-api.user.user-modify-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer [app]]
            [authenticated-compojure-api.test-utils :as helper]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.hashers :as hashers]
            [cheshire.core :as ch]
            [ring.mock.request :as mock]))

(defn setup-teardown [f]
  (try
    (query/insert-permission<! {:permission "basic"})
    (query/insert-permission<! {:permission "admin"})
    (helper/add-users)
    (f)
    (finally (query/truncate-all-tables-in-database!))))

(use-fixtures :once helper/create-tables)
(use-fixtures :each setup-teardown)

(deftest can-modify-a-users-username-with-valid-token-and-admin-permissions
  (testing "Can modify a users username with valid token and admin permissions"
    (let [user-id-1  (:id (first (query/get-registered-user-by-username {:username "JarrodCTaylor"})))
          _          (query/insert-permission-for-user<! {:userid user-id-1 :permission "admin"})
          response   (app (-> (mock/request :patch (str "/api/user/" user-id-1) (ch/generate-string {:username "Newman"}))
                              (mock/content-type "application/json")
                              (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body       (helper/parse-body (:body response))]
      (is (= 200         (:status response)))
      (is (= "Newman"    (:username body)))
      (is (= "j@man.com" (:email body))))))

(deftest can-modify-a-users-email-with-valid-token-and-admin-permissions
  (testing "Can modify a users email with valid token and admin permissions"
    (let [user-id-1    (:id (first (query/get-registered-user-by-username {:username "JarrodCTaylor"})))
          _            (query/insert-permission-for-user<! {:userid user-id-1 :permission "admin"})
          response     (app (-> (mock/request :patch (str "/api/user/" user-id-1) (ch/generate-string {:email "new@email.com"}))
                                (mock/content-type "application/json")
                                (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body         (helper/parse-body (:body response))
          updated-user (first (query/get-registered-user-by-id {:id user-id-1}))]
      (is (= 200             (:status response)))
      (is (= "JarrodCTaylor"      (:username body)))
      (is (= "new@email.com" (:email body)))
      (is (= "new@email.com" (str (:email updated-user)))))))

(deftest can-modify-a-users-password-with-valid-token-and-admin-permissions
  (testing "Can modify a users password with valid token and admin permissions"
    (let [user-id-1    (:id (first (query/get-registered-user-by-username {:username "JarrodCTaylor"})))
                       _ (query/insert-permission-for-user<! {:userid user-id-1 :permission "admin"})
          response     (app (-> (mock/request :patch (str "/api/user/" user-id-1) (ch/generate-string {:password "newPass"}))
                                (mock/content-type "application/json")
                                (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body         (helper/parse-body (:body response))
          updated-user (first (query/get-registered-user-by-id {:id user-id-1}))]
      (is (= 200  (:status response)))
      (is (= true (hashers/check "newPass" (:password updated-user)))))))

(deftest can-modify-your-own-password-with-valid-token-and-no-admin-permissions
  (testing "Can modify your own password with valid token and no admin permissions"
    (let [user-id-1    (:id (first (query/get-registered-user-by-username {:username "JarrodCTaylor"})))
          response     (app (-> (mock/request :patch (str "/api/user/" user-id-1) (ch/generate-string {:password "newPass"}))
                                (mock/content-type "application/json")
                                (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body         (helper/parse-body (:body response))
          updated-user (first (query/get-registered-user-by-id {:id user-id-1}))]
      (is (= 200  (:status response)))
      (is (= true (hashers/check "newPass" (:password updated-user)))))))

(deftest can-not-modify-a-user-with-valid-token-and-no-admin-permissions
  (testing "Can not modify a user with valid token and no admin permissions"
    (let [user-id-2        (:id (first (query/get-registered-user-by-username {:username "Everyman"})))
          response         (app (-> (mock/request :patch (str "/api/user/" user-id-2) (ch/generate-string {:email "bad@mail.com"}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body             (helper/parse-body (:body response))
          non-updated-user (first (query/get-registered-user-by-id {:id user-id-2}))]
      (is (= 401              (:status response)))
      (is (= "e@man.com"      (str (:email non-updated-user))))
      (is (= "Not authorized" (:error body))))))

(deftest trying-to-modify-a-user-that-does-not-exist-return-a-404
  (testing "Trying to modify a user that does not exist returns a 404"
    (let [user-id-1  (:id (first (query/get-registered-user-by-username {:username "JarrodCTaylor"})))
          _          (query/insert-permission-for-user<! {:userid user-id-1 :permission "admin"})
          response   (app (-> (mock/request :patch "/api/user/99" (ch/generate-string {:email "not@real.com"}))
                              (mock/content-type "application/json")
                              (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body       (helper/parse-body (:body response))]
      (is (= 404                     (:status response)))
      (is (= "Userid does not exist" (:error body))))))
