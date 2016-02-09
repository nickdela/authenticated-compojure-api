# authenticated-compojure-api

[![Dependencies Status](http://jarkeeper.com/JarrodCTaylor/authenticated-compojure-api/status.png)](http://jarkeeper.com/JarrodCTaylor/authenticated-compojure-api)

An example compojure-api application demonstrating everything you need for
token-based authentication using Buddy.

## Usage

### Add profiles.clj

The project pulls sensitive information from environment variables. For local
development you will need a `profiles.clj` in the root of the project. Populate
the file like so:

``` clojure
{:dev-env-vars  {:env {:database-url  "postgres://auth_user:password1@127.0.0.1:5432/auth?stringtype=unspecified"
                       :user-email    "Jarrod@JarrodCTaylor.com"
                       :user-pass-key "mandrill-pass-key"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}
 :test-env-vars {:env {:database-url  "postgres://auth_user:password1@127.0.0.1:5432/auth_test?stringtype=unspecified"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}}
```
Equivalent environment variables are `DATABASE_URL`, `USER_EMAIL`, `USER_PASS_KEY`, `AUTH_KEY`.

## Create the PostgreSQL database for local development

`psql < script/init_database.sql`

You can run the same script on remote servers -- see psql documentation.


### Running Locally

`lein run -m authenticated-compojure-api.server 3000`

Then visit [http://localhost:3000/api-docs/index.html](http://localhost:3000/api-docs/index.html)

### Table migrations / creation

When you start the server any needed tables will be created automatically.

See "Manual Setup" below if you see errors about missing tables (especially on old DB servers).

You will now be able to create new users.

### Running Tests

`lein test`

### Documentation

The HTML documentation can be viewed online at [authenticated-compojure-api](http://www.jarrodctaylor.com/authenticated-compojure-api/)
or generated locally with `lein doc` the output will be saved in `doc/api`.

### Manual Database setup

If you have an old version of PostgreSQL (8.4 or earlier), you may want to do this manually.

##### Permissions

When you start the server any needed tables will be created automatically.
Starting out you may need to create a `basic` permission in the permissions
table.

``` sql
INSERT INTO permission (permission)
VALUES ('basic');
```

##### Table Creation

``` sql
CREATE DATABASE auth;
CREATE DATABASE auth_test;
\c auth;
CREATE EXTENSION citext;
\c auth_test;
CREATE EXTENSION citext;
CREATE ROLE auth_user LOGIN;
ALTER ROLE auth_user WITH PASSWORD 'password1';
GRANT ALL PRIVILEGES ON DATABASE auth to auth_user;
GRANT ALL PRIVILEGES ON DATABASE auth_test to auth_user;
```
