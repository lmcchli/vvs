#!/bin/ksh
#############################################
#
# This scrippt uninstall mas on a client
# By : Håkan Tunell
# Date :2005-12-02
#
# PARAMETERS:
#
# IN: None
#
# OUT: None
#
#
# Revision
# By     Date         What				Version
#------------------------------------------------------ -------
# hatu   2005-12-02   Created				1.0
############################################
#/set -vx

 
VERSION="1.0"
DATE=`date '+%y-%m-%d %H:%M:%S'`

BACKUP_LOG="BACKUP.log"
ACC_DATA="mas_acc.dat"
RCCONF_FILE="rc.mas.conf"
RESPONSE_FILE="MAS_RESPONSE.cfg"

MAS_INSTALLED="/opt/MAS_INSTALLED.cfg"

CMD=$0
DIRNAME=`dirname $CMD`
cd $DIRNAME
DIRNAME=`pwd`

LOG_DIR="/opt/log/mas/"
LOG_FILENAME="MAS.log"
LOG_FILENAME_TMP=$DIRNAME"/MAS_TMP.log"


doMasLog() {

#/ **********************************
#/ Check if log directory exsist
#/ **********************************
   # echo "[$HOST $DATE] Check log dir  :\c"
   if [ -d $LOG_DIR ] 
   then
      if [ ! -w $LOG_DIR ] 
      then
    	 echo " Log dir not writable. ("$LOG_DIR")"
    	 echo " Write loggs to installdir."
    	 LOG_DIR=$INSTALL_DIR
      fi
   else
      #/ **********************************
      #/ Create log directory
      #/ **********************************
      mkdir $LOG_DIR
      if [ $? -gt 0 ]
      then
         LOG_DIR=$INSTALL_DIR
      fi
   fi
   
   #/ append current log to logfile to log dir ("$installdir/log" or "opt/log/mas dir")
   echo $LOG_FILENAME_TMP ">>" $LOG_DIR"/"$LOG_FILENAME
   cat $LOG_FILENAME_TMP >> $LOG_DIR/$LOG_FILENAME


}

#/ **********************************
#/ Chek input parameter
#/ **********************************
echo 
echo "[$HOST $DATE] Start MAS uninstall"

echo " " > $LOG_FILENAME_TMP
echo "[$HOST $DATE] MAS uninstall started " >> $LOG_FILENAME_TMP


#/ **********************************
#/ Check if prev installation exsist
#/ **********************************
	if [[ -f $MAS_INSTALLED ]]
	then
		#/ source file with the variable PREV_INSTALL_DIR
		. $MAS_INSTALLED  
		PREV_INSTALL_EXSIST="TRUE"
	else
		echo "[$HOST $DATE] Uninstall failed. Cant find install dir ."
		echo "[$HOST $DATE] Uninstall failed. Cant find install dir ." >> $LOG_FILENAME_TMP
		doMasLog
		exit 1
	fi


#/ **********************************
#/ Remove installed files and directorys. Only keep backup directory.
#/ **********************************
echo "[$HOST $DATE] Remove MAS installation dir : $PREV_INSTALL_DIR ." >> $LOG_FILENAME_TMP
rm -r $PREV_INSTALL_DIR
if [ $? -gt 0 ]
then
   echo "[$HOST $DATE] Remove failed. " 
   echo "[$HOST $DATE] Remove failed. " >> $LOG_FILENAME_TMP
   doMasLog
   exit 1
fi



	











