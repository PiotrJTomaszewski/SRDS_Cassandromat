#!/bin/bash

if [ $# -lt 2 ]
then
    echo "1. node address, 2. node port"
    exit -1
fi

java -jar target/cassandromat-1.0-SNAPSHOT-jar-with-dependencies.jar create_data $1 $2 2>logs/err_log.txt | tee logs/log.txt
