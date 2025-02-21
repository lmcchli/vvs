#!/bin/sh
#-----------------------------------------------------------------#
#  This postinstall will ensure that the ${rpm.alarms.directory}
#  is created and owned by mmas:mmas 
#-----------------------------------------------------------------#
if [ ! -f /tmp/NoMioNfsMount ]; then
    if [ -d "/opt/moip/common/alarms" ]; then
          echo '/opt/moip/common/alarms already exists, so there is no need to create it.'
    else
      echo 'Creating directory /opt/moip/common/alarms'
      mkdir -p /opt/moip/common/alarms
	  chown mmas:mmas /opt/moip/common/alarms
    fi
	
    echo 'Creating log for NTF'
    mkdir -p /home/messaging/templates/moip/logs/ntf -m 777
	chown mmas:mmas /home/messaging/templates/moip/logs/ntf
	
    echo 'Creating log for MAS'
    mkdir -p /home/messaging/templates/moip/logs/mas -m 777
	chown mmas:mmas /home/messaging/templates/moip/logs/mas
    ls -al /home/messaging/templates/moip/logs
fi
exit 0
