#!/bin/ksh

#================================================================================"
# This script will:
# create Broadcast Announcement
#   or
# populates a Broadcast Announcement list.
#
#    The primary reason for this script is for the system level Announcements that
#    need to span multiple OpCOs. All other Announcement types are supported, but
#    they can also be created in the PA GUI or VIA the CAI3G interface.
#
#  INPUT
#    The OpCOs will be configured in a configuration file  (opco.cfg)
#    The BA information will be provided in an input file in CAI3G format.
#    The list of msisdns must be provided for the case of populating a list
#        - the filename must be the same root as the ba name.
#
#================================================================================"
#-----  -----#
TEST_MODE=false
TEST_STUB_FILE="./createBA_test.ksh"

DELETE=false
OPCO_FILE="opco.cfg"
ALTERNATE_OPCO_PATH="/opt/moip/common/"
BA_INFILE=""
BATCHPROVISIONER="/opt/msgcore/batchprovisioner/bin/batchprovisioner.sh"
RETURN_VALUE=""
MEDIA_DIRECTORY="/opt/mio/common/broadcastAnnouncements"
CODEC_EXT="*"
INDENT="    "
NL_INDENT="${INDENT}"
BIG_INDENT="            "

BA_NAME=""
BA_MUID=""
BA_AUDIENCE=""
LANGUAGE_LIST=""
BA_COS=""
HOST=""
OPCO=""
SCOPE=""

RESULT_LOG="ba.log"
ERROR_LOG="ba_error.log"

# Provisioning user name and password (if not specified use the default from batchprovisioner)
USER=""
PASSWORD=""

#---- Template Files ----#
LIST_TEMPLATE="list_template.ldif"
TEMPLATE_FILE="ba_template.dat"
DELETE_TEMPLATE_FILE="delete_ba_template.dat"
OPCO_TEMPLATE="opco_template.cfg"

#---- Command Line Parameter Names ----#
PARAMNAME_MUID="muid"
PARAMNAME_NAME="name"
PARAMNAME_FILE="file"
PARAMNAME_HOST="host"
PARAMNAME_OPCO="opco"
PARAMNAME_POPULATE="populateList"
PARAMNAME_DELETE="delete"
PARAMNAME_USER="user"
PARAMNAME_PASSWORD="password"

#----set DEBUG to "true" to have log statements on stdout ----#
DEBUG=false

#---------------------------------------------------------------------#
# init
#    Initialize - process cmdline args, get Audience ...
#---------------------------------------------------------------------#
init(){

    printLog "Inside of init()"

	#----- First things first - ensure that the batchprovisioner is installed on this machine -----#
	if [[ ! -f $BATCHPROVISIONER ]]; then
	    echo "ERROR:  unable to locate file [$BATCHPROVISIONER]"
	    echo "Please ensure that you are running on the da node."
	    echo "Please try running on the `grep da-00 /cluster/cfg/SERVER_LIST.conf |awk '{print $1}'` node"
	    echo ""
	    exit 1
	fi

    #----- Process the command line parameters  -----#
	getCmdLineArgs $@

    #----- If in test mode, source the script to get some test methods available  -----#
    if [[ "$TEST_MODE" == "true" ]]; then
        if [[ -f $TEST_STUB_FILE ]];then
            source $TEST_STUB_FILE
            setupTestEnv
        fi
    fi

    printLog "DELETE=$DELETE"

    if [[ "$DELETE" = "true" ]]; then
        deleteBA
    fi

    if [[ "$POPULATE" = "true" ]]; then
        printLog "Calling populateBAList"
        populateBAList
    fi

    #----- 1. There has to be an input file set -----#
    if [[ "$BA_INFILE" == "" ]]; then
        printError "You must provide an input file for the broadcast announcement operation. Please retry proving -$PARAMNAME_FILE parameter"
        usage
    fi

    #-----2. Extract the BA Name and Audience from the user's input file to be sure it is set -----#

    if [[ -f "$BA_INFILE" ]];  then
        BA_AUDIENCE=`grep Audience $BA_INFILE |sed 's/MOIPBroadcastAnnouncementAudience=//'`
        BA_NAME=`grep "ba:" $BA_INFILE |sed 's/ba://'`
        printLog  " Setting Audience to $BA_AUDIENCE"
        printLog  " Setting name to $BA_NAME"
    else
        printError "Provided input file does not exist [$BA_INFILE] "
        usage
    fi

	#----- 3: There must be at least one media file deployed for this BA -----#
    validateBAMedia

	#----- 4. Now backup the log files since we will probably overwrite then -----#
	backupLogFiles

    printSettings
}

