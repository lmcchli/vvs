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

INSTALL="FALSE"
DEPLOY="FALSE"
SKIPTEST=""
LatestBuild=""
ABS_DIR=`pwd`
CUR_DIR=`pwd`
BUILD_DIR=`pwd`
MIO_DIR=$CUR_DIR/../../mio
VVSAPPS_DIR=$CUR_DIR/../../vvsapps

# ================================================================ #
# getCmdLine()
#     method to process command line args and check the 
#     possible options - INSTALL (run mvn) and deploy to vob.  
# ================================================================ #
getCmdLine () {
    while [ $# -ne 0 ]
    do
      case $1 in
         -install|-i)
         	INSTALL="TRUE"
         	echo "Maven Install has been configured"
                ;;
         -deploy|-d)
		PROCESSOR=`uname -p`
		if [ $PROCESSOR != "unknown" ]; then
			DEPLOY="TRUE"
			echo "Deploy has been configured"
		else
			#	windows paltform can not deploy
			echo "Deploy option is not supported on windows"
		fi
                ;;    
	 -dev)
		DEVELOPMENT="TRUE"
		echo "Maven development build"
		;;
         -help|-h)
                ;;
	  -skip)
        SKIPTEST="-DskipTests"
        echo "skiping test"
        ;;		
         *)  
         	LatestBuild=$1
         	echo "latestbuild = $LatestBuild"
      esac
      shift
    done
    
}

# ================================================================ #
# determineVersion()
#     method to create version from the latest baseline or 
#     use the version passed into the script 9see getCmdLine() )
# ================================================================ #
determineVersion() {
    if [ "$DEVELOPMENT" == "TRUE" ]; then
    	MOIPRelease=MIO.202CP.VVS.SNAPSHOT
    else
	if [ "$LatestBuild"  == "" ]; then
		MOIPRelease=MIO.202CP.VVS.SNAPSHOT
	else
  		MOIPRelease=$LatestBuild
	fi
    fi
}

# ================================================================ #
# showSettings()
#     Let the user know the settings we are using.
# ================================================================ #
showSettings() {

    echo ""    
    echo "#===================================================================#"
    echo "#  Makerelease Options: "    
    echo "#===================================================================#"
    echo "   INSTALL = $INSTALL"    
    echo "   DEPLOY  = $DEPLOY"    
    echo "   VERSION = $MOIPRelease"   
    echo "#===================================================================#"    
    echo ""    
    
}

# ================================================================ #
# createReleasePom()
#     Call the mvn plugin to create the release poms.
# ================================================================ #
createReleasePom() {
    mvn m4e-relpom:clean
    mvn m4e-relpom:generate -Dversion=$MOIPRelease -DapplicationVersion=$MOIPRelease
}

# ================================================================ #
# callMvn()
#     Call mvn install to create the build.
#     If install was not requested by user then show a help screen.
# ================================================================ #
callMvn() {
    if [ $INSTALL == "TRUE" ]
    then
			mvn clean install $SKIPTEST
    else
        echo Done
        echo -e "\n\n\tUse the following command to build and deploy the release to the web:"
        echo -e "\t\tmvn -f release-pom.xml clean deploy"
        echo -e "\n\tOR use the following command to to build and install the release to your local repository:"
        echo -e "\t\tmvn -f release-pom.xml clean install"
        echo ""
    fi
} 

# ================================================================ #
# deploy()
#     Use the deploy script to deploy the release.
#     If deploy was not requested by user, the do nothing.
# ================================================================ #
deploy() {
	if [ $DEPLOY == "TRUE" ]
	then
            cd $ABS_DIR
	    ./deploy.sh 
            cd -
	fi
}

# ================================================================ #
# usage()
#     Show help screen.
# ================================================================ #
usage() {
  echo "  `basename $0` : Create release-pom files for all pom files and run mvn if requested."
  echo " "
  echo "      usage : makerelease.sh [-install|-deploy|-help|-skip] <LABEL>"
  echo " "
  echo "          where  \"-install\" caused mvn to be run in install mode"
  echo "                 \"-deploy\" causes mvn to be run in deploy mode"
  echo "                 \"-skip\" causes mvn to skip tests"
  echo "                 only one of either install or deploy may be used "
  echo "                 \"<LABEL>\" may be passed to use as version for build."
  echo "                 if no label is provided then the current baseline will be used."
  echo "          examples :"
  echo "                 ./makerelease.sh -install      : uses baseline for versioning and runs mvn install "
  echo "                 ./makerelease.sh -deploy       : uses baseline for versioning and runs mvn deploy "
  echo "                 ./makerelease.sh -install PA01 : uses PA01 for versioning and runs mvn install "
  echo "                 ./makerelease.sh -help         : shows this help screen"
  echo "                    " 
       
  exit 0 
}

# ================================================================ #
# ============                 MAIN                  ============= #
# ================================================================ #

CUR_BR=`cd $CUR_DIR;git name-rev --name-only HEAD`
MIO_BR=`cd $MIO_DIR;git name-rev --name-only HEAD`
VVSAPPS_BR=`cd $VVSAPPS_DIR;git name-rev --name-only HEAD`

echo "VVS current branch is $CUR_BR"
echo "MIO  parent branch is $MIO_BR"
echo "VVSAPPS     branch is $VVSAPPS_BR"

if [ "$CUR_BR" != "$MIO_BR" ] || [ "$CUR_BR" != "$VVSAPPS_BR" ] || [ "$MIO_BR" != "$VVSAPPS_BR" ] 
then
	echo "Check and makre sure VVS branch, MIO parent branch and VVSAPPS branch are at the same level"
	exit 1
fi


echo -e "\n=============================================================="
echo -e "Creating Release POM files to produce an official MOIP Release"
echo -e "==============================================================\n"

getCmdLine $*
determineVersion
showSettings

# ===================================================== #
#  Step #1 : Clean and createrelease poms 
# ===================================================== #
createReleasePom

# ===================================================== #
#  Step #2 : Call maven to create the build
# ===================================================== #
callMvn

# ===================================================== #
#  Step #3 : Deploy the newly created build
# ===================================================== #
deploy

cd $CUR_DIR

