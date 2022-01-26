#!/bin/bash

java -jar target/cassandromat-1.0-SNAPSHOT-jar-with-dependencies.jar stress_test 2>logs/err_log.txt | tee logs/log.txt
