#!/bin/sh

rm -f /home/messaging/installation/vvs/lib >& /dev/null
ln -s /cluster/software/install/automation_network/lib /home/messaging/installation/vvs

exit 0;
