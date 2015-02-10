(ns authenticated-compojure-api.user.user-permission-creation-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body basic-auth-header token-auth-header]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(def basic-user {:email "e@man.com" :username "Everyman"      :password "pass"})
(def admin-user {:email "J@man.com" :username "JarrodCTaylor" :password "pass"})

(defn add-users []
  (app (-> (mock/request :post "/api/user" (ch/generate-string basic-user))
           (mock/content-type "application/json")))
  (app (-> (mock/request :post "/api/user" (ch/generate-string admin-user))
           (mock/content-type "application/json"))))

(defn setup-teardown [f]
  (try
    (query/create-registered-user-table-if-not-exists!)
    (query/create-permission-table-if-not-exists!)
    (query/create-user-permission-table-if-not-exists!)
    (query/insert-permission<! {:permission "basic"})
    (query/insert-permission<! {:permission "admin"})
    (query/insert-permission<! {:permission "other"})
    (add-users)
    (f)
    (finally
      (query/drop-user-permission-table!)
      (query/drop-permission-table!)
      (query/drop-registered-user-table!))))

(use-fixtures :each setup-teardown)

(deftest can-add-user-permission-with-valid-token-and-admin-permissions
  (testing "Can add user permission with valid token and admin permissions"
    (query/insert-permission-for-user<! {:userid 2 :permission "admin"})
    (is (= "basic" (:permissions (first (query/get-permissions-for-userid {:userid 1})))))
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "JarrodCTaylor:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :post "/api/user/1/permission/other")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 200           (:status response)))
      (is (= "Permission 'other' for user 1 successfully added" (:message body)))
      (is (= "basic,other" (:permissions (first (query/get-permissions-for-userid {:userid 1}))))))))

(deftest attempting-to-add-a-permission-that-does-not-exist-returns-404
  (testing "Attempting to add a permission that does not exist returns 404"
    (query/insert-permission-for-user<! {:userid 2 :permission "admin"})
    (is (= "basic" (:permissions (first (query/get-permissions-for-userid {:userid 1})))))
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "JarrodCTaylor:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :post "/api/user/1/permission/stranger")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 404           (:status response)))
      (is (= "Permission 'stranger' does not exist" (:error body)))
      (is (= "basic" (:permissions (first (query/get-permissions-for-userid {:userid 1}))))))))

(deftest can-not-add-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not add user permission with valid token and no admin permissions"
    (is (= "basic" (:permissions (first (query/get-permissions-for-userid {:userid 2})))))
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "Everyman:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :post "/api/user/2/permission/other")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 401                       (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic" (:permissions (first (query/get-permissions-for-userid {:userid 2}))))))))
