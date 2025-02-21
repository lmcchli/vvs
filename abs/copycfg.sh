#!/bin/ksh
#
# Post install script that takes the cfg files from the nfs drive to the /opt/moip...
#

echo -e "copying config files over /opt/moip\n" 

mkdir -p /opt/moip/config
mkdir -p /opt/moip/common
mkdir -p /opt/msgcore/config
mkdir -p /opt/msgcore/common

cp -r /home/ems/templates/moip/config/tn/* /opt/moip/config/
cp -r /home/ems/templates/moip/config/common/* /opt/moip/common/
cp -r /home/ems/templates/msgcore/config/common/* /opt/msgcore/common/
cp -r /home/ems/templates/msgcore/config/da/* /opt/msgcore/config/

  
