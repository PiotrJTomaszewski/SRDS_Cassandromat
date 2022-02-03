#!/bin/bash

if [ $# -lt 2 ]
then
    echo "1. node address, 2. node port"
    exit -1
fi

java -jar target/cassandromat-1.0-SNAPSHOT-jar-with-dependencies.jar check_logs $1 $2 2>/dev/null | tee logs/checker_log.txt
