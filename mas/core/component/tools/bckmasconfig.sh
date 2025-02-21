#!/bin/sh

BASEDIR=/apps/mas
DATE=`date +%Y_%m_%d_%H%M`
TAR_FILE="/apps/backup/mas_${DATE}_backup.tar"
######################################################################
#
# INFO: This script reports with its output what has happened.
# Either it prints "ERROR <some description>" when something has gone
# wrong, OR
# it prints RESULTFILE <name of the resultfile> when it has succeeded.
#
######################################################################

if [ "$1" = "clean" ]; then
    cd /apps/moipbackup
    rm -f mas_*_backup.tar
    exit 0
fi

tar_files(){
   
   
   cd $BASEDIR
   tar cf $TAR_FILE $BASEDIR/cfg/*.xml $BASEDIR/lib/*.xml $BASEDIR/etc/*.conf $BASEDIR/log/* >> /dev/null 2>&1

   #mkdir logs
   #cp -r /opt/logs/mas logs
   #/tar cf $TAR_FILE mtap/cfg/flowmanager.cfg mtap/cfg/hlink.cfg cfg/datamodel.cfg cfg/mvas.cfg cfg/numberanalysis.cfg mtap/hlink/root_container/data/*.rul mtap/hlink/root_container/data/*.cfg etc/language.txt etc/TTSlanguage.txt logs/mvas/* >> /dev/null 2>&1
   #/tar cf $TAR_FILE logs/mas/* >> /dev/null 2>&1
   #rm -r logs
}
tar_files
echo "RESULTFILE $TAR_FILE"
#END
