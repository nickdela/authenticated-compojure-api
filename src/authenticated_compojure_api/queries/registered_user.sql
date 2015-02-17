-- name: all-registered-users
-- Selects all registered-users
SELECT id
       , email
       , username
       , password
       , refresh_token
FROM   registered_user;

-- name: get-registered-user-by-reset-token
-- Selects a user with matching reset-token
SELECT id
       , email
       , username
       , password
       , refresh_token
FROM   registered_user
WHERE  refresh_token = :refresh_token;

-- name: get-registered-user-by-id
-- Selects the (id, email, username, password, refresh_token) for registered user matching the id
SELECT id
       , email
       , username
       , password
       , refresh_token
FROM   registered_user
WHERE  id = :id

-- name: get-registered-user-by-username
-- Selects the (id, email, username) for registered user matching the username
SELECT id
       , email
       , username
FROM   registered_user
WHERE  username = :username

-- name: get-registered-user-by-email
-- Selects the (id, email, username) for registered user matching the email
SELECT id
       , email
       , username
FROM   registered_user
WHERE  email = :email

-- name: get-registered-user-details-by-username
-- Selects user details for matching username
SELECT reg_user.id
       , reg_user.email
       , reg_user.username
       , reg_user.password
       , reg_user.refresh_token
       , STRING_AGG(perm.permission, ',') AS permissions
FROM   registered_user                    AS reg_user
       JOIN user_permission               AS perm
         ON (reg_user.id = perm.user_id)
WHERE  reg_user.username = :username
GROUP  BY reg_user.id;

-- name: insert-registered-user<!
-- inserts a single user
INSERT INTO registered_user (
    email
    , username
    , password
    , refresh_token)
VALUES (
    :email
    , :username
    , :password
    , :refresh_token);

-- name: update-registered-user<!
-- update a single user matching provided id
UPDATE registered_user
SET    email = :email
       , username = :username
       , password = :password
       , refresh_token = :refresh_token
WHERE  id = :id;

-- name: update-registered-user-password<!
-- update the password for the user matching the given userid
UPDATE registered_user
SET    password = :password
WHERE  id = :id;

-- name: delete-registered-user!
-- delete a single user matching provided id
DELETE FROM registered_user
WHERE       id = :id;

-- name: drop-registered-user-table!
-- drop the registered_user table
DROP TABLE registered_user;

-- name: create-registered-user-table-if-not-exists!
-- create the registered_user table if it does not exist
CREATE TABLE IF NOT EXISTS registered_user (
   id              SERIAL PRIMARY KEY NOT NULL
   , email         CITEXT             NOT NULL UNIQUE
   , username      CITEXT             NOT NULL UNIQUE
   , password      TEXT               NOT NULL
   , refresh_token TEXT               NOT NULL
);
