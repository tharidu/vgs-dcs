#!/bin/bash

while read node
do 
	set $node
	echo -e "$node"
	echo "Starting a cluster@$1"
	ssh $1 -n "./cluster.sh $1 $2 $3 $4 > /tmp/cluster 2>&1 &"
done 	
