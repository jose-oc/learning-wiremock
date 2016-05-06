# WIREMOCK standalone mode

Within this folder I'll put some mapping examples as well as the jar files and the sh script to run wiremock in standalone mode.

## Run Wiremock

You can simply type `java -jar wiremock-standalone-2.0.10-beta.jar --port 9832 --verbose` or run the bash script `start_wiremock_with_logs.sh` to use log4j2.

## Mappings

The json files within the folder *mappings* will configure wiremock with those mappings.

## HTTP API

Wiremock has an HTTP API with these operations.
Let's assume Wiremock is running in local and with the port 9832.

### List mappings

List all registered mappings.

```
curl --location http://localhost:9832/__admin
```

### Create a new mapping

```
curl -D - -X POST http://localhost:9832/__admin/mappings/new \
--data '{
  "request": {
    "method": "ANY",
    "urlPath": "/some/endpoint"      
  },
  "response": {
    "status": "500",
    "body": "This is a mocked response to force a 500 error"
  }
}'

```

### Save the mapping into a file

You can save the mapping you've created with the previous curl within the *mappings* folder just with this one:

```
curl -D - -X POST http://localhost:9832/__admin/mappings/save
```

### Restore default mappings

You can restore the mappings you have in the *mappings* folder with this curl:

```
curl -D - -X POST http://localhost:9832/__admin/mappings/reset
```

### Delete all mappings

```
curl -D - -X POST http://localhost:9832/__admin/reset
```

### Shutdown wiremock

```
curl -D - -X POST http://localhost:9832/__admin/shutdown
```

### Set configuration

```
curl -D - -X POST http://localhost:9832/__admin/settings \
--data '{
    "fixedDelay": 500
}'
```