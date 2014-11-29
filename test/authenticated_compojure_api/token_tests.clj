(ns authenticated-compojure-api.token-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated_compojure_api.test_utils :refer [parse-body basic-auth-header]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(facts "Token tests"

  (fact "Test valid username and password return correct auth credentials"
    (let [response (app (-> (mock/request :get "/api/token")
                            (basic-auth-header "JarrodCTaylor:password1")))
          body     (parse-body (:body response))]
      (:status response) => 200
      (:userid body) => 1))

  (fact "Test invalid username and password do not return auth credentials"
    (let [response (app (-> (mock/request :get "/api/token")
                            (basic-auth-header "JarrodCTaylor:badpass")))
          body     (parse-body (:body response))]
      (:status response) => 401
      (:error body) => "Not authorized."))

  (fact "Test no auth credentials are returned when no username and password provided"
    (let [response (app (mock/request :get "/api/token"))
          body     (parse-body (:body response))]
      (:status response) => 401
      (:error body) => "Not authorized.")))
