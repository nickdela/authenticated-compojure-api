# authenticated-compojure-api

An example compojure-api application demonstrating authentication using buddy.

## Usage

### Running

`lein ring server`

### Running Tests

`lein midje`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`
