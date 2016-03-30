-- :name create-permission-table-if-not-exists!
-- :command :execute
-- :result :affected
-- :doc Create the permission table if it does not exist
CREATE TABLE IF NOT EXISTS permission (
    id          SERIAL PRIMARY KEY
    , permission TEXT UNIQUE NOT NULL);

-- :name drop-permission-table!
-- :command :execute
-- :result :affected
-- :doc Drop the permission table
DROP TABLE permission;

-- :name create-basic-permission-if-not-exists!
-- :command :insert
-- :result :raw
-- :doc Create the 'basic' permission if it does not exist
INSERT INTO permission (permission)
 SELECT 'basic'
 WHERE NOT EXISTS (SELECT 1 FROM permission WHERE permission='basic');
