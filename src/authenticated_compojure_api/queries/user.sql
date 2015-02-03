-- name: all-registered-users
-- Selects all registered-users
SELECT id
       ,email
       ,username
       ,password
       ,refresh_token
FROM   registered_user;

-- name: get-user-by-reset-token
-- Selects a user with matching reset-token
SELECT id
       ,email
       ,username
       ,password
       ,refresh_token
FROM   registered_user
WHERE  refresh_token = :refresh_token;

-- name: get-user-by-username
-- Selects a user with matching username
SELECT id
       ,email
       ,username
       ,password
       ,refresh_token
FROM   registered_user
WHERE  username = :username;

-- name: insert-user<!
-- inserts a single user
INSERT INTO registered_user
            (email
             ,username
             ,password
             ,refresh_token)
VALUES      (:email
             ,:username
             ,:password
             ,:refresh_token);

-- name: drop-registered-user-table!
-- drop the registered_user table
DROP TABLE registered_user;

-- name: create-registered-user-table-if-not-exists!
-- create the registered_user table if it does not exist
CREATE TABLE IF NOT EXISTS registered_user (
   id             SERIAL PRIMARY KEY
   ,email         TEXT UNIQUE
   ,username      TEXT UNIQUE NOT NULL
   ,password      TEXT NOT NULL
   ,refresh_token TEXT NOT NULL);
