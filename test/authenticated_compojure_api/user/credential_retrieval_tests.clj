(ns authenticated-compojure-api.user.credential-retrieval-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body basic-auth-header]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [cheshire.core :as ch]
            [ring.mock.request :as mock]))

(def basic-user {:email "e@man.com"         :username "Everyman"      :password "password2"})
(def admin-user {:email "Jarrod@Taylor.com" :username "JarrodCTaylor" :password "password1"})

(defn add-users []
  (app (-> (mock/request :post "/api/user" (ch/generate-string basic-user))
                                           (mock/content-type "application/json")))
  (app (-> (mock/request :post "/api/user" (ch/generate-string admin-user))
                                           (mock/content-type "application/json"))))

(defn setup-teardown [f]
  (query/create-registered-user-table-if-not-exists!)
  (query/create-permission-table-if-not-exists!)
  (query/create-user-permission-table-if-not-exists!)
  (query/insert-permission<! {:permission "basic"})
  (add-users)
  (f)
  (query/drop-user-permission-table!)
  (query/drop-permission-table!)
  (query/drop-registered-user-table!))

(use-fixtures :each setup-teardown)

(deftest user-credential-retrieval-tests

  (testing "Valid username and password return correct auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (basic-auth-header "Everyman:password2")))
          body     (parse-body (:body response))]
      (is (= 200        (:status response)))
      (is (= "Everyman" (:username body)))
      (is (= 36         (count (:refresh_token body))))
      (is (= ["basic"]  (:permissions (bs/loads (:token body) auth-key))))))

  (testing "Invalid username and password do not return auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (basic-auth-header "JarrodCTaylor:badpass")))
          body     (parse-body (:body response))]
      (is (= 401               (:status response)))
      (is (= "Not authorized." (:error body)))))

  (testing "No auth credentials are returned when no username and password provided"
    (let [response (app (mock/request :get "/api/user/token"))
          body     (parse-body (:body response))]
      (is (= 401               (:status response)))
      (is (= "Not authorized." (:error body)))))

  (testing "User can generate a new token with a valid refresh-token"
    (let [initial-response   (app (-> (mock/request :get "/api/user/token")
                                      (basic-auth-header "JarrodCTaylor:password1")))
          initial-body       (parse-body (:body initial-response))
          refresh_token      (:refresh_token initial-body)
          refreshed-response (app (-> (mock/request :post "/api/user/token/refresh" (ch/generate-string {:refresh-token refresh_token}))
                                      (mock/content-type "application/json")))]
      (is (= 200 (:status refreshed-response)))))

  (testing "Invalid refresh token does not return a new token"
    (let [response (app (-> (mock/request :post "/api/user/token/refresh" (ch/generate-string {:refresh-token "abcd1234"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (is (= 400           (:status response)))
      (is (= "Bad Request" (:error body))))))
