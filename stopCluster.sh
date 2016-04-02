#!/bin/bash

for node in `cat ~/dashosts`
do
	ssh $node "pkill -f cluster"
done
