#!/bin/bash

JAR_PATH=$1
if [ ! -e $JAR_PATH ]; then
    echo "The path '$JAR_PATH' does not exist"
    exit 1
fi

if [ ! -f $JAR_PATH ]; then
    echo "The path '$JAR_PATH' is not a file"
    exit 1
fi

if [  -z ${ES_HOST+x} ]; then
    echo "The env variable ES_HOST is undefined"
    exit 1
fi

if [  -z ${ES_PORT+x} ]; then
    echo "The env variable ES_PORT is undefined"
    exit 1
fi

if [  -z ${ES_CLUSTER_NAME+x} ]; then
    echo "The env variable ES_CLUSTER_NAME is undefined"
    exit 1
fi

CONFIG_FILE=/conf/application.properties
mkdir /conf
echo "elasticsearch.host=$ES_HOST" > $CONFIG_FILE
echo "elasticsearch.port=$ES_PORT" >> $CONFIG_FILE
echo "elasticsearch.cluster-name=$ES_CLUSTER_NAME" >> $CONFIG_FILE

java -Dspring.config.location=/conf/application.properties  -jar $JAR_PATH

