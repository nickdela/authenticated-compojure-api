-- name: get-password-reset-keys-for-userid
-- get the password reset key(s) for a given userid
SELECT id
       , reset_key
       , already_used
       , user_id
       , valid_until
FROM   password_reset_key
WHERE  user_id = :userid;

-- name: insert-password-reset-key-with-default-valid-until<!
-- inserts a row in the password_reset_key table using the default valid until timestamp
INSERT INTO password_reset_key (reset_key , user_id)
VALUES (:reset_key, :user_id);

-- name: insert-password-reset-key-with-provide-valid-until-date<!
-- inserts a row in the password_reset_key table using the provided valid until timestamp
INSERT INTO password_reset_key (reset_key , user_id, valid_until)
VALUES (:reset_key, :user_id, :valid_until);

-- name: drop-password-reset-key-table!
-- drop the passowrd-reset-key table
DROP TABLE password_reset_key;

-- name: create-password-reset-key-table-if-not-exists!
-- create the password_reset_key table if it does not exist
CREATE TABLE IF NOT EXISTS password_reset_key (
  id              SERIAL    PRIMARY KEY NOT NULL
  , reset_key     TEXT                  NOT NULL UNIQUE
  , already_used  BOOLEAN               NOT NULL DEFAULT FALSE
  , user_id       INTEGER   REFERENCES registered_user (id) ON DELETE CASCADE
  , valid_until   TIMESTAMP WITH TIME ZONE DEFAULT NOW() + INTERVAL '24 hours'
);
