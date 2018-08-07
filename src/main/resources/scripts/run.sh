#!/bin/bash

java -jar $ROLLCALL_INSTALL_PATH/install/ROLLCALL.jar \
        --spring.profiles.active=$ROLLCALL_ACTIVE_PROFILES \
        --server.port=$ROLLCALL_SERVER_PORT \
        --elasticsearch.host=$ROLLCALL_ES_HOST \
        --elasticsearch.port=$ROLLCALL_ES_PORT \
        --elasticsearch.cluster-name=$ROLLCALL_ES_CLUSTER \
        --auth.jwt.publicKeyUrl=$EGO_URL