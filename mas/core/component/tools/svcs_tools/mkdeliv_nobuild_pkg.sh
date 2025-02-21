#!/bin/ksh
#
# IN : VERSION Mandantory : [MAS_R1B] Version som skall byggas.
#      OPT     Optional   : [PRIVATE] Om PRIVATE använd ej byggnummer.
#
#
#
# bygga ett solarispaket

BUILD_NUMBER=0
NUMBER_FILE=""
LABEL=""
PRIVATE=""
BRANCH=""
BUILD_FROM_LABEL=""
VERSION=""

DIRNAME=`dirname $CMD`
cd $DIRNAME

DIRNAME=`pwd`
cd ..

#BASEDIR=`pwd`
BASEDIR=/vobs/ipms/mas
PKGNAME=MOBYmas

cd $DIRNAME





# chk parameters

BRANCH=`cleartool lstype -s -kind lbtype | grep START_MAS`
PREV_LABELS=`cleartool lstype -s -kind lbtype | grep -v START | grep MAS`

BUILD_FROM_LABEL=$1
export BUILD_FROM_LABEL
LABEL=$1
export LABEL
BUILD_NUM=$2
export BUILD_NUM

NUMBER_FILE=$BASEDIR/tools/$BRANCH"_numbers"

#skapa katalog där man kan lägga installationsfilerna
cd $BASEDIR
cd lib
rm log4j.xml
cd ..
ant disttar

# chk if dir exsists and have write permission
if [ ! -d $BASEDIR/$PKGNAME ] 
then
  mkdir $BASEDIR/$PKGNAME
  mkdir $BASEDIR/$PKGNAME/src
  mkdir $BASEDIR/$PKGNAME/src/lib
  
fi;

if [ -w $BASEDIR/$PKGNAME ] 
then
     echo " Direcrory "$BASEDIR/$PKGNAME" OK."
else
     echo " Directory "$BASEDIR/$PKGNAME" not writable."
     exit 1
fi;


# unpack MAS tar to MASPKG directory
echo "Unpack lib. : \c"

cd $BASEDIR/$PKGNAME/src/lib
if [ $? -gt 0 ]
then
   echo "Directory $PKGNAME/src/lib not found."
   exit 1
fi

tar xf $BASEDIR/MAS_drop.tar
if [ $? -gt 0 ]
then
   echo "Error unpacking lib."
   exit 1
else
   echo "OK"
fi


# Add aditional files to the install_pkg directory


cd $BASEDIR/$PKGNAME/src
# Create the application directory
mkdir applications
mkdir bin
mkdir log
mkdir data
mkdir etc
mkdir tools
mkdir tmp


cd $BASEDIR/$PKGNAME
mkdir bin
mkdir etc
mkdir proto
mkdir spool
mkdir built


# Create pkginfo file
sed s!#_BUILD_VERSION_#!$LABEL!g $BASEDIR/tools/svcs_tools/main_pkginfo > $BASEDIR/tools/svcs_tools/pkginfo_tmp
sed s!#_BUILD_NUMBER_#!$BUILD_NUM!g $BASEDIR/tools/svcs_tools/pkginfo_tmp > $BASEDIR/tools/svcs_tools/pkginfo

echo "Copy MAS admin scripts."
# copy rc scripts
cp $BASEDIR/tools/svcs_tools/mas src/bin/
cp $BASEDIR/tools/svcs_tools/update_configuration src/bin/
cp $BASEDIR/tools/svcs_tools/rc.mas src/tools/ 
cp $BASEDIR/tools/svcs_tools/rc.masadm src/tools/
cp $BASEDIR/tools/svcs_tools/svcs.masadm src/tools/ 
cp $BASEDIR/tools/svcs_tools/mascommon src/tools/ 
cp $BASEDIR/tools/mcpadmin.sh src/tools/ 
cp $BASEDIR/tools/appadmin.sh src/tools/ 
cp $BASEDIR/tools/mergeEcmaScripts.sh src/tools/ 
cp $BASEDIR/tools/createxml.nawk src/tools/ 
cp $BASEDIR/bin/getTimeZone src/bin/ 

# Temporary
cp $BASEDIR/lib/callmanager.xml src/lib/callmanager.xml
cp $BASEDIR/lib/callmanager_pattern.xml src/lib/callmanager_pattern.xml


cp $BASEDIR/tools/svcs_tools/Makefile .

echo "Copy Manifest."
#Copy manifest
cp $BASEDIR/tools/svcs_tools/mas.xml src/etc/.
#cp $BASEDIR/tools/svcs_tools/install.txt etc/.

echo "Copy install scripts."
cp $BASEDIR/tools/svcs_tools/postinstall proto/.
cp $BASEDIR/tools/svcs_tools/preinstall proto/.
cp $BASEDIR/tools/svcs_tools/postremove proto/.
cp $BASEDIR/tools/svcs_tools/preremove proto/.
cp $BASEDIR/tools/svcs_tools/pkginfo proto/.
cp $BASEDIR/tools/svcs_tools/request proto/.






# Copy the default applications
cp -r $BASEDIR/applications/* src/applications/.


#echo "Skapa tar fil med install script och jar fil"
#echo
#tar cvf MAS_tools.tar rc.mas bckmasconfig.sh uninstall_mas.sh
#echo
#echo
#tar cvf $LABEL".tar" MAS_drop.tar MAS_tools.tar install_mas.sh install.txt applications
#
#
#rm -f -R $BASEDIR/install_pkg/MAS_drop.tar
#rm -f -R $BASEDIR/install_pkg/rc.mas
#rm -f -R $BASEDIR/install_pkg/install_mas.sh
#rm -f -R $BASEDIR/install_pkg/MAS_tools.tar
#rm -f -R $BASEDIR/install_pkg/install.txt
#rm -f -R $BASEDIR/install_pkg/bckmasconfig.sh
#rm -f -R $BASEDIR/install_pkg/uninstall_mas.sh
#rm -f -R $BASEDIR/install_pkg/applications       
#
#
#
#
#   #skapa ny label och stega upp räknare (MAS_R1B_001)
#    # ställ dig i root för mas /vobs/ipms/mas
#    # ct mklbtype LABEL
#    # ct mklabel . LABEL -recourse -replace
#   
#    # anropa ANT för att compilera och skapa tar fil.
#    # ant makedist
#
## om PRIVATE
#    #
#
## skapa tar fil med jar filen + install filer.
#
#
#
## vilken version skall byggas.??
#
## skapa version ie. MAS_R1B.001
#
#
#echo
#echo
#echo "Install package $LABEL.tar succsessfully created."
#


cd $BASEDIR/$PKGNAME
#build all
make 



#
# restore config spec
#
#if [[ -z $PRIVATE ]] || [[ -n BUILD_FROM_LABEL ]]     
#then
#	echo "restore saved config spec"
#	cleartool setcs $BASEDIR/tmpSavedConfigSpec
#fi;
