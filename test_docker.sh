#!/bin/bash

docker run --network host -e "ROLLCALL_ACTIVE_PROFILES=default" -e "ROLLCALL_SERVER_PORT=9001" -e "ROLLCALL_ES_HT=127.0.0.1" -e "ROLLCALL_ES_PORT=9300" -e "ROLLCALL_ES_CLUSTER=elasticsearch" -e "EGO_URL=https://<url>/oauth/token/public_key" <id>