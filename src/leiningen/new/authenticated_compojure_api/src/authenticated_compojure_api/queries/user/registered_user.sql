-- :name all-registered-users
-- :command :query
-- :result :many
-- :doc Selects all registered-users
SELECT id
       , email
       , username
       , password
FROM   registered_user;

-- :name get-registered-user-by-id
-- :command :query
-- :result :one
-- :doc Selects the (id, email, username, password) for registered user matching the id
SELECT id
       , email
       , username
       , password
FROM   registered_user
WHERE  id = :id

-- :name get-registered-user-by-username
-- :command :query
-- :result :one
-- :doc Selects the (id, email, username) for registered user matching the username
SELECT id
       , email
       , username
FROM   registered_user
WHERE  username = :username

-- :name get-registered-user-by-email
-- :command :query
-- :result :one
-- :doc Selects the (id, email, username) for registered user matching the email
SELECT id
       , email
       , username
FROM   registered_user
WHERE  email = :email

-- :name get-registered-user-details-by-username
-- :command :query
-- :result :one
-- :doc Selects user details for matching username
SELECT   reg_user.id
         , reg_user.created_on
         , reg_user.email
         , reg_user.username
         , reg_user.password
         , STRING_AGG(perm.permission, ',') AS permissions
FROM     registered_user                    AS reg_user
         JOIN user_permission               AS perm
           ON (reg_user.id = perm.user_id)
WHERE    reg_user.username = :username
GROUP BY reg_user.id;

-- :name get-registered-user-details-by-email
-- :command :query
-- :result :one
-- :doc Selects user details for matching email
SELECT   reg_user.id
         , reg_user.email
         , reg_user.username
         , reg_user.password
         , STRING_AGG(perm.permission, ',') AS permissions
FROM     registered_user                    AS reg_user
         JOIN user_permission               AS perm
           ON (reg_user.id = perm.user_id)
WHERE    reg_user.email = :email
GROUP BY reg_user.id;

-- :name insert-registered-user!
-- :command :insert
-- :result :raw
-- :doc Inserts a single user
INSERT INTO registered_user (
    email
    , username
    , password)
VALUES (
    :email
    , :username
    , :password);

-- :name update-registered-user!
-- :command :execute
-- :result :affected
-- :doc Update a single user matching provided id
UPDATE registered_user
SET    email = :email
       , username = :username
       , password = :password
WHERE  id = :id;

-- :name update-registered-user-password!
-- :command :execute
-- :result :affected
-- :doc Update the password for the user matching the given userid
UPDATE registered_user
SET    password = :password
WHERE  id = :id;

-- :name delete-registered-user!
-- :command :execute
-- :result :affected
-- :doc Delete a single user matching provided id
DELETE FROM registered_user
WHERE       id = :id;
