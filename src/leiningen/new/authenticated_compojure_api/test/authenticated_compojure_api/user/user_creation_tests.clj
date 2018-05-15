(ns {{ns-name}}.user.user-creation-tests
  (:import
    [java.time Instant]
    [java.time.temporal ChronoUnit])
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [ring.mock.request :as mock]
    [taoensso.timbre :as timbre]
    [mount.core :as mount]
    [cheshire.core :as ch]
    [{{ns-name}}.specs :as specs]
    [{{ns-name}}.handler :refer [app]]
    [{{ns-name}}.test-utils :refer [parse-body]]
    [{{ns-name}}.query-defs :as query]))

(defn create-user [user-map]
  (app (-> (mock/request :post "/api/v1/user")
           (mock/content-type "application/json")
           (mock/body (ch/generate-string user-map)))))

(defn assert-no-dup [user-1 user-2 expected-error-message]
  (let [_ (create-user user-1)
        response (create-user user-2)
        body (parse-body (:body response))]
    (is (= 409 (:status response)))
    (is (= 1 (count (query/all-registered-users))))
    (is (= expected-error-message (:error body)))))

(use-fixtures :once (fn [f]
                      (timbre/merge-config! {:level :warn})
                      (mount/start)
                      (f)))

(use-fixtures :each (fn [f]
                      (try
                        (query/insert-permission! {:permission "basic"})
                        (f)
                        (finally (query/truncate-all-tables-in-database!)))))

(deftest can-successfully-create-a-new-user-who-is-given-basic-permission-as-default
  (testing "Can successfully create a new user who is given basic permission as default"
    (is (= 0 (count (query/all-registered-users))))
    (let [rand-user (gen/generate (s/gen ::specs/register-request))
          response (create-user rand-user)
          body (parse-body (:body response))
          new-registered-user (query/get-registered-user-details-by-username {:username (:username body)})
          registered-at (.truncatedTo (.toInstant (:created_on new-registered-user)) ChronoUnit/SECONDS)
          expected-time (.truncatedTo (Instant/now) ChronoUnit/SECONDS)]
      (is (= 201 (:status response)))
      (is (= 1 (count (query/all-registered-users))))
      (is (= (:username rand-user) (:username body)))
      (is (= (:username rand-user) (str (:username new-registered-user))))
      (is (= expected-time registered-at))
      (is (= "basic" (:permissions new-registered-user))))))

(deftest can-not-create-a-user-if-username-already-exists-using-the-same-case
  (testing "Can not create a user if username already exists using the same case"
    (assert-no-dup {:email "Jrock@Taylor.com"   :username "Jarrod" :password "pass5678"}
                   {:email "Jam@Master.com"     :username "Jarrod" :password "pass5678"}
                   "Username already exists")))

(deftest can-not-create-a-user-if-username-already-exists-using-mixed-case
  (testing "Can not create a user if username already exists using mixed case"
    (assert-no-dup {:email "Jrock@Taylor.com"   :username "jarrod" :password "pass5678"}
                   {:email "Jam@Master.com"     :username "Jarrod" :password "pass5678"}
                   "Username already exists")))

(deftest can-not-create-a-user-if-email-already-exists-using-the-same-case
  (testing "Can not create a user if email already exists using the same case"
    (assert-no-dup {:email "jarrod@taylor.com" :username "Jarrod"   :password "the-first-pass"}
                   {:email "jarrod@taylor.com" :username "JarrodCT" :password "the-second-pass"}
                   "Email already exists")))

(deftest can-not-create-a-user-if-email-already-exists-using-mixed-case
  (testing "Can not create a user if email already exists using mixed case"
    (assert-no-dup {:email "wOnkY@email.com" :username "Jarrod" :password "Pass5678"}
                   {:email "WonKy@email.com" :username "Jrock"  :password "Pass5678"}
                   "Email already exists")))

(deftest can-not-create-a-user-if-username-and-email-already-exist-using-same-and-mixed-case
  (testing "Can not create a user if username and email already exist using same and mixed case"
    (assert-no-dup {:email "wOnkY@email.com" :username "jarrod" :password "pass5678"}
                   {:email "WonKy@email.com" :username "jarrod" :password "pass5678"}
                   "Username and Email already exist")))
