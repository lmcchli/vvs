#!/bin/sh
#############################################
# Copyright (c) 2002 Abcxyz Radio Systems AB
# All Rights Reserved
#
# Description: This script makes a backup of 
# the NTF and saves it as NTF.<hostname>.backup.tar
# Either it prints "ERROR: <some description>" when something has gone 
# wrong, OR it prints RESULTFILE <path and name of resultfile> when it has succeeded.
#
# Last changed: 2003-04-15 
#############################################

PRG_NAME=`basename $0`
PWD=`pwd`
TR="/usr/xpg4/bin/tr"


################
#Sets BASEDIR and NTFHOME and exits if this is not possible
################
setdirectories() {
    BASEDIR=`pkgparam LMENtf BASEDIR`
    if [ $? != "0" ]; then
        error "ERROR: No NTF package is installed, can not make backup"
    fi

    cd `dirname $0`
    TMP=`pwd`
    if [ `basename $TMP` != moipbackup ]; then
        error "$PROGNAME: ERROR: can not determine location of files, because the backup script is in a strange location"
    fi

    TMP=`dirname $TMP`
    if [ "$TMP" = /apps ]; then
        NTFHOME=$BASEDIR
    else
        NTFHOME=$TMP
    fi
}


###########
#Writes out an error and exits
###########
error(){
  echo "$PRG_NAME: ERROR: $*"
  rm -f $NTF_TAR_FILE
  exit 1
}


###########
#Tar the backup together.
###########
tar_NTF(){
    FILES="cfg logs data templates"
    
    cd $NTFHOME
    tar cf $NTF_TAR_FILE $FILES >> /dev/null 2>&1
}


#############
#Main program
#############
setdirectories;

OPERATION=`echo "$1" | $TR '[:upper:]' '[:lower:]'`
INSTANCE=`$BASEDIR/bin/getconfig MCR_INSTANCE_NAME $NTFHOME/cfg/notification.cfg`
NTF_TAR_FILE=$PWD/NTF.$INSTANCE.backup.tar

#Remove old tar file.
if [ -f $NTF_TAR_FILE ]; then rm -f $NTF_TAR_FILE; fi

#If script takes clean as argument, exit the script without making a new tar.
if [ "$OPERATION" = clean ]; then
    exit 0
fi

#Tar files for all NTF instances.
tar_NTF

# Backup completed. Echo the name of the resulting file.
echo RESULTFILE $NTF_TAR_FILE
