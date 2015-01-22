(ns authenticated-compojure-api.user.token-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body basic-auth-header]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(def basic-user {:access "User"  :username "Everyman"      :password "password2" :refresh_token "1HN05Az5P0zUhDDRzdcg"})
(def admin-user {:access "Admin" :username "JarrodCTaylor" :password "password1" :refresh_token "zeRqCTZLoNR8j0irosN9"})

(defn add-users []
  (query/insert-user<! admin-user)
  (query/insert-user<! basic-user))

(defn setup-teardown [f]
  (query/create-users-table-if-not-exists!)
  (add-users)
  (f)
  (query/drop-users-table!))

(use-fixtures :each setup-teardown)

(deftest user-token-reterival-tests

  (testing "Test valid username and password return correct auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (basic-auth-header "JarrodCTaylor:password1")))
          body     (parse-body (:body response))]
      (is (= (:status response)    200))
      (is (= (:username body)      "JarrodCTaylor"))
      (is (= (:refresh_token body) "zeRqCTZLoNR8j0irosN9"))))

  (testing "Test invalid username and password do not return auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (basic-auth-header "JarrodCTaylor:badpass")))
          body     (parse-body (:body response))]
      (is (= (:status response) 401))
      (is (= (:error body)      "Not authorized."))))

  (testing "Test no auth credentials are returned when no username and password provided"
    (let [response (app (mock/request :get "/api/user/token"))
          body     (parse-body (:body response))]
      (is (= (:status response) 401))
      (is (= (:error body)      "Not authorized."))))

  (testing "Test user can generate a new token with a valid refresh-token"
    (let [initial-response   (app (-> (mock/request :get "/api/user/token")
                                      (basic-auth-header "JarrodCTaylor:password1")))
          initial-body       (parse-body (:body initial-response))
          refresh_token      (:refresh_token initial-body)
          refreshed-response (app (-> (mock/request :post "/api/user/token/refresh" (ch/generate-string {:refresh-token refresh_token}))
                                      (mock/content-type "application/json")))]
      (is (= (:status refreshed-response) 200))))

  (testing "Test invalid refresh token does not return a new token"
    (let [response (app (-> (mock/request :post "/api/user/token/refresh" (ch/generate-string {:refresh-token "abcd1234"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (is (= (:status response) 400))
      (is (= (:error body)      "Bad Request")))))
