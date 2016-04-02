#!/bin/bash
if [ $# -ne 2 ]
then
	echo "Usage : ./runClient.sh <node> <number_of_jobs>"
	exit 1
fi
echo "Running client at $1 with $2 jobs"
ssh $1 -n "./client.sh $2"