#---------------------------------------------------------------------#
# backupLogFiles
#    Backup the 2 log files since the operation will overwrite them.
#---------------------------------------------------------------------#
backupLogFiles() {
	echo "DATE_STR = $DATE_STR"
	if [[ -f $RESULT_LOG ]] ; then
	     cp -f $RESULT_LOG ${RESULT_LOG}-backup
	fi
	if [[ -f $ERROR_LOG ]] ; then
	     cp -f $ERROR_LOG ${ERROR_LOG}-backup
	fi

}

printSettings() {
	OPERATION="Creating"

	if [[ "$DELETE" == "true" ]]; then
	    OPERATION="Deleting"
	fi

    echo " "

    if [[ "$BA_NAME" != "" ]]; then
        echo "${INDENT}${OPERATION} $BA_AUDIENCE Broadcast Announcement : $BA_NAME"
    fi

    if  [[ "$BA_INFILE" != "" ]]; then
        echo "${INDENT} - Using input file $BA_INFILE "
    fi

    if [[ "$HOST" != "" ]]; then
        echo "${INDENT} - Creating announcement on host $HOST "
    fi
    echo " "
}

#---------------------------------------------------------------------#
# getCmdLineArgs
#    Process command line arguments, if any.
#---------------------------------------------------------------------#
getCmdLineArgs() {
    printLog "Inside of getCmdLineArgs()"
    while [[ $# -gt 0 ]]
    do
        PARAM=$1
        shift
        case $PARAM in
            "-${PARAMNAME_MUID}")
            BA_MUID=$1
            printLog "BA_MUID set to $BA_MUID"
            shift
            ;;

            "-${PARAMNAME_NAME}")
            BA_NAME=$1
            printLog "BA_NAME set to $BA_NAME"
            shift
            ;;

            "-${PARAMNAME_FILE}")
            BA_INFILE=$1
            printLog "BA_INFILE set to $BA_INFILE"
            shift
            ;;

            "-${PARAMNAME_HOST}")
            HOST=$1
            printLog "Parameter host is set to $HOST"
            shift
            ;;

            "-${PARAMNAME_OPCO}")
            OPCO="$1"
            # SYSTEM is reserved for deletion of SYSTEM based BA.
            if [[ "$OPCO" != "SYSTEM" ]]; then
                getOpcoAddr $OPCO
                HOST="$RETURN_VALUE"
            fi
            printLog "Parameter host is set to $HOST"
            shift
            ;;

            "-${PARAMNAME_POPULATE}")
            printLog "-populateList found"
            POPULATE=true
            ;;

            "-${PARAMNAME_DELETE}")
            printLog "-delete found"
            DELETE=true
            ;;
            
            "-${PARAMNAME_USER}")
            USER=$1
            shift
            printLog "User set to ${USER}"
            ;;
            
            "-${PARAMNAME_PASSWORD}")
            PASSWORD=$1
            shift
            printLog "Password set to ${PASSWORD}"
            ;;
            
            '-debug')
            DEBUG=true
            ;;

            '-backup_logs')
            backupLogFiles
            exit 0
            ;;

            '-test')
            printLog "Running in test mode"
            TEST_MODE=true
            ;;

            "?" | '-h'|'help'|'-help')
            usage -long
            ;;
        esac
    done
}

#---------------------------------------------------------------------#
# deleteBA
#    Delete a broadcast announcement from the system.
#    If the OPCO parameter is passed as "SYSTEM" we delete the
#    BA from all OPCOs in the OPCO list.
#---------------------------------------------------------------------#
deleteBA() {

	if [[ "$BA_MUID" == "" ]]; then
	    printError "You must provide the muid of the Broadcast Announcement profile for the delete operation"
	    usage
	    exit
	fi

    echo -n "Are you sure that you want to delete Broadcast Announcement $BA_MUID on host $HOST?[y/n] "
    read reply
    if [[ "$reply" != "y" ]]; then
        echo "Deletion aborted by user."
        exit
    fi

    #---- Create the input file that will be used to delete the BA  ----#
    BA_INFILE=delete_${BA_MUID}.dat
    cat $DELETE_TEMPLATE_FILE | sed "s/<ENTER BA MUID HERE>/$BA_MUID/" > $BA_INFILE

    if [[ "$OPCO" == "SYSTEM" ]]; then

        #---- If the user requested deletion for the entire SYSTEM then loop through each OPCO  ----#
        getOpcos
        printLog "Deleting System Broadcast "
        for opco in $OPCO_LIST
        do
        	echo "Deleting broadcast announcement $BA_MUID from OPCO $opcoName"
            HOST=`echo $opco| awk -F'=' '{print $2}'`
        	printComment "Processing OPCO $opco at addr $HOST"
    	    processBroadcastAnnouncement
        done
    else
        #---- If the user did not request deletion for the entire SYSTEM then just delete on currently configured OPCO or HOST  ----#
        processBroadcastAnnouncement
    fi

    #----- cleanup the delete file we created -----#
    if [[ -f $BA_INFILE ]]; then
        rm  $BA_INFILE  2> /dev/null
    fi

    exit 0
}

