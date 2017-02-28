(ns {{ns-name}}.refresh-token.generating-refresh-token-tests
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [{{ns-name}}.handler :refer :all]
            [{{ns-name}}.test-utils :as helper]
            [{{ns-name}}.queries.query-defs :as query]
            [buddy.sign.jwt :as jwt]
            [ring.mock.request :as mock]
            [clj-time.coerce :as c]))

(defn setup-teardown [f]
  (try
    (query/insert-permission! query/db {:permission "basic"})
    (helper/add-users)
    (f)
    (finally (query/truncate-all-tables-in-database! query/db))))

(use-fixtures :once helper/create-tables)
(use-fixtures :each setup-teardown)

(deftest ^:wip user-can-generate-a-new-token-with-a-valid-refresh-token
  (testing "User can generate a new tokens with a valid refresh-token"
    (let [initial-response   (app (-> (mock/request :get "/api/v1/auth")
                                      (helper/basic-auth-header "JarrodCTaylor:pass")))
          initial-body       (helper/parse-body (:body initial-response))
          id                 (:id initial-body)
          refresh-token      (:refreshToken initial-body)
          original-token     (query/get-refresh-token query/db {:refresh_token refresh-token})
          refreshed-response (app (mock/request :get (str "/api/v1/refresh-token/" refresh-token)))
          body               (helper/parse-body (:body refreshed-response))
          token-contents     (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})
          new-token          (query/get-refresh-token query/db {:refresh_token (:refreshToken body)})]
      (is (= 200              (:status refreshed-response)))
      (is (= 2                (count body)))
      (is (= true             (contains? body :token)))
      (is (= true             (contains? body :refreshToken)))
      (is (not= refresh-token (:refreshToken body)))
      (is (= 5                (count        token-contents)))
      (is (= "basic"          (:permissions token-contents)))
      (is (= id               (:id          token-contents)))
      (is (= "j@man.com"      (:email       token-contents)))
      (is (= "JarrodCTaylor"  (:username    token-contents)))
      (is (number?            (:exp         token-contents)))
      (is  (< (c/to-long (:created_on original-token))
              (c/to-long (:created_on new-token)))))))

(deftest user-can-generate-multiple-refresh-tokens
  (testing "User can generate multiple refresh-tokens simultaneously"
    (let [first-response        (app (-> (mock/request :get "/api/v1/auth")
                                         (helper/basic-auth-header "JarrodCTaylor:pass")))
          first-body            (helper/parse-body (:body first-response))
          first-id              (:id first-body)
          first-refresh-token   (:refreshToken first-body)
          first-token-contents  (jwt/unsign (:token first-body) (env :auth-key) {:alg :hs512})
          second-response       (app (-> (mock/request :get "/api/v1/auth")
                                         (helper/basic-auth-header "JarrodCTaylor:pass")))
          second-body           (helper/parse-body (:body second-response))
          second-id             (:id second-body)
          second-refresh-token  (:refreshToken second-body)
          second-token-contents (jwt/unsign (:token second-body) (env :auth-key) {:alg :hs512})]
      (is (= 200              (:status first-response)))
      (is (= 200              (:status second-response)))
      (is (not= (:refreshToken first-body) (:refreshToken second-body)))
      (is (= first-id         (:id    first-token-contents)))
      (is (= second-id        (:id    second-token-contents)))
      (is (= "JarrodCTaylor"  (:username first-token-contents)))
      (is (= "JarrodCTaylor"  (:username second-token-contents))))))

(deftest invalid-refresh-token-does-not-return-a-new-token
  (testing "Invalid refresh token does not return a new token"
    (let [response       (app (mock/request :get (str "/api/v1/refresh-token/" (java.util.UUID/randomUUID))))
          body           (helper/parse-body (:body response))]
      (is (= 400           (:status response)))
      (is (= "Bad Request" (:error body))))))
