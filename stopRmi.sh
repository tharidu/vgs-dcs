#!/bin/bash
for node in `cat dashosts`
do
echo "Killing rmi registry in $node"
ssh $node "pkill -f rmiregistry"
done
