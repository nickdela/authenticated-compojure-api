(ns authenticated-compojure-api.queries.query-defs
  (:require [hugsql.core :as hugsql]
            [environ.core :refer [env]]))

(def db (env :database-url))

(hugsql/def-db-fns "authenticated_compojure_api/queries/user/password_reset_key.sql")
(hugsql/def-db-fns "authenticated_compojure_api/tables/user/registered_user.sql")
(hugsql/def-db-fns "authenticated_compojure_api/tables/user/permission.sql")
(hugsql/def-db-fns "authenticated_compojure_api/tables/user/user_permission.sql")
(hugsql/def-db-fns "authenticated_compojure_api/tables/user/password_reset_key.sql")
(hugsql/def-db-fns "authenticated_compojure_api/tables/user/truncate_all.sql")
(hugsql/def-db-fns "authenticated_compojure_api/queries/user/registered_user.sql")
(hugsql/def-db-fns "authenticated_compojure_api/queries/user/permission.sql")
(hugsql/def-db-fns "authenticated_compojure_api/queries/user/user_permission.sql")