#---------------------------------------------------------------------#
# processBAResult
#    process result of the batchprovisioner
#---------------------------------------------------------------------#
processBAResult() {

	SUCCESS=`grep "Success =" $RESULT_LOG`
	FAILURE=`grep "Failed =" $RESULT_LOG`
    if [[ "$SUCCESS" == "" ]]; then
	    printError "Operation Failed:"
	    cat $ERROR_LOG |grep "Caused by"
	    echo "\nFor more information, refer to $RESULT_LOG and $ERROR_LOG"
	else
		echo "Your request succeeded.  For more information, please refer to ba.log"
	fi

}

#---------------------------------------------------------------------#
# usage
#    Show the user how to use the script..
#---------------------------------------------------------------------#
usage() {
    if [[ "$1" != "" ]]; then
        if [[ "$1" != "-long" ]]; then
            printError "$1"
        fi
    fi

    echo "USAGE:"
    echo "------"
    echo "createBA.ksh:  [-${PARAMNAME_FILE} <filename>] [-${PARAMNAME_OPCO} <opco>] [-${PARAMNAME_HOST} <host>] [-${PARAMNAME_DELETE}] [-$PARAMNAME_MUID <ba_muid>] [-${PARAMNAME_POPULATE}] [-$PARAMNAME_NAME <ba_name>] [-$PARAMNAME_USER <user>] [-$PARAMNAME_PASSWORD <password>] [-help|-h|?]"
    echo "\nWhere:"
    echo "${INDENT}<filename> is the ldap file containing the Broadcast Announcement details used for creation. Please refer to the provided template file."
    echo "${INDENT}<opco> is the name of the OPCO that will be used."
    echo "${INDENT}<host> is the name of the HOST that will be used."
    echo "${INDENT}<ba_muid> is the muid of the Announcement to be used for the ${PARAMNAME_DELETE} command."
    echo "${INDENT}<ba_name> is the name of the Announcement to be used for the ${PARAMNAME_POPULATE} command."
    echo "${INDENT}-${PARAMNAME_DELETE} will delete a Broadcast Announcement."
    echo "${INDENT}-${PARAMNAME_POPULATE} will populate a Broadcast Announcement list to all required subscribers. (the Broadcast Announcement should already be created)"
    echo "${INDENT}-${PARAMNAME_USER} will specify the provisioning user name."
	echo "${INDENT}-${PARAMNAME_PASSWORD} will specify the provisioning user password."
    echo "${INDENT}-help or -h  or ? will provide a detailed help page."
    echo ""

    #---- If the request is for long help, then add detailed description ----#
    if [[ "$1" == "-long" ]]; then

	    echo "========================================="
	    echo " Creating a Broadcast Announcement "
	    echo "========================================="
	    echo ""
	    echo "Syntax: createBA.ksh: -$PARAMNAME_FILE <filename>"
	    echo "${INDENT}Where the <filename> is the input data that defines the settings of the announcement."
	    echo ""
	    echo "When creating a Broadcast Announcement, an input file is required that defines the settings of the broadcast announcement to be created."
	    echo "The ${TEMPLATE_FILE} is provided as an example input file.  The template file includes comments to explain the syntax."
	    echo "Here is an explanation of parameters that can be set in the input file"
	    echo ""
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementPlayOnce=[yes|no]"
	    echo "$BIG_INDENT Configure BA to only be played once to the subscriber (Optional). - Default is no."
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementDescription=<DESCRIPTION>"
	    echo "$BIG_INDENT This a textual description for the Broadcast Announcement (Optional)."
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementStartDate=<DATE>"
	    echo "$BIG_INDENT This is the start date of the Broadcast Announcement (Mandatory).  Format = YYYY-MM-DD HH:MM"
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementEndDate=<DATE>"
	    echo "$BIG_INDENT This is the end date of the Broadcast Announcement (Mandatory).  Format = YYYY-MM-DD HH:MM"
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementAudience=[SYSTEM|OPCO|COS|LIST]"
	    echo "$BIG_INDENT This is the intended audience for the Broadcast Announcement (Mandatory). Must be one of :"
	    echo "${BIG_INDENT} - SYSTEM\tCreate a Broadcast Announcement that will be played to all subscribers in all OPCOs."
	    echo "${BIG_INDENT} - OPCO\tCreate a Broadcast Announcement that will be played to all subscribers in a given OPCO."
	    echo "${BIG_INDENT} - COS\tCreate a Broadcast Announcement that will be played to all subscribers who belong to a given."
	    echo "${BIG_INDENT} - LIST\tCreate a Broadcast Announcement that will be played to all subscribers of a discreet list."
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementCos=<COS>"
	    echo "$BIG_INDENT This is the COS  (Optional but required when Audience is set to COS). Enter one line for each COS. "
	    echo "${NL_INDENT} MOIPBroadcastAnnouncementPriority=<PRIORITY>."
	    echo "$BIG_INDENT This is the priority for the Broadcast Announcement (Optional)."
	    echo ""
	    echo "========================================="
	    echo " Deleting a Broadcast Announcement."
	    echo "========================================="
        echo "Use the ${PARAMNAME_DELETE} option to remove a broadcast announcement."
	    echo ""
	    echo "Syntax: createBA.ksh -${PARAMNAME_DELETE} -$PARAMNAME_MUID <ba_muid> [-${PARAMNAME_OPCO} <opco>]"
	    echo "${INDENT}Where the <ba_muid> is the muid of the broadcast announcement to remove."
	    echo "${INDENT}and <opco> is the opco that has the announcement. If empty, use localhost; setting to SYSTEM removes the announcement from all OPCOs."
	    echo ""
	    echo "for example. To delete the dailyBA announcement from all OPCOs :"
	    echo "${INDENT}createBA.ksh: -${PARAMNAME_DELETE} -$PARAMNAME_MUID muidstring -${PARAMNAME_OPCO}=SYSTEM"
	    echo ""
        echo "========================================="
	    echo " Populating a list of subscribers to a BA."
	    echo "========================================="
	    echo "Use the ${PARAMNAME_POPULATE} option to assign a broadcast announcement to be played to a list of subscribers."
	    echo "The list of subscribers must be provided in a file which has the same name as the broadcast announcement, with a \"list\" extension"
	    echo ""
	    echo "Syntax: createBA.ksh: -${PARAMNAME_POPULATE} -$PARAMNAME_NAME <ba_name> "
	    echo ""
	    echo "for example:"
	    echo "${INDENT}createBA.ksh: -${PARAMNAME_POPULATE} -$PARAMNAME_NAME dailyBA "
	    echo "${BIG_INDENT} - dailyBA is the name of an existing Broadcast Announcement"
	    echo "${BIG_INDENT} - there must be a file in the current directory named dailyBA.list which contains a list of subscriber msisdn who will hear this announcement."
	    echo ""
	    echo "NOTE: When populating a list, the Broadcast Announcement should already be created, and must have an AUDIENCE of type LIST."
	    echo ""

    fi
    exit
}

