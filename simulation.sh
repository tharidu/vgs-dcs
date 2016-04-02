#!/bin/bash
./startRmi.sh
echo "[+] Running the clusters"
./runCluster.sh <<ANSWERS
node322 node322 node322 5
node331 node322 node322 5
ANSWERS

sleep 2
echo "[+] Running the primary GS"
./runGs.sh node322
sleep 2
echo "[+] Running the client"
./runClient.sh node332 10
