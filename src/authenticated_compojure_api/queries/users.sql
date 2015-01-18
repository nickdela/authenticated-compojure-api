-- name: all-users
-- Selects all users
SELECT id
       ,access
       ,username
       ,password
       ,refresh_token
FROM   users;

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
   ,username      VARCHAR (25) NOT NULL
   ,password      VARCHAR (25) NOT NULL
   ,refresh_token VARCHAR (50) NOT NULL);
