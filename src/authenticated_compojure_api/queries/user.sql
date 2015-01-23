-- name: all-users
-- Selects all users
SELECT id
       ,access
       ,username
       ,password
       ,refresh_token
FROM   users;

-- name: get-user-by-reset-token
-- Selects a user with matching reset-token
SELECT id
       ,access
       ,username
       ,password
       ,refresh_token
FROM   users
WHERE  refresh_token = :refresh_token;

-- name: get-user-by-username
-- Selects a user with matching username
SELECT id
       ,access
       ,username
       ,password
       ,refresh_token
FROM   users
WHERE  username = :username;

-- name: insert-user<!
-- inserts a single user
INSERT INTO users
            (access
             ,username
             ,password
             ,refresh_token)
VALUES      (:access
             ,:username
             ,:password
             ,:refresh_token);

-- name: drop-users-table!
-- drop the users table
DROP TABLE users;

-- name: create-users-table-if-not-exists!
-- create the users table if it does not exist
CREATE TABLE IF NOT EXISTS users (
   id             SERIAL PRIMARY KEY
   ,access        VARCHAR (20) NOT NULL
   ,username      VARCHAR (25) UNIQUE NOT NULL
   ,password      VARCHAR (225) NOT NULL
   ,refresh_token VARCHAR (50) NOT NULL);
