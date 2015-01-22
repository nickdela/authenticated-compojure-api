(ns authenticated-compojure-api.user.credential-retrieval-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body basic-auth-header]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(def basic-user {:username "Everyman"      :password "password2"})
(def admin-user {:username "JarrodCTaylor" :password "password1"})

(defn add-users []
  (app (-> (mock/request :post "/api/user" (ch/generate-string basic-user))
                                           (mock/content-type "application/json")))
  (app (-> (mock/request :post "/api/user" (ch/generate-string admin-user))
                                           (mock/content-type "application/json"))))

(defn setup-teardown [f]
  (query/create-users-table-if-not-exists!)
  (add-users)
  (f)
  (query/drop-users-table!))

(use-fixtures :each setup-teardown)

(deftest user-credential-retrieval-tests

  (testing "Valid username and password return correct auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (basic-auth-header "JarrodCTaylor:password1")))
          body     (parse-body (:body response))]
      (is (= (:status response)            200))
      (is (= (:username body)              "JarrodCTaylor"))
      (is (= (count (:refresh_token body)) 36))))

  (testing "Invalid username and password do not return auth credentials"
    (let [response (app (-> (mock/request :get "/api/user/token")
                            (basic-auth-header "JarrodCTaylor:badpass")))
          body     (parse-body (:body response))]
      (is (= (:status response) 401))
      (is (= (:error body)      "Not authorized."))))

  (testing "No auth credentials are returned when no username and password provided"
    (let [response (app (mock/request :get "/api/user/token"))
          body     (parse-body (:body response))]
      (is (= (:status response) 401))
      (is (= (:error body)      "Not authorized."))))

  (testing "User can generate a new token with a valid refresh-token"
    (let [initial-response   (app (-> (mock/request :get "/api/user/token")
                                      (basic-auth-header "JarrodCTaylor:password1")))
          initial-body       (parse-body (:body initial-response))
          refresh_token      (:refresh_token initial-body)
          refreshed-response (app (-> (mock/request :post "/api/user/token/refresh" (ch/generate-string {:refresh-token refresh_token}))
                                      (mock/content-type "application/json")))]
      (is (= (:status refreshed-response) 200))))

  (testing "Invalid refresh token does not return a new token"
    (let [response (app (-> (mock/request :post "/api/user/token/refresh" (ch/generate-string {:refresh-token "abcd1234"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (is (= (:status response) 400))
      (is (= (:error body)      "Bad Request")))))
