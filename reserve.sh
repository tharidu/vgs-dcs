#!/bin/bash
rm -rf $HOME/dashosts
preserve -t 04:00:00 -np $1
sleep 1
cmd=`preserve -llist | grep $USER`
set -- $cmd
for var in $@
do
  echo $var | grep "node" >> $HOME/dashosts
done
