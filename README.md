# authenticated-compojure-api

Everything you need to get you up and running with a compojure-api utilizing token-based authentication.

## Usage

### Create New Local Project From Template

Make sure you have the [latest version of leiningen installed](https://github.com/technomancy/leiningen#installation).

```
lein new authenticated-compojure-api <project-name>
```

### Customize profiles.clj

The project pulls sensitive information from environment variables.
Make sure to populate the file completely.

Equivalent environment variables are `DATABASE_URL`, `USER_EMAIL`, `USER_PASS_KEY`, `AUTH_KEY`.

## Create the PostgreSQL database for local development

`psql < script/init_database.sql`

## Run Migrations

Migrations are managed by [migratus](https://github.com/yogthos/migratus) to begin working initially
run both:

`lein migratus migrate`
`lein with-profile test migratus migrate`

### Running Locally

`lein run -m <project-name>.server 3000`

Then visit [http://localhost:3000/api-docs/index.html](http://localhost:3000/api-docs/index.html)

### Running Tests

`lein test`

### Documentation

The HTML documentation can be generated with `lein doc` the output will be
saved in `doc/api`.
