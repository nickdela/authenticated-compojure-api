(defproject {{ns-name}} "0.1.0-SNAPSHOT"
  :description "An example compojure-api app with authentication using buddy"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [metosin/compojure-api      "1.1.8"]
                 [metosin/ring-http-response "0.8.0"]
                 [metosin/ring-swagger-ui    "2.2.2-0"]
                 [cheshire                   "5.6.3"]
                 [http-kit                   "2.2.0"]
                 [buddy                      "1.1.0"]
                 [org.clojure/java.jdbc      "0.6.1"]
                 [postgresql/postgresql      "9.3-1102.jdbc41"]
                 [com.layerware/hugsql       "0.4.7"]
                 [environ                    "1.1.0"]
                 [clj-time                   "0.12.0"]
                 [com.draines/postal         "2.0.1"]]

  :plugins      [[lein-environ "1.1.0"]
                 [funcool/codeina "0.5.0" :exclusions [org.clojure/clojure]]]

  :min-lein-version  "2.5.0"

  :uberjar-name "server.jar"

  :codeina {:sources ["src"]
            :reader :clojure}

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
