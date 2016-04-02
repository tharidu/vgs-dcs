#!/bin/bash

cd src
java -Djava.rmi.server.codebase=file://bin/ -cp bin:log4j-api-2.3.jar:log4j-core-2.3.jar dcs.group8.gridscheduler.RunGridScheduler false &
exit 0
