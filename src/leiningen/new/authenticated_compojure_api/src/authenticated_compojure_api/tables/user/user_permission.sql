-- :name create-user-permission-table-if-not-exists!
-- :command :execute
-- :result :affected
-- :doc Create the user_permission table if it does not exist
CREATE TABLE IF NOT EXISTS user_permission (
    id           SERIAL  PRIMARY KEY
    , user_id    UUID    REFERENCES registered_user (id)    ON DELETE CASCADE
    , permission TEXT    REFERENCES permission (permission) ON DELETE CASCADE);

-- :name drop-user-permission-table!
-- :command :execute
-- :result :affected
-- :doc Drop the user_permission table
DROP TABLE user_permission CASCADE;
