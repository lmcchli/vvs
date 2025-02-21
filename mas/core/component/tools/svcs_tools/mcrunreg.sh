#!/bin/ksh
###################################################################
# File    : mcrreg.sh
# Author  : Hakan Tunell
# Created : 2006-04-10
#
# Description:
#	This file is used in when registiring services
#
# OS: 	Solaris 10
#
# History
#	Date		Who	What
#	2006-04-10  	MHATU	Created
#
#
####################################################################



#*********************************************
# These parameters exsist in mas.conf file
# INSTALL_DIR=#__INSTALL_DIR__#
# JAVA_HOME=#__JAVA_HOME__#
#*********************************************


CRONFILE="/tmp/mas.cron"
JAVA_CLASSPATH=""
BASEDIR=`pkgparam MOBYmas BASEDIR`
params=$@
name=$1
noOfArguments=$#

# Load config values
. $BASEDIR/etc/mas.conf



DIRNAME=`pwd`

cd $BASEDIR

setupEnv() {

    MAS_INSTALL_PATH=$BASEDIR
    export MAS_INSTALL_PATH    

    
    # set up library path
    LD_LIBRARY_PATH=/usr/lib/lwp:/usr/sfw/lib:/opt/sfw/lib:$BASEDIR/lib
    export LD_LIBRARY_PATH
     
    # Setup java class path
    libs=`ls $BASEDIR/lib/*.jar`
    for lib in $libs
    do 
       JAVA_CLASSPATH=$JAVA_CLASSPATH$lib":"
    done

    # Add lib directory to class path
    JAVA_CLASSPATH=$BASEDIR/lib:$BASEDIR/etc:$BASDIR/cfg:$JAVA_CLASSPATH

}




cronEntry() {
	crontab -l root > $CRONFILE
	CRONEXIST=`grep "mcrlib unregister $params" $CRONFILE` 
	if [ "$CRONEXIST" = "" ]; then
		echo "0,5,10,15,20,25,30,35,40,45,50,55 * * * * $BASEDIR/etc/mcrlib unregister $params > /dev/null 2>&1" >> $CRONFILE
		crontab $CRONFILE
	fi
}



#
# MAIN
#


# if less then 1 argument, exit
if (( $noOfArguments < 2 ))
then
	echo "Parameter Error. No retries will be attempted"
        exit 1
fi


# try to unregister, 
# if exit code was 0, OK. 
# if exit code was 1, try to add cron entry
# if exit code was 2, dont try to add cron entry. 

$BASEDIR/etc/mcrlib unregister $params
exitCode=$?

case "$exitCode" in
        '0')
                #OK
                echo "Unregistration ok"
                exit 0
                ;;

        '1')
                # try to unregister a cron entry;;
                echo "MCR unreachable. No retries will be attempted."
                echo "MCR entries must be removed manually."
                # cronEntry
                exit 0
                ;;

        '2')
                # Parameter fault,exit
                echo "Parameter Error. No retries will be attempted"
                echo "MCR entries must be removed manually."
                exit 0;;

        *)
                exit 0
                ;;
esac


