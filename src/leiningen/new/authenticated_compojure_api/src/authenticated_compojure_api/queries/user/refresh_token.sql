-- :name insert-refresh-token!
-- :command :execute
-- :result :affected
-- :doc Insert a new refresh token into refresh_token table
INSERT INTO refresh_token (
    user_id
    , refresh_token)
VALUES (
    :user_id
    , :refresh_token);

-- :name delete-refresh-token!
-- :command :execute
-- :result :affected
-- :doc Delete row associated with matching refresh token
DELETE FROM refresh_token
WHERE refresh_token = :refresh_token;

-- :name update-refresh-token!
-- :command :execute
-- :result :affected
-- :doc Update the refresh token and created on values
UPDATE refresh_token
SET    refresh_token = :refresh_token
       , created_on = CURRENT_TIMESTAMP
WHERE  refresh_token = :old_token;

-- :name get-registered-user-details-by-refresh-token
-- :command :query
-- :result :one
-- :doc Selects user details for matching refresh token
SELECT   reg_user.id
         , reg_user.email
         , reg_user.username
         , reg_user.password
         , string_agg(perm.permission, ',') AS permissions
FROM     registered_user AS reg_user
         JOIN user_permission AS perm
           ON (reg_user.id = perm.user_id)
         JOIN refresh_token AS token
           ON (token.user_id = reg_user.id)
WHERE    token.refresh_token = :refresh_token
GROUP BY reg_user.id;


-- :name get-refresh-token
-- :command :query
-- :result :one
-- :doc Selects the row associated with the provided refresh token
SELECT id
       , created_on
       , user_id
       , refresh_token
FROM   refresh_token
WHERE  refresh_token = :refresh_token;
