(ns {{ns-name}}.refresh-token.refresh-token-deletion-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [taoensso.timbre :as timbre]
    [mount.core :as mount]
    [ring.mock.request :as mock]
    [{{ns-name}}.handler :refer [app]]
    [{{ns-name}}.specs :as specs]
    [{{ns-name}}.test-utils :as helper]
    [{{ns-name}}.query-defs :as query]))

(use-fixtures :once (fn [f]
                      (try
                        (timbre/merge-config! {:level :warn})
                        (mount/start)
                        (query/insert-permission! {:permission "basic"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database!)))))

(deftest testing-refresh-token-deletion

  (testing "Can delete refresh token with valid refresh token"
    (let [user-id (:id (query/get-registered-user-by-username {:username "JarrodCTaylor"}))
          initial-response (helper/basic-auth-get-request "/api/v1/auth" "JarrodCTaylor:passwords")
          initial-body (helper/parse-body (:body initial-response))
          refresh-token (:refresh-token initial-body)
          refresh-delete-response (app (mock/request :delete (str "/api/v1/refresh-token/" refresh-token)))
          body (helper/parse-body (:body refresh-delete-response))
          registered-user-row (query/get-registered-user-by-id {:id user-id})]
      (is (= 200 (:status refresh-delete-response)))
      (is (= "Refresh token successfully deleted" (:message body)))
      (is (= nil (:refresh_token registered-user-row)))))

  (testing "Attempting to delete an invalid refresh token returns an error"
    (let [refresh-delete-response (app (mock/request :delete (str "/api/v1/refresh-token/" (gen/generate (s/gen ::specs/refresh-token)))))
          body (helper/parse-body (:body refresh-delete-response))]
      (is (= 404 (:status refresh-delete-response)))
      (is (= "The refresh token does not exist" (:error body))))))
