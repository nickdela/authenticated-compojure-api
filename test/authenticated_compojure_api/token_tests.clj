(ns authenticated-compojure-api.token-tests
  (:require [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body basic-auth-header]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(def basic-user {:access "User"  :username "Everyman"      :password "password2" :refresh_token "1HN05Az5P0zUhDDRzdcg"})
(def admin-user {:access "Admin" :username "JarrodCTaylor" :password "password1" :refresh_token "zeRqCTZLoNR8j0irosN9"})
;; Why you no work here?
; (defn add-users []
;   (map query/insert-user<! [admin-user basic-user]))
(defn add-users []
  (query/insert-user<! {:access "Admin" :username "JarrodCTaylor" :password "password1" :refresh_token "zeRqCTZLoNR8j0irosN9"})
  (query/insert-user<! {:access "User"  :username "Everyman"      :password "password2" :refresh_token "1HN05Az5P0zUhDDRzdcg"}))

(facts "Token api tests"
  (with-state-changes [(before :facts (do
                                        (query/create-users-table-if-not-exists!)
                                        (add-users)))
                       (after  :facts (query/drop-users-table!))]

  (fact "Test valid username and password return correct auth credentials"
    #_(query/insert-user<! {:access "Admin" :username "JarrodCTaylor" :password "password1" :refresh_token "zeRqCTZLoNR8j0irosN9"})
    (let [response (app (-> (mock/request :get "/api/token")
                            (basic-auth-header "JarrodCTaylor:password1")))
          body     (parse-body (:body response))]
      (:status response) => 200
      (:id body)         => 1))

  (fact "Test invalid username and password do not return auth credentials"
    (let [response (app (-> (mock/request :get "/api/token")
                            (basic-auth-header "JarrodCTaylor:badpass")))
          body     (parse-body (:body response))]
      (:status response) => 401
      (:error body)      => "Not authorized."))

  (fact "Test no auth credentials are returned when no username and password provided"
    (let [response (app (mock/request :get "/api/token"))
          body     (parse-body (:body response))]
      (:status response) => 401
      (:error body)      => "Not authorized."))

  (fact "Test user can generate a new token with a valid refresh-token"
    (let [initial-response   (app (-> (mock/request :get "/api/token")
                                      (basic-auth-header "JarrodCTaylor:password1")))
          initial-body       (parse-body (:body initial-response))
          refresh_token      (:refresh_token initial-body)
          refreshed-response (app (mock/request :get (str "/api/token/refresh/" refresh_token)))]
      (:status refreshed-response) => 200))

  (fact "Test invalid refresh token does not return a new token"
    (let [response (app (mock/request :get (str "/api/token/refresh/" "abcd1234")))
          body     (parse-body (:body response))]
      (:status response) => 400
      (:error body)      => "Bad Request"))))
