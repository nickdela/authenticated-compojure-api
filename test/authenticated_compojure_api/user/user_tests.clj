(ns authenticated-compojure-api.user.user-tests
  (:require [clojure.test :refer :all]
            [authenticated-compojure-api.handler :refer :all]
            [authenticated-compojure-api.test-utils :refer [parse-body]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]))

(defn setup-teardown [f]
  (query/create-users-table-if-not-exists!)
  (f)
  (query/drop-users-table!))

(use-fixtures :each setup-teardown)

(deftest user-tests

  (testing "Can successfully create a new user"
    (let [response (app (-> (mock/request :post "/api/user" (ch/generate-string {:username "Jarrod" :password "the-password"}))
                            (mock/content-type "application/json")))
          body     (parse-body (:body response))]
      (is (= (count (query/all-users)) 1))
      (is (= (:username body) "Jarrod")))))
