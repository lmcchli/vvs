#!/bin/ksh
#############################################
#
# This scrippt install mas on a client
# By : Håkan Tunell
# Date :2005-10-10
#
# PARAMETERS:
#
# IN: Install directory DEFAULT :/opt/mas
#
# OUT: None
#
#
# Revision
# By     Date         What				Version
#------------------------------------------------------ -------
# hatu   2005-10-10   Created				1.0
# hatu	 2005-10-27   Added functionality		1.1
# hatu	 2005-11-18   Bugfix TR25772			1.2
# hatu	 2005-11-20   Bugfix TR25825 + Some bugs	1.3
############################################
#/set -vx

 
VERSION="1.3"
DATE=`date '+%y-%m-%d %H:%M:%S'`


RCCONF_FILE="rc.mas.conf"
RESPONSE_FILE="MAS_RESPONSE.cfg"
INSTALL_STATUS="SUCCESS"
MAS_INSTALLED="/opt/MAS_INSTALLED.cfg"
MAS_INSTALLED_DIR="/opt/"
PREV_INSTALL_EXSIST="FALSE"
#/PREV_INSTALL_DIR   # THIS EXSISTS IN MAS_INSTALLED.cfg file
DEF_INSTALL_DIR="/opt/mas"
DEF_JAVA_HOME="/usr/local/jre1.5.0_05" 
	       
INSTALL_DIR=$DEF_INSTALL_DIR
JAVA_HOME=$DEF_JAVA_HOME

SILENT="NOT_SILENT"


CMD=$0
DIRNAME=`dirname $CMD`
cd $DIRNAME
DIRNAME=`pwd`

OPT_DIR="/opt/"
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

      if [ ! -w $OPT_DIR ] 
      then
         echo "[$HOST $DATE] Log dir not writable. ("$LOG_DIR")" >> $LOG_FILENAME_TMP
         #echo "[$HOST $DATE] Write loggs to installdir." >> $LOG_FILENAME_TMP
         #echo "[$HOST $DATE] Log dir not writable. ("$LOG_DIR")"
         #echo "[$HOST $DATE] Write loggs to installdir."
    	 LOG_DIR=$INSTALL_DIR
      else
         mkdir $LOG_DIR >> $LOG_FILENAME_TMP
	 if [ $? -gt 0 ]
	 then
	    LOG_DIR=$INSTALL_DIR
	 fi
      fi
   fi
   
   echo "[$HOST $DATE] Loggs written to dir : $LOG_DIR." >> $LOG_FILENAME_TMP
   echo "[$HOST $DATE] Loggs written to dir : $LOG_DIR."
   #/ append current log to logfile to log dir ("$installdir/log" or "opt/log/mas dir")
   cat $LOG_FILENAME_TMP >> $LOG_DIR/$LOG_FILENAME


}

