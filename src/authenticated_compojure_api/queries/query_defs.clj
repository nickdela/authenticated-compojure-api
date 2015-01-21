(ns authenticated-compojure-api.queries.query-defs
  (:require [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(defqueries "authenticated_compojure_api/queries/quotes.sql" {:connection (env :database-url)})
(defqueries "authenticated_compojure_api/queries/user.sql" {:connection (env :database-url)})
