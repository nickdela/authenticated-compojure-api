(ns {{ns-name}}.server
  (:require
    [org.httpkit.server :as httpkit]
    [taoensso.timbre :as timbre]
    [mount.core :as mount]
    [{{ns-name}}.handler :refer [app]]))

(defn -main [port]
  (httpkit/run-server app {:port (Integer/parseInt port) :join false})
  (timbre/merge-config! {:level :warn})
  (mount/start)
  (println "server started on port:" port))
