#!/bin/bash

for node in $@
do
	echo -e "For $node enter primaryGsNodenode: \c "
	read primaryGs
	ssh $node "./backupGs.sh $primaryGs > /tmp/backupGs 2>&1 &"
done
