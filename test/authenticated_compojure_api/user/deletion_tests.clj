(ns authenticated-compojure-api.user.deletion-tests
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
    (add-users)
    (f)
    (finally
      (query/drop-user-permission-table!)
      (query/drop-permission-table!)
      (query/drop-registered-user-table!))))

(use-fixtures :each setup-teardown)

(deftest can-delete-user-who-is-not-self-and-associated-permissions-with-valid-token-and-admin-permissions
  (testing "Can delete user who is not self and associated permissions with valid token and admin permissions"
    (is (= 2 (count (query/all-registered-users))))
    (is (= 1 (count (query/get-permissions-for-userid {:userid 1}))))
    (query/insert-permission<! {:permission "admin"})
    (query/insert-permission-for-user<! {:userid 2 :permission "admin"})
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "JarrodCTaylor:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :delete "/api/user/1")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 200                              (:status response)))
      (is (= "User id 1 successfully removed" (:message body)))
      (is (= 1 (count (query/all-registered-users))))
      (is (= 0 (count (query/get-permissions-for-userid {:userid 1})))))))

(deftest can-delete-self-and-associated-permissions-with-valid-token-and-basic-permissions
  (testing "Can delete self and associated permissions with valid token and basic permissions"
    (is (= 2 (count (query/all-registered-users))))
    (is (= 1 (count (query/get-permissions-for-userid {:userid 1}))))
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "Everyman:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :delete "/api/user/1")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 200                              (:status response)))
      (is (= "User id 1 successfully removed" (:message body)))
      (is (= 1 (count (query/all-registered-users))))
      (is (= 0 (count (query/get-permissions-for-userid {:userid 1})))))))

(deftest can-not-delete-user-who-is-not-self-with-valid-token-and-basic-permissions
  (testing "Can not delete user who is not self with valid token and basic permissions"
    (is (= 2 (count (query/all-registered-users))))
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "Everyman:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :delete "/api/user/2")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= 2                (count (query/all-registered-users)))))))

(deftest return-404-when-trying-to-delete-a-user-that-does-not-exists
  (testing "Return 404 when trying to delete a user that does not exists"
    (query/insert-permission<! {:permission "admin"})
    (query/insert-permission-for-user<! {:userid 2 :permission "admin"})
    (let [initial-response (app (-> (mock/request :get "/api/user/token")
                                    (basic-auth-header "JarrodCTaylor:pass")))
          initial-body     (parse-body (:body initial-response))
          the-token        (:token initial-body)
          response         (app (-> (mock/request :delete "/api/user/99")
                                    (token-auth-header the-token)))
          body             (parse-body (:body response))]
      (is (= 404                    (:status response)))
      (is (= "Userid does not exist" (:error body))))))