#/ **********************************
#/ Chek input parameter
#/ **********************************
echo 
echo "[$HOST $DATE] Start MAS Installation"
if [[ ("$1" = "-?") || ($# > 1) || ($# = 1 && $1 != -[Ss][Ii][Ll][Ee][Nn][Tt]) ]]  
then
	 echo
         echo " Install script for MAS"
         echo
         echo " Version :" $VERSION
         echo " Syntax: install_mas.sh [?] | [-s]" # [-idir <install dir>] [-jdir <java_path>] "
         echo
         echo " ?	:  print this help "
         echo
         echo " -silent	:  Defines that parameters are read from a response file."
         echo " 	   A responsfile must exsist in the run directory."
         
	 
	 exit 1
fi;
echo " " > $LOG_FILENAME_TMP
echo "[$HOST $DATE] MAS Installation started " >> $LOG_FILENAME_TMP
   
#/ **********************************
#/ Check if prev installation exsist
#/ **********************************
	if [[ -f $MAS_INSTALLED ]]
	then
		#/ source file with the variable PREV_INSTALL_DIR
		. $MAS_INSTALLED  
		PREV_INSTALL_EXSIST="TRUE"
		echo "[$HOST $DATE] Previus installation found." >> $LOG_FILENAME_TMP

	fi

#/ **********************************
#/ Check silent parameter
#/ **********************************
if [[ $# = 1 ]] then
    if [[ $1 = -[Ss][Ii][Ll][Ee][Nn][Tt] ]]
    then
    	SILENT="SILENT"
    	echo "[$HOST $DATE] Using SILENT installation"
    else
    	echo "[$HOST $DATE] ERROR: Wrong parameter type"
    	exit 1
    fi
fi


#/ **********************************
#/ If silent
#/ **********************************
if [[ $SILENT = "SILENT" ]]
then
	
	#/echo "[$HOST $DATE] file: "$RESPONSE_FILE
	
	#/ **********************************
	#/ load responsefile
	#/ **********************************
	#/ check if response file exsists"
	if [[ -f $RESPONSE_FILE ]]
	then
		echo "[$HOST $DATE] Load response file ."
		. MAS_RESPONSE.cfg #/ $RESPONSEFILE
		echo "[$HOST $DATE] Installation using silent mode. " >> $LOG_FILENAME_TMP

	else
		#/ No responsefile. Use default values
		echo "[$HOST $DATE] Response file not exsists. Switching to Post configurated installation "
		POST_INST="YES"
		echo "[$HOST $DATE] Installation using post installation mode. " >> $LOG_FILENAME_TMP

		#/exit 1
	fi
else

	#/ **********************************
	#/ Create responsefile header
	#/ **********************************
       	echo "#/   *****************************************************" > $RESPONSE_FILE
	echo "#/   *                                                   *" >> $RESPONSE_FILE
	echo "#/   *  Variables in this file is used for silent        *" >> $RESPONSE_FILE
	echo "#/   *  install of MAS component                         *" >> $RESPONSE_FILE
	echo "#/   *                                                   *" >> $RESPONSE_FILE
	echo "#/   *  VARIABLES:                                       *" >> $RESPONSE_FILE
	echo "#/   *  INSTALL_DIR : The install dir of MAS Component.  *" >> $RESPONSE_FILE
	echo "#/   *  JAVA_HOME   : The install dir of java.           *" >> $RESPONSE_FILE
	echo "#/   *  REUSE       : If install reused configuration.   *" >> $RESPONSE_FILE
	echo "#/   *                                                   *" >> $RESPONSE_FILE
	echo "#/   *  This script was created : $DATE      *" >> $RESPONSE_FILE
	echo "#/   *  Install script version  : $VERSION                    *" >> $RESPONSE_FILE
	echo "#/   *                                                   *" >> $RESPONSE_FILE
       	echo "#/   *****************************************************" >> $RESPONSE_FILE
	echo "" >> $RESPONSE_FILE
	

	#/ **********************************
	#/ Ask for install dir
	#/ **********************************

	#/ IF previous install exsits. make sure of that install cant be done to another dir
   	if [[ ! $PREV_INSTALL_EXSIST = "TRUE" ]]
   	then
		echo
		echo " Default install dir $DEF_INSTALL_DIR "
		echo " Enter MAS install dir. 'Enter' for default : \c"
			
		read INSTALL_DIR
		
		if [ ! $INSTALL_DIR ]
		then
			
			INSTALL_DIR=$DEF_INSTALL_DIR
		fi
	else
	
		echo "Previous install exsist:" $PREV_INSTALL_DIR
		INSTALL_DIR=$PREV_INSTALL_DIR
	fi
fi

echo "[$HOST $DATE] Installation directory : $INSTALL_DIR " >> $LOG_FILENAME_TMP


#/ **********************************
#/ Check if parameters is valid
#/ **********************************

   #/ **********************************
   #/ Check  JAVA_HOME directory
   #/ **********************************

   #/echo 
   while true; do
   echo "[$HOST $DATE] Check java install dir :\c"
 
   if [ ! -d $JAVA_HOME  ]
   then
        echo " NOT FOUND"
        
        if [[ $SILENT = "SILENT" ]]
        then
        	#/ no java dir found. Dont update JAVA_HOME.
        	#/ This has to be done manualy.
        	#/ echo "Java install dir not found"
        	POST_INST="YES"	
        	break
        else
	   echo " Enter java install dir : \c"
	   read JAVA_HOME
	fi
   else
        echo " OK"
        echo "[$HOST $DATE] Java dir: OK " >> $LOG_FILENAME_TMP
        break
   fi

  done

  if [[ ! $SILENT = "SILENT" ]]
  then
     	echo "JAVA_HOME="$JAVA_HOME >> $RESPONSE_FILE
  fi

 
   #/ **********************************
   #/ Check install directry
   #/ **********************************
   #/ TODO - must check every directory if it exsist.
   
   # IF previous install exsists. make sure of that install cant be done to another dir
   if [[ $PREV_INSTALL_EXSIST = "TRUE" ]]
   then
     	INSTALL_DIR=$PREV_INSTALL_DIR
   fi
 
 
   echo "[$HOST $DATE] Check MAS install dir  :\c"
   if [ -d $INSTALL_DIR ] 
   then

      if [ ! -w $INSTALL_DIR ] 
      then
    	 echo "[$HOST $DATE] Not wtitable. ("$INSTALL_DIR")"
         echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
         doMasLog
    	 
	 exit 1
      fi

   else
    
      echo "[$HOST $DATE] Create installation directory. " >> $LOG_FILENAME_TMP

      #/ **********************************
      #/ Create install directory
      #/ **********************************
    
      #/    echo " Create :\c"
      mkdir $INSTALL_DIR 
      if [ $? -gt 0 ]
      then
      	echo "Error creating installdir"
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
      	doMasLog
      	exit 1
      fi
      mkdir $INSTALL_DIR/lib
      if [ $? -gt 0 ]
      then
      	echo "Error creating installdir"
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
      	doMasLog
      	exit 1
      fi
      mkdir $INSTALL_DIR/log
      if [ $? -gt 0 ]
      then
      	echo "Error creating installdir"
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
      	doMasLog
      	exit 1
      fi
      mkdir $INSTALL_DIR/tools
      if [ $? -gt 0 ]
      then
      	echo "Error creating installdir"
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
      	doMasLog
      	exit 1
      fi

      mkdir $INSTALL_DIR/applications
      if [ $? -gt 0 ]
      then
      	echo "Error creating installdir"
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
      	doMasLog
      	exit 1
      fi

  #    mkdir $INSTALL_DIR/applications/mediacontentpackages
  #    if [ $? -gt 0 ]
  #    then
  #    	echo "Error creating installdir"
  #    	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
  #    	doMasLog
  #    	exit 1
  #    fi
#
#      mkdir $INSTALL_DIR/applications/mediacontentpackages/en_audio_1
#      if [ $? -gt 0 ]
#      then
#      	echo "Error creating installdir"
#      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
#      	doMasLog
#      	exit 1
#      fi
#
#      mkdir $INSTALL_DIR/applications/mediacontentpackages/en_video_1
#      if [ $? -gt 0 ]
#      then
#      	echo "Error creating installdir"
#      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
#      	doMasLog
#      	exit 1
#      fi

      mkdir $INSTALL_DIR/backup
      if [ $? -gt 0 ]
      then
      	echo "Error creating installdir"
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
      	doMasLog
      	exit 1
      fi

   fi

   if [ -w $INSTALL_DIR ] 
   then
     	 if [[ ! $SILENT = "SILENT" ]]
	 then
      	 	echo "INSTALL_DIR="$INSTALL_DIR >> $RESPONSE_FILE
     	 fi
	 echo " OK"
         echo "[$HOST $DATE] Install dir: OK " >> $LOG_FILENAME_TMP

   else
	echo " FAILED."
      	echo "[$HOST $DATE] Creation of installdir failed. " >> $LOG_FILENAME_TMP
      	echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
	doMasLog
	exit 1
   fi
   
   
#/ **********************************
#/ Check if there is a previous installation
#/ **********************************

	#/if [ -r $INSTALL_DIR/log/$RESPONSE_FILE ] 
	if [[ $PREV_INSTALL_EXSIST = "TRUE" ]]
	then
		PREV_INSTALL="EXSIST"

		#/ ********************************************************
		#/ TODO-- add config files to be stored.
		#/ ********************************************************
		#/ store configfiles in safe location for later use
		
		if [ -r $INSTALL_DIR/log/$RESPONSE_FILE ] 
		then
			echo "[$HOST $DATE] Backup old response file to $INSTALL_DIR/backup " >> $LOG_FILENAME_TMP
			cp $INSTALL_DIR/log/$RESPONSE_FILE $INSTALL_DIR/backup/$RESPONSE_FILE
			if [ $? -gt 0 ]
			then
				echo "[$HOST $DATE] Error backing upp responsfile. " >> $LOG_FILENAME_TMP
  				ERROR="TRUE"
			fi
		
		fi

		
		#/ ********************************************************
		#/ If not silent , Ask if user wants to keep configfile in installdir
		#/ ********************************************************
		
		if [[ ! $SILENT = "SILENT" ]]
		then
		   #/ ********************************************************
		   #/ If not silent , Ask if user wants to keep configfile in installdir
		   #/ ********************************************************

		   while true;do
						
			echo " Reuse exsisting configuration ? Y/N :\c" 
			read ANSWER
			if [[ $ANSWER = [Yy] || $ANSWER = [Nn]  ]]
			then
				if [[ $ANSWER = [Yy] ]]
				then
					REUSE="YES"
					break
				else
					REUSE="NO"
					break
				fi
			fi
			
		   done		
		   echo "REUSE="$REUSE >> $RESPONSE_FILE
		   
		#/else
  		   #/ ********************************************************
		   #/ If silent , Reuse value is red from responsefile
		   #/ ********************************************************
		fi
	else
		PREV_INSTALL="NOT_EXSIST"
	fi


#/ **********************************
#/ Check enviroment
#/ **********************************


echo "[$HOST $DATE] Checking space         : OK"

#/*******************************************
#/ TODO: check for req space..
#/*******************************************

echo "[$HOST $DATE] Unpack lib.            : \c"

cd $INSTALL_DIR/lib
tar xf $DIRNAME/MAS_drop.tar >> $LOG_FILENAME_TMP
if [ $? -gt 0 ]
then
   echo "Error unpacking lib."
   echo "[$HOST $DATE] Error unpacking lib. " >> $LOG_FILENAME_TMP
   echo "[$HOST $DATE] Installation failed. " >> $LOG_FILENAME_TMP
   
   doMasLog
   exit 1
else
   echo "OK"
fi

chmod 755 *.so
#/*******************************************
#/ packa upp jar fil. starta från $INSTALL_DIR
#/ check for errors
#/*******************************************


echo "[$HOST $DATE] Unpack tools.          : \c"
#/*******************************************
#/unpack scripts/tools
#/*******************************************

cd $INSTALL_DIR/tools
if [ $? -gt 0 ]
then
  ERROR="TRUE"
fi

tar xf $DIRNAME/MAS_tools.tar >> $LOG_FILENAME_TMP
if [ $? -gt 0 ]
then
  ERROR="TRUE"
  echo "Error."
else
  echo "OK"
fi



echo "[$HOST $DATE] Update $RCCONF_FILE     : \c"

#/*********************************************
#/* If no conf file exsist. Create conf file with default values
#/*********************************************
if [[ ! -f $INSTALL_DIR/tools/$RCCONF_FILE ]]
then
	echo "# Configurationfile for RC.MAS script." >  ${INSTALL_DIR}"/tools/rc.mas.conf"
	echo "INSTALL_DIR=#__INSTALL_DIR__#" >> ${INSTALL_DIR}"/tools/rc.mas.conf"
	echo "JAVA_HOME=#__JAVA_HOME__#" >> ${INSTALL_DIR}"/tools/rc.mas.conf"
	echo "INIT_HEAP_SIZE=128M" >> ${INSTALL_DIR}"/tools/rc.mas.conf"
	echo "MAX_HEAP_SIZE=256M" >> ${INSTALL_DIR}"/tools/rc.mas.conf"
	if [ $? -gt 0 ]
	then
  		ERROR="TRUE"
  		echo "Error."
  		echo "[$HOST $DATE] Error updating rc.mas configuation file." >> $LOG_FILENAME_TMP
  	fi
fi


#/*******************************************
#/ update rc.mas med installdir
#/*******************************************
sed s!#__INSTALL_DIR__#!"${INSTALL_DIR}"!g ${INSTALL_DIR}"/tools/rc.mas.conf" > rc.mas.conf2
mv rc.mas.conf2 rc.mas.conf
if [ $? -gt 0 ]
then
  ERROR="TRUE"
  echo "Error."
  echo "[$HOST $DATE] Error backing up rc.mas configuation file." >> $LOG_FILENAME_TMP
  echo "[$HOST $DATE] Error backing up rc.mas configuation file."
  
fi


sed s!#__JAVA_HOME__#!"${JAVA_HOME}"!g ${INSTALL_DIR}"/tools/rc.mas.conf" > rc.mas.conf2
mv rc.mas.conf2 rc.mas.conf
chmod 755 rc.mas
if [ $? -gt 0 ]
then
  ERROR="TRUE"
  echo "Error."
  echo "[$HOST $DATE] Error updating rc.mas configuation file." >> $LOG_FILENAME_TMP
else
  echo "OK"
fi



echo "[$HOST $DATE] Installing default application : \c"

cd $INSTALL_DIR/applications
if [ $? -gt 0 ]
then
  ERROR="TRUE"
  echo "[$HOST $DATE] Error moving to $INSTALL_DIR/applications " >> $LOG_FILENAME_TMP
fi

cp -fr $DIRNAME/applications/* . >> $LOG_FILENAME_TMP
if [ $? -gt 0 ]
then
  echo "Error."
  ERROR="TRUE"
else
  echo "OK"
fi



#/ ********************************************************
#/ if the user vants to keep prev config. copy them back from backup dir
#/ ********************************************************
     if [[ $REUSE = "YES" ]]
     then
	#/ TODO Dont know filenames.
	echo "[$HOST $DATE] Reuse exsisting configuration." >> $LOG_FILENAME_TMP
	echo "[$HOST $DATE] Reuse exsisting configuration"
     fi

#/ ********************************************************
#/ Copy responsefile to log directory
#/ ********************************************************
#/echo "INSTALL_STATUS=SUCCESS" >> $RESPONSE_FILE 

echo "[$HOST $DATE] Copy responsfile to log dir    : \c"
if [ -r $DIRNAME/$RESPONSE_FILE ] 
then
	cp $DIRNAME/$RESPONSE_FILE $INSTALL_DIR/log/$RESPONSE_FILE
        if [ $? -gt 0 ]
        then
        	echo "Failed"
        else
        	echo "OK"
        fi
fi


#/ ********************************************************
#/ Create a cfg file containing the install dir
#/ ********************************************************
   echo "[$HOST $DATE] Check directory $MAS_INSTALLED_DIR          : \c"
   if [ -d $MAS_INSTALLED_DIR ] 
   then

      if [ ! -w $MAS_INSTALLED_DIR ] 
      then
      	 #/ cant create MAS_INSTALLED.cfg file. Installaction can't determine if mas was installed before.
      	 echo "[$HOST $DATE] Directorey $MAS_INSTALLED_DIR is not writable. Unable to create $MAS_INSTALLED." >> $LOG_FILENAME_TMP
    	 echo "Not writable."
      else
      	 echo "OK."
	 echo " #/This is a file that keep track of MAS Installation dir" > $MAS_INSTALLED
	 echo " PREV_INSTALL_DIR="$INSTALL_DIR >> $MAS_INSTALLED
      
      fi
   else
   	echo "Not exsist."
   fi

#/ make logfile

   
   
#/ ********************************************************
#/ Register MAS component into MCR  TODO...
#/ ********************************************************

if [[ $ERROR = "TRUE" ]]
then
	echo "[$HOST $DATE] Some errors ocured during installation. "
	echo "[$HOST $DATE] Some errors ocured during installation. " >> $LOG_FILENAME_TMP
else
	echo "[$HOST $DATE] Installation completed successfully."
	echo "[$HOST $DATE] Installation completed successfully." >> $LOG_FILENAME_TMP

fi

doMasLog
echo ""

if [[ $POST_INST = "YES" ]]
then
	echo "   ---  The installation may NOT be fully configured. --- "
	echo 
	echo "   1. Old configuration was saved under backup before installation. "
	echo "      If previus configuration is to be used. copy it from backup directorey. "
	echo 
	echo "   2. Parameters for rc.mas may not have ben initiated correctly. "
	echo "      Update parameters in rc.mas.conf file. "
fi	
echo

	











