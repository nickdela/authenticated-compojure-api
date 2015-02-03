(ns authenticated-compojure-api.queries.query-defs
  (:require [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(def db-connection {:connection (env :database-url)})

(defqueries "authenticated_compojure_api/queries/quotes.sql" db-connection)
(defqueries "authenticated_compojure_api/queries/user.sql" db-connection)
(defqueries "authenticated_compojure_api/queries/permission.sql" db-connection)
(defqueries "authenticated_compojure_api/queries/user_permission.sql" db-connection)
