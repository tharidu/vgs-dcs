#!/bin/bash
for node in `cat dashosts`
do 
echo "Checking rmi for $node"
ssh $node "ps aux | grep rmiregistry"
done

