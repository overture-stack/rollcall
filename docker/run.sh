#!/bin/bash

server_port=8080
network_id=default
elasticsearch_host=localhost
elasticsearch_port=9300
elasticsearch_custer_name=kf-es
image_name=rollcall-1.0
container_name=kf-rc

docker run \
    -d \
    --rm \
    -p $server_port:8080 \
    --net=$network_id \
    -e "ES_HOST=$elasticsearch_host" \
    -e "ES_PORT=$elasticsearch_port" \
    -e "ES_CLUSTER_NAME=$elasticsearch_custer_name" \
    --name $container_name \
    $image_name
