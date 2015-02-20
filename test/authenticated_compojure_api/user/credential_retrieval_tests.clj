(ns authenticated-compojure-api.user.credential-retrieval-tests
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :as helper]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [cheshire.core :as ch]
            [ring.mock.request :as mock]))

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

(deftest valid-username-and-password-return-correct-auth-credentials
  (testing "Valid username and password return correct auth credentials"
    (let [response       (app (-> (mock/request :get "/api/user/token")
                                  (helper/basic-auth-header "Everyman:pass")))
          body           (helper/parse-body (:body response))
          token-contents (bs/loads (:token body) (env :auth-key))]
      (is (= 200         (:status response)))
      (is (= "Everyman"  (:username body)))
      (is (= 36          (count (:refresh_token body))))
      (is (= 4           (count token-contents)))
      (is (= "basic"     (:permissions token-contents)))
      (is (= 1           (:id token-contents)))
      (is (= "e@man.com" (:email token-contents)))
      (is (= "Everyman"  (:username token-contents))))))

(deftest mutiple-permissions-are-properly-formated
  (testing "Multiple permissions are properly formated"
    (query/insert-permission<! {:permission "admin"})
    (query/insert-permission-for-user<! {:userid 2 :permission "admin"})
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (helper/basic-auth-header "JarrodCTaylor:pass")))
          body     (helper/parse-body (:body response))]
      (is (= 200             (:status response)))
      (is (= "JarrodCTaylor" (:username body)))
      (is (= 36              (count (:refresh_token body))))
      (is (= "basic,admin"   (:permissions (bs/loads (:token body) (env :auth-key))))))))

(deftest invalid-username-and-password-do-not-return-auth-credentials
  (testing "Invalid username and password do not return auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (helper/basic-auth-header "JarrodCTaylor:badpass")))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body))))))

(deftest no-auth-credentials-are-returned-when-no-username-and-password-provided
  (testing "No auth credentials are returned when no username and password provided"
    (let [response (app (mock/request :get "/api/user/token"))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body))))))

(deftest user-can-generate-a-new-token-with-a-valid-refresh-token
  (testing "User can generate a new token with a valid refresh-token"
    (let [initial-response   (app (-> (mock/request :get "/api/user/token")
                                      (helper/basic-auth-header "JarrodCTaylor:pass")))
          initial-body       (helper/parse-body (:body initial-response))
          refresh-token      (:refresh_token initial-body)
          refresh-token-json (ch/generate-string {:refresh-token refresh-token})
          refreshed-response (app (-> (mock/request :post "/api/user/token/refresh" refresh-token-json)
                                      (mock/content-type "application/json")))]
      (is (= 200 (:status refreshed-response))))))

(deftest invalid-refresh-token-does-not-return-a-new-token
  (testing "Invalid refresh token does not return a new token"
    (let [bad-token-json (ch/generate-string {:refresh-token "abcd1234"})
          response       (app (-> (mock/request :post "/api/user/token/refresh" bad-token-json)
                                  (mock/content-type "application/json")))
          body           (helper/parse-body (:body response))]
      (is (= 400           (:status response)))
      (is (= "Bad Request" (:error body))))))
