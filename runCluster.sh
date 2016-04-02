#!/bin/bash

for node in $@
do 
	echo -e "For cluster@$node enter primary GS url: \c"
	read primaryGs
	echo -e "For cluster@$node enter replica GS url: \c"
	read replicaGs
	echo -e "For cluster@$node enter number of node: \c"
	read noNodes
	echo "Starting a cluster@$node"
	ssh $node "./cluster.sh $node $primaryGs $replicaGs $noNodes"
done 	