#---------------------------------------------------------------------#
# getOpcos(){
#   Expected Format of this file is:
#     <opconame>=<opco-ip>
#---------------------------------------------------------------------#
getOpcos(){

	#----- Retrieve the opco list if it has not already been retrieved -----#
    if [[ "$OPCO_LIST" == "" ]]; then
        if [[ ! -f $OPCO_FILE ]]; then
            if [[  -f "$ALTERNATE_OPCO_PATH/$OPCO_FILE" ]]; then
                #So lets use the alternate opc file path since
                OPCO_FILE="$ALTERNATE_OPCO_PATH/$OPCO_FILE"
            else
                printError "No valid OPCO configuration file($OPCO_FILE) was found in current directory or in $ALTERNATE_OPCO_PATH directory"
                usage
            fi
        fi

        OPCO_LIST=`cat $OPCO_FILE`
        printLog "Found Opco list : $OPCO_LIST"
    fi

}

#---------------------------------------------------------------------#
# getOpcoAddr(){
#   Search for an opco and return its address field
#---------------------------------------------------------------------#
getOpcoAddr() {

    #----- Get the opco list -----#
    getOpcos

    opcoName=$1
    RETURN_VALUE=""

    getOpcos

    if [[ "$opcoName" != "" ]]; then
        opco=`echo $OPCO_LIST | grep $opcoName`
        if [[ "$opco" != "" ]]; then
            RETURN_VALUE=`echo $opco| awk -F'=' '{print $2}'`
            printComment "getOpco() : Retrieved OPCO $opcoName at addr $opcoAddr"
        else
            printComment "ERROR - opco $opcoName not found in config file $OPCO_FILE"
        fi
    else
        printComment "ERROR - empty parameter to getOpco() call."
    fi
}

