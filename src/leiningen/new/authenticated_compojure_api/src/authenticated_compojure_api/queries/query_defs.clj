(ns {{ns-name}}.queries.query-defs
  (:require [hugsql.core :as hugsql]
            [environ.core :refer [env]]))

(def db (env :database-url))

(hugsql/def-db-fns "{{sanitized}}/queries/user/password_reset_key.sql")
(hugsql/def-db-fns "{{sanitized}}/tables/user/registered_user.sql")
(hugsql/def-db-fns "{{sanitized}}/tables/user/permission.sql")
(hugsql/def-db-fns "{{sanitized}}/tables/user/user_permission.sql")
(hugsql/def-db-fns "{{sanitized}}/tables/user/password_reset_key.sql")
(hugsql/def-db-fns "{{sanitized}}/tables/user/truncate_all.sql")
(hugsql/def-db-fns "{{sanitized}}/tables/user/refresh_token.sql")
(hugsql/def-db-fns "{{sanitized}}/queries/user/registered_user.sql")
(hugsql/def-db-fns "{{sanitized}}/queries/user/permission.sql")
(hugsql/def-db-fns "{{sanitized}}/queries/user/user_permission.sql")
(hugsql/def-db-fns "{{sanitized}}/queries/user/refresh_token.sql")
