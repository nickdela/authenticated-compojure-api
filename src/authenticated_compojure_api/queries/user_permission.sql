-- name: insert-permission-for-user<!
-- inserts the specified permission for the designated user
INSERT INTO user_permission
            (user_id
             ,permission)
VALUES      (:userid
             ,:permission);

-- name: get-permissions-for-userid
-- get all the permissions for a given userid
SELECT (user_id, permission)
FROM   user_permission
WHERE  user_id = :userid

-- name: drop-user-permission-table!
-- drop the user_permission table
DROP TABLE user_permission CASCADE;

-- name: create-user-permission-table-if-not-exists!
-- create the user_permission table if it does not exist
CREATE TABLE IF NOT EXISTS user_permission (
    id          SERIAL PRIMARY KEY
    ,user_id    INTEGER REFERENCES registered_user (id) ON DELETE CASCADE
    ,permission TEXT REFERENCES permission (permission) ON DELETE CASCADE);
