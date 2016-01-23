# authenticated-compojure-api

[![Dependencies Status](http://jarkeeper.com/JarrodCTaylor/authenticated-compojure-api/status.png)](http://jarkeeper.com/JarrodCTaylor/authenticated-compojure-api)

An example compojure-api application demonstrating everything you need for
token based authentication using buddy.

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
## Create the Postgres database for local dev

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

### Running Locally

`lein run -m authenticated-compojure-api.server 3000`

### Add the required permission

When you start the server any needed tables will be created automatically.
Starting out you will need to create a `basic` permission in the permissions
table.

``` sql
INSERT INTO permission (permission)
VALUES ('basic');
```

You will now be able to create new users.

### Running Tests

`lein test`

### Documentation

The HTML documentation can be viewed online at [authenticated-compojure-api](http://www.jarrodctaylor.com/authenticated-compojure-api/)
or generated locally with `lein doc` the output will be saved in `doc/api`.
