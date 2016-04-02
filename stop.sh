#!/bin/bash 
for node in `cat dashosts`
do
	echo "killing all processes in all nodes"
	ssh $node "pkill -f group8"
done
