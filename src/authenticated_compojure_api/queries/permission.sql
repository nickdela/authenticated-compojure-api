-- name: insert-permission<!
-- inserts a single permission into the permission table
INSERT INTO permission (permission)
VALUES                 (:permission);

-- name: drop-permission-table!
-- drop the permission table
DROP TABLE permission;

-- name: create-permission-table-if-not-exists!
-- create the permission table if it does not exist
CREATE TABLE IF NOT EXISTS permission (
    id          SERIAL PRIMARY KEY
    , permission TEXT UNIQUE NOT NULL);
