-- name: all-quotes
-- Selects all quotes
SELECT id
      ,author
      ,quote
FROM quotes;

-- name: get-quote
-- select a single quote matching provided id
SELECT id
      ,author
      ,quote
FROM quotes
WHERE id = :id;

-- name: delete-quote!
-- delete a single quote matching provided id
DELETE FROM quotes
WHERE id = :id;

-- name: insert-quote<!
-- inserts a single quote
INSERT INTO quotes
            (author ,quote)
VALUES      (:author ,:quote);

-- name: update-quote<!
-- update a single quote matching provided id
UPDATE quotes
SET    author = :author
      ,quote = :quote
WHERE  id = :id;

-- name: drop-quotes-table!
-- drop the quotes table
DROP TABLE quotes;

-- name: create-quotes-table-if-not-exists!
-- create the quotes table if it does not exist
CREATE TABLE IF NOT EXISTS quotes (
    id      SERIAL PRIMARY KEY
    ,author VARCHAR (30) NOT NULL
    ,quote  TEXT NOT NULL);
