-- name: create-permission-table-if-not-exists!
-- create the permission table if it does not exist
CREATE TABLE IF NOT EXISTS permission (
    id          SERIAL PRIMARY KEY
    , permission TEXT UNIQUE NOT NULL);

-- name: drop-permission-table!
-- drop the permission table
DROP TABLE permission;

-- name: create-basic-permission-if-not-exists!
-- create the 'basic' permission if it does not exist
INSERT INTO permission (permission)
 SELECT 'basic'
 WHERE NOT EXISTS (SELECT 1 FROM permission WHERE permission='basic');
