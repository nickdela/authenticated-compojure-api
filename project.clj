(defproject authenticated-compojure-api "0.1.0-SNAPSHOT"
  :description "An example compojure-api app with authentication using buddy"

  :dependencies [[org.clojure/clojure        "1.7.0"]
                 [metosin/compojure-api      "0.22.2"]
                 [metosin/ring-http-response "0.6.3"]
                 [metosin/ring-swagger-ui    "2.1.1"]
                 [cheshire                   "5.5.0"]
                 [http-kit                   "2.1.19"]
                 [buddy                      "0.6.1"]
                 [org.clojure/java.jdbc      "0.4.1"]
                 [postgresql/postgresql      "9.3-1102.jdbc41"]
                 [yesql                      "0.5.0"]
                 [environ                    "1.0.0"]
                 [clj-time                   "0.11.0"]
                 [com.draines/postal         "1.11.3"]]

  :plugins      [[lein-environ "1.0.0"]]

  :min-lein-version  "2.5.0"

  :uberjar-name "server.jar"

  :profiles {:uberjar {:resource-paths ["swagger-ui"]
                       :aot :all}

             :test-local   {:dependencies [[javax.servlet/servlet-api "2.5"]
                                           [ring-mock                 "0.1.5"]]}

             ;; Set these in ./profiles.clj
             :test-env-vars {}
             :dev-env-vars  {}

             :test       [:test-local :test-env-vars]
             :dev        [:dev-env-vars :test-local]
             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}}

  :test-selectors {:default (constantly true)
                   :wip     :wip})
