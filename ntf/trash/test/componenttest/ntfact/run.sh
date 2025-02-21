#!/bin/bash

for tst in `ls /apps/dist/ntfact/tst/*.tst`
do
(cd /apps/dist/starfish/ && ./starfish_engine.sh $tst)
done
