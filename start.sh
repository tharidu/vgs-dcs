#!/usr/bin/env bash
PWD=`pwd`
echo "Entering bin folder of the project.."
cd "$PWD/bin/"
BR="bin/rmiregistry"
REG="$JAVA_HOME$BR"
echo "Starting rmiregistry in the background"
`$REG` &
REG_PID=$!
sleep 1
echo "Starting the cluster..."
java dcs.group8.cluster.RunCluster localhost localhost 10  &
sleep 2
echo "Starting the grid scheduler..."
java dcs.group8.gridscheduler.RunGridScheduler localhost &
sleep 4
java dcs.group8.client.RunClient &
read -n1 -r -p "Press any key to stop the execution"
echo "Killing rmiregistry.."
pkill -f rmiregistry
