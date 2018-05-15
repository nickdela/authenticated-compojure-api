(defproject {{ns-name}} "0.1.0-SNAPSHOT"
  :description "compojure-api with token-based authentication using Buddy."

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [metosin/compojure-api "2.0.0-alpha19"]
                 [metosin/spec-tools "0.6.1"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [metosin/ring-http-response "0.9.0"]
                 [cheshire "5.8.0"]
                 [http-kit "2.3.0"]
                 [buddy "2.0.0"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [postgresql/postgresql "9.3-1102.jdbc41"]
                 [com.layerware/hugsql "0.4.8"]
                 [environ "1.1.0"]
                 [mount "0.1.12"]
                 [com.taoensso/timbre "4.10.0"]
                 [migratus "1.0.6"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]
                 [conman "0.7.8"]
                 [com.draines/postal "2.0.2"]]

  :plugins      [[lein-environ "1.1.0"]
                 [migratus-lein "0.5.2"]
                 [funcool/codeina "0.5.0" :exclusions [org.clojure/clojure]]]

  :min-lein-version  "2.5.0"

  :resource-paths ["resources"]

  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}

  :uberjar-name "server.jar"

  :codeina {:sources ["src"]
            :reader :clojure}

  :profiles {:uberjar {:resource-paths ["swagger-ui"]
                       :aot :all}

             ;; Set these in ./profiles.clj
             :test-env-vars {}
             :dev-env-vars  {}

             :test [:test-env-vars]
             :dev [{:dependencies [[ring/ring-mock "0.3.2"]]}
                    :dev-env-vars]
             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}}

  :test-selectors {:default (constantly true)
                   :wip :wip})