#---------------------------------------------------------------------#
# processBroadcastAnnouncement(){
#   Create or delete a broadcast announcement by calling the batchprovisioner.
#   Here are the rules on which host to use:
#      - Use the second parameter as host address if it has been provided
#      - If there is no host passed into this method, but HOST variable is set (from cmdline) then use it.
#      - If there is no host configured at all, do not pass as parameter.
#
# NOTE:
#    $BA_INFILE will be used as the batch file.  It is provided by the
#    user when creating a BA and created by this script when deleting.
#
# NOTE2:  If processList has been requested, then we use a template
#         based commandline for the batchprovisioner.
#		  Otherwise we use the stnadard batchfile for create and delete
#
#---------------------------------------------------------------------#
processBroadcastAnnouncement(){

    printLog "Inside of processBroadcastAnnouncement"
    cmdline=""

	#----- Default is empty host parameter which means use localhost -----#
	hostParam=""
	
	#---- Credential for batchprovisioner -----#
	typeset credential=""

	#----- Determine host to pass to batchprovisioner (if any) -----#
    if [[ "$HOST" != "" ]]; then
        # If there is no host passed into this method, but HOST variable is set (from cmdline) then use it.
        hostParam="host=$HOST"
    fi


	if [[ "$POPULATE" != true ]]; then
	    cmdline="batchfile=$BA_INFILE profileclass=broadcastannouncement ldif=false"
	else
        cmdline="template=$LIST_TEMPLATE file=$RANGE_FILE"
	fi
	
	if [[ -n $USER ]]; then
		credential="user=$USER"
	fi
	
	if [[ -n $PASSWORD ]]; then
		credential="$credential password=$PASSWORD"
	fi
	
    if [[ "$DEBUG" == true ]]; then
        echo "$BATCHPROVISIONER $cmdline $hostParam $credential 2> $ERROR_LOG 1> $RESULT_LOG"
    else
        echo 'y' | $BATCHPROVISIONER $cmdline $hostParam $credential 2> $ERROR_LOG 1> $RESULT_LOG
    fi

    processBAResult
}

#---------------------------------------------------------------------#
# createOpcoBA(){
#   Create a broadcast announcement for a selected OpCO
#
# Parameters:
#   $1 - opcoName (optional): Name of the Opco
#   $2 - opcoAddr (optional): Address of the Opco
#        If the opco Address is not provided then we look it up in the
#        OPCO_LIST. The HOST variable is used to pass OPCO address to
#        the processBroadcastAnnouncement.
#---------------------------------------------------------------------#
createOpcoBA(){

    printLog "Creating Opco Broadcast "
    processBroadcastAnnouncement
}

#---------------------------------------------------------------------#
# createSystemBA(){
#   Create a broadcast announcement for the entire system.
#   This means that we create the same BA for all configured OpCOs.
#---------------------------------------------------------------------#
createSystemBA() {
    opcoName=""
    opcoAddr=""

    #----- Get the opco list -----#
    getOpcos

    #----- 3: If we are SYSTEM, then the OPCO list must have at least one entry -----#
    #if [[ "$OPCO_LIST" == "" ]]; then

    printLog "Creating System Broadcast "
    for opco in $OPCO_LIST
    do
       	echo "Creating broadcast announcement $BA_NAME from OPCO $opcoName"
        HOST=`echo $opco| awk -F'=' '{print $2}'`
    	printComment "Processing OPCO $opco at addr $HOST"
    	processBroadcastAnnouncement
    done
}

#---------------------------------------------------------------------#
# createCosBA(){
#   Create a broadcast announcement for a given COS only.
#---------------------------------------------------------------------#
createCosBA(){
    printHeading "Creating Cos Broadcast "
    processBroadcastAnnouncement
}

