# {{ns-name}}

Details Go Here...

## Usage

### Add profiles.clj

The project pulls sensitive information from environment variables. For local
development you will need a profiles.clj in the root of the project. Populate
the file like so:

``` clojure
{:dev-env-vars  {:env {:database-url  "postgres://{{sanitized}}_user:password1@127.0.0.1:5432/{{sanitized}}?stringtype=unspecified"
                       :sendinblue-user-login    "You@Something.com"
                       :sendinblue-user-password "sendinblue"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}
 :test-env-vars {:env {:database-url  "postgres://{{sanitized}}_user:password1@127.0.0.1:5432/{{sanitized}}_test?stringtype=unspecified"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}}
```
Equivalent environment variables are `DATABASE_URL`, `USER_EMAIL`, `USER_PASS_KEY`, `AUTH_KEY`.

## Create the PostgreSQL database for local development

`psql < script/init_database.sql`

## Run Migrations

Migrations are managed by [migratus](https://github.com/yogthos/migratus) to begin working initially
run both:

`lein migratus migrate && lein with-profile test migratus migrate`

### Running Locally

`lein run -m {{ns-name}}.server 3000`

#### Example create a new user

`curl 'http://localhost:3000/api/v1/user' -X POST -H 'Content-Type: application/json' -d '{"email":"some@one.com","username":"someone","password":"somePassword1"}'`

### Table migrations / creation

When you start the server any needed tables will be created automatically.

You will now be able to create new users.

### Running Tests

`lein test`

*NOTE* Test will fail the first run after migrations due to a duplicate key.

### Documentation

The HTML documentation can generated locally with `lein doc` the output will be
saved in `doc/api`.
