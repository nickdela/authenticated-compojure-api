-- allow low-level commands on a remote DB
CREATE EXTENSION IF NOT EXISTS dblink;

-- ensure required user has been created
DO
$body$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_user
      WHERE  usename = '{{sanitized}}_user') THEN

      CREATE ROLE {{sanitized}}_user LOGIN PASSWORD 'password1';
   END IF;
END
$body$;

-- ensure required databases have been created
DO
$doDev$
BEGIN

IF EXISTS (SELECT 1 FROM pg_database WHERE datname = '{{sanitized}}') THEN
   RAISE NOTICE 'Database {{sanitized}} already exists';
ELSE
   PERFORM dblink_exec('dbname=' || current_database()  -- current db
                     , 'CREATE DATABASE {{sanitized}} OWNER {{sanitized}}_user');
END IF;

END
$doDev$;


DO
$doTest$
BEGIN

IF EXISTS (SELECT 1 FROM pg_database WHERE datname = '{{sanitized}}_test') THEN
   RAISE NOTICE 'Database {{sanitized}}_test already exists';
ELSE
   PERFORM dblink_exec('dbname=' || current_database()  -- current db
                     , 'CREATE DATABASE {{sanitized}}_test OWNER {{sanitized}}_user');
END IF;

END
$doTest$;

GRANT ALL PRIVILEGES ON DATABASE {{sanitized}} to {{sanitized}}_user;
GRANT ALL PRIVILEGES ON DATABASE {{sanitized}}_test to {{sanitized}}_user;

-- add case-insensitive option to both databases
\c {{sanitized}};
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c {{sanitized}}_test;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
