#!/bin/bash

if [ $# -lt 4 ]
then
    echo "1. node address, 2. node port, 3. node count, 4. node ID"
    exit -1
fi

java -jar target/cassandromat-1.0-SNAPSHOT-jar-with-dependencies.jar stress_test $1 $2 $3 $4 2>logs/err_log.txt | tee logs/log.txt
