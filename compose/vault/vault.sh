
setKV(){
    RET=1
    until [ ${RET} -eq 0 ]; do
        echo trying to set the secret in vault
        vault kv
        vault kv put secret/dev/rollcall "elasticsearch.user"=${ELASTICSEARCH_USER} "elasticsearch.password"=${ELASTICSEARCH_PASSWORD}
        RET=$?
        sleep 1
    done
    echo done
}

start(){
    echo Start vault server
    vault server -dev
}

setKV & start