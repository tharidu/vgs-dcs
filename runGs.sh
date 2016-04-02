#!/bin/bash

for node in $@
do
	echo "Running primary GS in $node"
	ssh $node "./gs.sh > /tmp/gs 2>&1 &"
done
