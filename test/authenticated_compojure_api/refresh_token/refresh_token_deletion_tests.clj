(ns authenticated-compojure-api.refresh-token.refresh-token-deletion-tests
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

(deftest can-delete-refresh-token-with-valid-refresh-token
  (testing "Can delete refresh token with valid refresh token"
    (let [user-id-1                (:id (query/get-registered-user-by-username query/db {:username "JarrodCTaylor"}))
          initial-response         (app (-> (mock/request :get "/api/v1/auth")
                                            (helper/basic-auth-header "JarrodCTaylor:pass")))
          initial-body             (helper/parse-body (:body initial-response))
          refresh-token            (:refreshToken initial-body)
          refresh-delete-response  (app (mock/request :delete (str "/api/v1/refresh-token/" refresh-token)))
          body                     (helper/parse-body (:body refresh-delete-response))
          registered-user-row      (query/get-registered-user-by-id query/db {:id user-id-1})]
      (is (= 200 (:status refresh-delete-response)))
      (is (= "Refresh token successfully deleted" (:message body)))
      (is (= nil (:refresh_token registered-user-row))))))

(deftest attempting-to-delete-an-invalid-refresh-token-returns-an-error
  (testing "Attempting to delete an invalid refresh token returns an error"
    (let [refresh-delete-response  (app (mock/request :delete (str "/api/v1/refresh-token/" "123abc")))
          body                     (helper/parse-body (:body refresh-delete-response))]
      (is (= 404 (:status refresh-delete-response)))
      (is (= "The refresh token does not exist" (:error body))))))
