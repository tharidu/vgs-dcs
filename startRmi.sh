#!/bin/bash
for node in `cat dashosts`
do
echo "Running rmi registry in $node"
ssh $node "./rmi.sh > /tmp/rmi 2>&1 &"
done
