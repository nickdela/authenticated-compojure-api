(defproject authenticated-compojure-api "0.1.0-SNAPSHOT"
  :description "An example compojure-api app with authentication using buddy"

  :dependencies [[org.clojure/clojure        "1.6.0"]
                 [metosin/compojure-api      "0.16.5"]
                 [metosin/ring-http-response "0.5.2"]
                 [metosin/ring-swagger-ui    "2.0.17"]
                 [cheshire                   "5.3.1"]
                 [buddy                      "0.2.0"]
                 [org.clojure/java.jdbc      "0.3.5"]
                 [postgresql/postgresql      "9.1-901-1.jdbc4"]
                 [yesql                      "0.5.0-beta2"]
                 [environ                    "1.0.0"]]

  :plugins      [[lein-ring    "0.8.13"]
                 [lein-environ "1.0.0"]]

  :ring {:handler authenticated-compojure-api.handler/app
         :init    authenticated-compojure-api.handler/init}

  :min-lein-version  "2.5.0"

  :uberjar-name "server.jar"

  :profiles {:uberjar {:resource-paths ["swagger-ui"]
                       :aot :all}

             :test-local   {:dependencies [[javax.servlet/servlet-api "2.5"]
                                           [ring-mock                 "0.1.5"]
                                           [cheshire                  "5.3.1"]]}

             ;; Set these in ./profiles.clj
             :test-env-vars {}
             :dev-env-vars  {}

             :test       [:test-local :test-env-vars]
             :dev        [:dev-env-vars]
             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}})
