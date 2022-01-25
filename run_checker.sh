#!/bin/bash

java -jar target/cassandromat-1.0-SNAPSHOT-jar-with-dependencies.jar check_logs | tee logs/checker_log.txt
