#!/bin/bash
BASE_PATH="/home/jortiz/code/sequoia/infra/buildhelper-git-infra/dist/buildhelper/files/wiremock"

### Using log4j1
#CLASSPATH="$CLASSPATH:$BASE_PATH/log4j-1.2.17.jar"
#CLASSPATH="$CLASSPATH:$BASE_PATH/config/log4j1"

### Using log4j2
CLASSPATH="$CLASSPATH:$BASE_PATH/log4j-api-2.5.jar"
CLASSPATH="$CLASSPATH:$BASE_PATH/log4j-core-2.5.jar"
CLASSPATH="$CLASSPATH:$BASE_PATH/log4j-slf4j-impl-2.5.jar"
CLASSPATH="$CLASSPATH:$BASE_PATH/config/log4j2"

WIREMOCK="wiremock-standalone-2.0.10-beta.jar"
#WIREMOCK="wiremock-1.58-standalone.jar"

# -Dlogback.configurationFile=/home/jortiz/code/buildhelper-git-infra/dist/buildhelper/files/wiremock/config/logback.xml
java -cp $CLASSPATH:$WIREMOCK com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --port 9832 --enable-browser-proxying --verbose

