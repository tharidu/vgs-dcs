#!/bin/bash
cd src
java -Djava.rmi.server.codebase=file://bin/ -cp bin:log4j-api-2.3.jar:log4j-core-2.3.jar dcs.group8.cluster.RunCluster $1 $2 $3 $4 &
exit 0
