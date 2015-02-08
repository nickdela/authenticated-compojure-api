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

-- name: get-user-details-by-username
-- Selects user details for matching username
SELECT reg_user.id
       ,reg_user.email
       ,reg_user.username
       ,reg_user.password
       ,reg_user.refresh_token
       ,string_agg(perm.permission, ',') as permissions
FROM   registered_user      AS reg_user
       JOIN user_permission AS perm
         ON ( reg_user.id = perm.user_id )
WHERE  reg_user.username = :username
GROUP  BY reg_user.id;

-- name: get-registered-user-by-username
-- Selects the (id, email, username) for registered user matching the username
SELECT id
       ,email
       ,username
FROM registered_user
WHERE username = :username

-- name: get-registered-user-by-email
-- Selects the (id, email, username) for registered user matching the email
SELECT id
       ,email
       ,username
FROM registered_user
WHERE email = :email

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
   ,email         CITEXT UNIQUE NOT NULL
   ,username      CITEXT UNIQUE NOT NULL
   ,password      TEXT NOT NULL
   ,refresh_token TEXT NOT NULL);
