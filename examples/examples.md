# Wiremock examples

## Record a Request-Response

Run wiremock: `java -jar wiremock-standalone-2.0.10-beta.jar --port 9832 --verbose --record-mappings --proxy-all="https://github.com/"`

Run the request: `curl "http://localhost:9832/search?utf8=✓&q=wiremock"`

We can also record this information while wiremock is running in proxy.
Start wiremock with this command:

`java -jar wiremock-standalone-2.0.10-beta.jar --port 9832 --enable-browser-proxying --verbose --record-mappings`

Then perform a couple of GET to different hosts

```
curl -D - --proxy localhost:9832 --location "http://stackoverflow.com/search?q=wiremock"
curl -D - --proxy localhost:9832 --location http://www.joseoc.es/?s=wiremock
curl -D - --proxy localhost:9832 --location http://echo-joseortiz.rhcloud.com/echo/ping
```

Now you'll have these mappings saved in json files under `mappings` and `__files` folders.


## Faults

### Delays 

Let's set a delay of 5 seconds:

```
curl -D - -X POST http://localhost:9832/__admin/settings \
--data '{ "fixedDelay": 5000 }'
```

And now let's try to get to the mapping recorded before:

```
curl "http://localhost:9832/search?utf8=✓&q=wiremock"
```

We can check that now it takes 5 secs to get the response.

### Socket Timeout

Let's define a socket timeout.

```
curl -D - -X POST http://localhost:9832/__admin/socket-delay \
--data '{ "milliseconds": 300 }'
```

And now let's try to get to the mapping recorded before:

```
curl "http://localhost:9832/search?utf8=✓&q=wiremock"
