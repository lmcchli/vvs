#!/bin/sh

#------------------------------------------------------------------------#
# makerelease.sh
#----------------
# This script will use the m4e-relpom plugin to create release-pom files
# for all MoIP release artifacts.
#
# The version information is extracted from the u4e baseline.
#
# For now, the core version is extracted from the parent pom and appended
# to the MoIP version to aid installers to know which version of CORE to
# install with this MoIP release
#----------------
# Author: lmcjhut
#------------------------------------------------------------------------#

RELEASE_BASE_DIR=/project/moipsoft/component/release
MOIPABS_DIR=.
PARENT_POM=$MOIPABS_DIR/pom.xml
TEMP_POM=$MOIPABS_DIR/pom-temp.xml
MessageCoreVersion=""
MoipVersion=""
Version=""
MOIPRelease=""

getVersion(){
    #===========================================================================#
    #  If the release version was not passed in as parameter, then 
    #  It is computed by combining the latest MoipVersion with the CoreVersion
    #===========================================================================#

    if [ "$MOIPRelease" == "" ]
    then
        getMessageCoreVersion
        getMoipVersion
        MOIPRelease=$MoipVersion"_$MessageCoreVersion"
    fi
}

getMoipVersion(){
    cp -f $PARENT_POM $TEMP_POM
    dos2unix $TEMP_POM
    MessageCoreVersion=`grep "<project.version>" $TEMP_POM | sed 's/<project.version>//' |sed 's/<\/MessageCore.version>//' | sed -e 's/^[ \t]*//'`
    echo Using CORE relese defined in parent POM : "$MessageCoreVersion"
    \rm -f $TEMP_POM
}

getMessageCoreVersion(){
    cp -f $PARENT_POM $TEMP_POM
    dos2unix $TEMP_POM
    MessageCoreVersion=`grep "<MessageCore.version>" $TEMP_POM | sed 's/<MessageCore.version>//' |sed 's/<\/MessageCore.version>//' || sed -e 's/^[ \t]*//'`
    echo Using CORE relese defined in parent POM : "$MessageCoreVersion"
    \rm -f $TEMP_POM
}

populateReleaseDirectory(){
    echo Release will be "[$MOIPRelease]"
    RELEASE_DIR=$RELEASE_BASE_DIR/$MOIPRelease
    echo  "creating directory $RELEASE_DIR"

    mkdir $RELEASE_DIR
    cp $MOIPABS_DIR/iso/target/*.iso $RELEASE_DIR

    mkdir $RELEASE_DIR/sdps
    cp $MOIPABS_DIR/iso/target/iso/MOIP/products/*.sdp $RELEASE_DIR/sdps 
    cp $MOIPABS_DIR/iso/target/iso/MOIP/cfg/COMPONENT_LIST.conf $RELEASE_DIR/sdps
    find $RELEASE_DIR
}

listAll(){
    echo -e "\n=============================================================="
    echo -e "Here is the list of existing Releases: "
    echo -e "In release directory: $RELEASE_BASE_DIR"
    echo -e "==============================================================\n"
    echo -e " "
    ls -altr $RELEASE_BASE_DIR  |grep "^d"
    exit 0
}


getArgs(){
    echo -e " Inside of getArgs() parameter 1 = $1"
    case "$1" in 
        'listAll')
            listAll
            ;;
        *)
           MOIPRelease=$1
           echo "MOIPRelease=$MOIPRelease"
           ;;
    esac
}

echo -e "\n=============================================================="
echo -e "Deploying Release POM files to produce an official MOIP Release"
echo -e "==============================================================\n"
echo -e " parameter 1 = $1"

PROCESSOR=`uname -p`

if [ $PROCESSOR == "unknown" ]; then
	echo "deploy is not supported in windows"
	exit 1
fi

getArgs $1
getVersion
populateReleaseDirectory
createDeliveryAnnouncement