#---------------------------------------------------------------------#
# createListBA(){
#   Create a broadcast announcement for a operator defined list
#   of subscribers.
#---------------------------------------------------------------------#
createListBA(){
    printHeading "Creating List Broadcast "
    processBroadcastAnnouncement
}

#---------------------------------------------------------------------#
# populateBAList(){
#   Populate all subscribers of a BA List
#---------------------------------------------------------------------#
populateBAList(){
	POPULATE_LIST=true
	RANGE_FILE="${BA_NAME}.list"

    printLog "Inside of populateBAList()"

    #----- Step 1:  Verify that there is an msisdn list provided by user ----#
    if [[ ! -f $RANGE_FILE ]] ; then
        printError "You must provide a file named $RANGE_FILE that contains list of subscriber msisdn to populate.\n Aborting ..."
        exit 0
    fi

    #----- Step 2:  Create the list template and then populate the BA ----#
	createListTemplate
	processBroadcastAnnouncement

	exit 0
}

#---------------------------------------------------------------------#
# createBAByType(){
#   Create the BA based on the Audience type.
#---------------------------------------------------------------------#
createBAByType(){
    TYPE="$1"
    printLog "Creating Broadcast Announcement of type [$TYPE]"

    case "$TYPE" in
         'SYSTEM')
         printLog "Handling SYSTEM type"
         createSystemBA
         ;;
         'OPCO')
         printLog "Handling OPCO type"
         createOpcoBA
         ;;
         'COS')
         printLog "Handling COS type"
         createCosBA
         ;;
         'LIST')
         printLog "Handling LIST type"
         createListBA
         ;;
    esac

}

#---------------------------------------------------------------------#
# validateBAMedia(){
#---------------------------------------------------------------------#
#   Check to ensure that the BA media file(s) is present.
#   The rule is simple.  The media has to have the same name as the
#   baname, and then appends the language.
#
#   If there are media files available, create the language list.
#
#   Will find all files related to BA and construct the language list.
#---------------------------------------------------------------------#
validateBAMedia() {
    LANGUAGE_LIST=""
    SEPARATOR=" "

	#----- First, see whether the media directory exists. If not, create it. -----#
    if [[ ! -d ${MEDIA_DIRECTORY} ]]; then
        su -p mmas -c "mkdir -p ${MEDIA_DIRECTORY}"
        echo "Created directory ${MEDIA_DIRECTORY}.  Please copy your media file(s) there."
        exit 1
    fi

	#----- There must be media files that match the BA name -----#
    printLog "Looking for associated media file in ${MEDIA_DIRECTORY}"
    LANGUAGE_LIST=`ls ${MEDIA_DIRECTORY}/${BA_NAME}*.${CODEC_EXT} 2>/dev/null`
    printLog "LANGUAGE_LIST=$LANGUAGE_LIST"
    if [[ "$LANGUAGE_LIST" == "" ]] ; then
        echo "There are no media files provided in the directory [$MEDIA_DIRECTORY] for [$BA_NAME] \n"
        echo "Please upload the required media files to this directory and try again.\n"
        exit 1
    fi

}

printHeading(){
    printCommand ""
    printCommand "#====================================================="
    printCommand "#    $1"
    printCommand "#====================================================="
    printCommand ""
}
printCommand(){
    echo "$1"
}
printComment(){
    echo "$1"
}
printError(){
    echo "[ERROR : $1] \n"
}
printLog() {
    if [[ "$DEBUG" == "true" ]] ; then
    	echo "$1"
    fi
}

#---------------------------------------------------------------------#
# createListTemplate(){
#---------------------------------------------------------------------#
#   Create the CAI3G input file for deletion of the BA.
#---------------------------------------------------------------------#
createListTemplate() {
echo "
version:1
dn:identity=tel:\$ntel,ou=subscriber,o=opco1
changetype:modify
-
add:MOIPBroadcastAnnouncements
MOIPBroadcastAnnouncements:$BA_NAME
" > $LIST_TEMPLATE

}

#---------------------------------------------------------------------------------"
# Cleanup any temporary files that were created during the run.
#---------------------------------------------------------------------------------"
cleanupTemporaryFiles() {
    printLog "Inside of cleanupTemporaryFiles()"
}

printOpenningScreen() {

    clear
	echo "==============================================="
	echo "Broadcast Announcement"
	echo "==============================================="
	echo " "
}

#---------------------------------------------------------------------------------"
# 				Main method
#---------------------------------------------------------------------------------"
main() {
    printOpenningScreen
    init $@
    createBAByType $BA_AUDIENCE
}


main $@

