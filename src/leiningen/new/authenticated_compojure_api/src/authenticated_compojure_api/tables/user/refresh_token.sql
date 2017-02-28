-- :name create-refresh-token-table-if-not-exists!
-- :command :execute
-- :result :affected
-- :doc Create the refresh_token table if it does not exist
CREATE TABLE IF NOT EXISTS refresh_token (
   id              UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4()
   , created_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   , user_id       UUID      REFERENCES registered_user (id) ON DELETE CASCADE
   , refresh_token UUID UNIQUE
);

-- :name drop-refresh-token-table!
-- :command :execute
-- :result :affected
-- :doc Drop the refresh_token table
DROP TABLE refresh_token;
