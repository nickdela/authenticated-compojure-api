# {{ns-name}}

Details Go Here...

## Usage

### Customize profiles.clj

The project pulls sensitive information from environment variables.
Make sure to populate the file correctly.

Equivalent environment variables are `DATABASE_URL`, `USER_EMAIL`, `USER_PASS_KEY`, `AUTH_KEY`.

## Create the PostgreSQL database for local development

`psql < script/init_database.sql`

### Running Locally

`lein run -m {{ns-name}}.server 3000`

Then visit [http://localhost:3000/api-docs/index.html](http://localhost:3000/api-docs/index.html)

### Table migrations / creation

When you start the server any needed tables will be created automatically.

You will now be able to create new users.

### Running Tests

`lein test`

### Documentation

The HTML documentation can generated locally with `lein doc` the output will be
saved in `doc/api`.
