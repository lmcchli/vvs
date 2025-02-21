#!/bin/ksh

##
# Create a MAS delivery in /vobs/ipms/mas.
# 
# Can build either from a label of from the current view.
##


NO_GNU_BUILD=""
NO_JAR_BUILD=""
NO_CLEAN=""
LABEL_FILES=""

BUILD_NUMBER=0
NUMBER_FILE=""
LABEL=""
NOBUILD="FALSE"
PRIVATE=""
BUILD_FROM_LABEL=""
VERSION=""
FILENAME=""


DIRNAME=`dirname $CMD`
cd $DIRNAME

DIRNAME=`pwd`
cd ..

test -n "$BASEDIR" || BASEDIR=/vobs/ipms/mas
CFGFILE_TMPDIR=$BASEDIR/cfgfiletemp
PKGNAME=MOBYmas
cd $DIRNAME



##
# Check the arguments
# Determine if build from label, private build, etc.
##
check_arguments()
{
if (( $# < 1 )) || (( $# > 5 )) then
    echo 
    echo
    echo " you must use the following syntax "
    echo
    echo " Syntax: mkdeliv_pkg.sh <version> [-s] [-p] [-l] [-nobuild] | [[-nojarbuild] [-nognubuild]] [-noclean] ] "
    echo " -p           Using private build. Does not label and does not increase build number. "
    echo " -l           Build using label specified by version."
    echo " -nobuild     Do not rebuild, use any existing files."
    echo " -nojarbuild  Do not build the java classes, use any existing files."
    echo " -nognubuild  Do not build the c++ source code, use any existing files."
    echo " -noclean     Do not clean up after the build."  
    
    exit 1
    
else

    echo
    echo
    echo " ***********************************************************"
    echo " *                                                         *"
    echo " *    This script builds a delivery package for MAS.       *"
    echo " *                                                         *"
    echo " *  Before running this script,                            *"
    echo " *  Be sure to use the right ClearCase view with the       *"
    echo " *  right config spec.                                     *"
    echo " *                                                         *"
    echo " ***********************************************************"
    echo

# /usr/bin/tr seems to return "Bad string" for locales such as 
# "sv_SE.UTF-8", using LC_ALL=C as a workaround

    VERSION=`echo $1 | LC_ALL=C /usr/bin/tr -s '[:lower:]' '[:upper:]'`

    for vars in "$@"
    do
        case "$vars" in
        -l)
                BUILD_FROM_LABEL=$1
                PRIVATE="-p"
                LABEL=$VERSION
                ;;
        -p)
                PRIVATE="-p"
                LABEL=$VERSION
                #echo "ERROR: Private build not implemented yet. exiting."
                #exit 1                
                ;;
        -nojarbuild)
                NO_JAR_BUILD="TRUE"
                ;;
        -nognubuild)
                NO_GNU_BUILD="TRUE"
                ;;
        -nobuild)
                NO_JAR_BUILD="TRUE"
                NO_GNU_BUILD="TRUE"
                ;;
        -noclean)
                NO_CLEAN="TRUE"
                ;;              
        esac
    done        
        
    if [ -z $PRIVATE ]
    then
	echo "INFO: Not private build. (Label files, and increase build numbers" 
    else
	echo "INFO: Private build. (Not label files, and not increase build numbers" 
    fi
    
    if [ -z "$BUILD_FROM_LABEL" ]
    then	
        echo "INFO: Using current view for build."
    else 
        echo "INFO: Using label $LABEL for build." 
    fi

    if [ -z $NO_JAR_BUILD ]
    then
        echo "INFO: Compile jar files" 
    else
        echo "INFO: Not compile jar files" 
    fi

    if [ -z $NO_GNU_BUILD ]
    then
        echo "INFO: Compile C files" 
    else
        echo "INFO: Not compile C files" 
    fi   
    
    if [ -z $NO_CLEAN ]
    then
        echo "INFO: Clean up after build" 
    else
        echo "INFO: Do not clean up after build" 
    fi

fi
}


convert_cfg_files_to_unixformat(){

CURDIR=`pwd`
rm -rf $CFGFILE_TMPDIR
mkdir $CFGFILE_TMPDIR

cp $BASEDIR/cfg/*.xml $CFGFILE_TMPDIR
cp $BASEDIR/cfg/*.xsd $CFGFILE_TMPDIR
cp $BASEDIR/cfg/*.properties $CFGFILE_TMPDIR

TMPFILE=tmpfile
cd $CFGFILE_TMPDIR
ls -1 |
while read FILE
do
  dos2unix $FILE > $TMPFILE 2>/dev/null
  mv -f $TMPFILE $FILE
done

cd $CURDIR
}

make_platformaccess_javadoc(){
CURDIR=`pwd`
cd $BASEDIR/execution_engine/interface/com/mobeon/masp/execution_engine/platformaccess
rm -rf javadoctmp
javadoc -d javadoctmp -sourcepath . com.mobeon.masp.execution_engine.platformaccess PlatformAccess.java PlatformAccessUtil.java > /dev/null 2>&1
if [ $? != 0 ];then
  abort "Failed to create platformaccess javadoc"
fi
cd javadoctmp
FILENAME=IWD_PlatformAccess.3.IWD.MAS0001.zip
zip -r $FILENAME  *
cleartool co -nc $BASEDIR/doc/$FILENAME
mv $FILENAME $BASEDIR/doc/$FILENAME
cleartool ci -nc -identical $BASEDIR/doc/$FILENAME

rm -rf javadoctmp
cd $CURDIR
}



##
# Get the build number to use.
##
get_build_number()
{
BASEVERSION=`echo $VERSION | /usr/bin/sed 's/_P/_R/' | cut -f1 -d.`
echo "BASEVERSION=$BASEVERSION"

STARTLABEL="START_$BASEVERSION"

if [ -z $PRIVATE ]
then
    NUMBER_FILE=$BASEDIR"/tools/"$STARTLABEL"_numbers"
    if [ -f "$NUMBER_FILE" ]; then           
        cleartool co -nc $NUMBER_FILE
        num=`sort -n $NUMBER_FILE | tail -1`
     
        if [ -z "$num" ]; then
            echo "INFO: Found no numbers in $NUMBER_FILE"
            num=0;
        fi
      
        # add one to build_number
        (( num=num+1 ))
        BUILD_NUM=$num
        echo "INFO: Adding $BUILD_NUM to number file $NUMBER_FILE"
        echo $BUILD_NUM >> $NUMBER_FILE
        
    else   
        BUILD_NUM="001"
        echo "INFO: Adding $BUILD_NUM to number file $NUMBER_FILE"
        echo "INFO: Create number file: $NUMBER_FILE"
        echo $BUILD_NUM >> $NUMBER_FILE
           
        cd $BASEDIR      
        cleartool co -nc tools        
        cleartool mkelem -eltype file -c "from mkdeliv.sh" $NUMBER_FILE        
        cd $DIRNAME
    fi

    #Format build  to 3 digits with prec 0.
    BUILD_NUM=`echo $BUILD_NUM | awk '{ printf "%03s", $0 }'`

    LABEL=$VERSION"."$BUILD_NUM

    echo
    echo "INFO: Using label $LABEL"

    CONTINUE=`ckkeywd -Q -d y -p "Is this information correct? (default: y)" \
    -e "Not a valid choice" y n` || exit $?
    if [ "$CONTINUE" = "n" ]; then
        ### TODO read new values instead.
        exit 0
    fi
   
    cleartool ci -c "$BUILD_NUM" $NUMBER_FILE
    cleartool ci -c "added $NUMBER_FILE" $BASEDIR/tools 2>&1 >> /dev/null

else
    # Private build
    NUMBER_FILE=$BASEDIR/tools/"$STARTLABEL"_private_numbers
    ### TODO

    echo ""
    echo " INFO: Using label $LABEL "


   PS3="Enter your choice: "
   select menu_list in Yes No
   do
        case $menu_list in
            Yes)        break;;
            No)         cleartool uncheckout -rm  $NUMBER_FILE; exit 0;;
            *)       print " ????";;
        esac
    done


     #echo "Check in number file :$NUMBER_FILE"
     cleartool ci -c "from mkdeliv.sh" $NUMBER_FILE

   #Private
   # if building on a specified label
   if [ -z $BUILD_FROM_LABEL ]   
   then
       echo "Using build number file: $NUMBER_FILE"
       if [[ -f $NUMBER_FILE ]] then
	 BUILD_NUM=`tail -1 $NUMBER_FILE`
       else
    	 BUILD_NUM="001"
       fi

      (( BUILD_NUM=BUILD_NUM+1 ))
      echo $BUILD_NUM >> $NUMBER_FILE

 
       #Format build  to 3 digits with prec 0.
       BUILD_NUM=`echo $BUILD_NUM | awk '{ printf "%03s", $0 }'`
   
       LABEL=$VERSION"_P"
    else
	echo "create new config spec  ( label $VERSION )"
   	echo "element * CHECKEDOUT" > $BASEDIR/tmpNewConfigSpec 
   	echo "element * $VERSION" >> $BASEDIR/tmpNewConfigSpec 
   	
   	#cat $VOB/tmpSavedConfigSpec >> $BASEDIR/tmpNewConfigSpec 		 
   	echo "set new config spec " 
   	cleartool setcs $BASEDIR/tmpNewConfigSpec
	
	LABEL=$VERSION
   fi

   echo " New build with label: $LABEL "
      

   PS3="Enter your choice: "
    select menu_list in Yes No
    do
        case $menu_list in
            Yes)        break;;
            No)         exit 0;;
            *)       print " ????";;
        esac
    done


fi     
}


##
# Label all files in MAS vob with the label
##
label_files()
{
EXIST_LABEL=`cleartool lstype -s -kind lbtype | grep $LABEL`
if [[ $EXIST_LABEL = $LABEL ]]
then
    echo
    echo "WARNING: The label $LABEL already exist, this label will now be moved."
    CONTINUE=`ckkeywd -Q -d y -p "Do you want to continue? (default: y)" \
    -e "Not a valid choice" y n` || exit $?
    if [ "$CONTINUE" = "n" ]; then
        abort "INFO: Interrupted by user request."
    fi
else
    echo "INFO: Createing label: $LABEL"
    cleartool mklbtype -nc $LABEL 
    if [ $? -gt 0 ]
    then
        abort "ERROR: Failed to create label type $LABEL"
    fi
fi

echo "INFO: Label files"
cleartool mklabel -replace -recurse $LABEL $BASEDIR 
if [ $? -gt 0 ]
then
   abort "ERROR: Failed to label files."      
fi
}


##
# Compile source code
##
compile()
{
# cleanup directory
rm -rf $BASEDIR/$PKGNAME

echo "INFO: Building $LABEL, build $BUILD_NUM"

cd $BASEDIR

if [ -z $NO_JAR_BUILD ]
then
   ant distclean
   if [ $? -ne 0 ]; then
       abort "ERROR: Compilation failed."
   fi
   
   ant distjar -Dbuild.failoncomponenterror=true
   if [ $? -ne 0 ]; then
       abort "ERROR: Compilation failed."
   fi
fi

if [ -z $NO_GNU_BUILD ]
then
   cd stream
   
   ant compile.3pp
   if [ $? -ne 0 ]; then
       abort "ERROR: Compilation failed."
   fi
   
   ant compile.native
   if [ $? -ne 0 ]; then
       abort "ERROR: Compilation failed."
   fi
   
   clearmake -M -C gnu dist
   if [ $? -ne 0 ]; then
       abort "ERROR: Compilation failed."
   fi
   
   cd ..
fi   
   
if [[ -f "lib/log4j.xml" ]] then
   rm lib/log4j.xml
fi

# This will create a tar file of all jars and libs,
# it will be used in the package later.   
ant disttar
}


##
# Copies all needed files to a temporary directory and builds the package
##
create_package()
{
#Create package directory
cd $BASEDIR

if [ ! -d $BASEDIR/$PKGNAME/src/lib ] 
then 
    mkdir -p $BASEDIR/$PKGNAME/src/lib
fi;

if [ ! -w $BASEDIR/$PKGNAME ] 
then    
    abort "ERROR: Directory $BASEDIR/$PKGNAME has no write permission."
fi;


cd $BASEDIR/$PKGNAME/src/lib
if [ $? -gt 0 ]
then
    abort "ERROR: Directory $PKGNAME/src/lib not found."
fi

tar xf $BASEDIR/MAS_drop.tar
if [ $? -gt 0 ]
then
    abort "ERROR: Failed to un-tar $BASEDIR/MAS_drop.tar"
fi
rm $BASEDIR/MAS_drop.tar


# Add aditional files to the install_pkg directory

cd $BASEDIR/$PKGNAME/src
# Create the application directory
mkdir applications
mkdir bin
mkdir log
mkdir data
mkdir etc
mkdir tmp
mkdir cfg
mkdir lub

cd $BASEDIR/$PKGNAME
mkdir bin
mkdir etc
mkdir proto
mkdir spool
mkdir built


# Create pkginfo file
sed s!#_BUILD_VERSION_#!$LABEL!g $BASEDIR/tools/svcs_tools/main_pkginfo > $BASEDIR/tools/svcs_tools/pkginfo_tmp
sed s!#_BASE_VERSION_#!$LABEL!g $BASEDIR/tools/svcs_tools/pkginfo_tmp > $BASEDIR/tools/svcs_tools/pkginfo
rm $BASEDIR/tools/svcs_tools/pkginfo_tmp

echo "INFO: Copy MAS admin scripts."
# copy rc scripts
cp $CFGFILE_TMPDIR/* src/cfg/.
rm -rf $CFGFILE_TMPDIR
cp $BASEDIR/etc/* src/etc/.

cp $BASEDIR/tools/svcs_tools/mas src/bin/.
cp $BASEDIR/tools/svcs_tools/snmpagent src/bin/.
cp $BASEDIR/tools/svcs_tools/update_configuration src/bin/.
cp $BASEDIR/tools/svcs_tools/updatemasxml.sh src/bin/.
cp $BASEDIR/tools/svcs_tools/xmlmodify.sh src/bin/.
cp $BASEDIR/tools/svcs_tools/rc.mas src/etc/.
cp $BASEDIR/tools/svcs_tools/rc.masadm src/etc/.
cp $BASEDIR/tools/svcs_tools/svcs.masadm src/etc/.
cp $BASEDIR/tools/svcs_tools/mascommon src/etc/.
cp $BASEDIR/tools/svcs_tools/mcrreg.sh src/etc/.
cp $BASEDIR/tools/svcs_tools/mcrunreg.sh src/etc/.
cp $BASEDIR/tools/svcs_tools/mcrlib src/etc/.

cp $BASEDIR/tools/svcs_tools/platformDependentParameters src/etc/.
cp $BASEDIR/tools/svcs_tools/platformDependentFunctions_V210 src/etc/.
cp $BASEDIR/tools/svcs_tools/platformDependentFunctions_T200 src/etc/.
cp $BASEDIR/tools/svcs_tools/getPlatformDependentParameter src/etc/.
cp $BASEDIR/tools/svcs_tools/platformIndependentFunctions src/etc/.

cp $BASEDIR/tools/svcs_tools/CfgTool src/bin/.
cp $BASEDIR/tools/svcs_tools/logger.sh src/bin/.
cp $BASEDIR/tools/svcs_tools/info.sh src/bin/.

cp $BASEDIR/tools/mcpadmin.sh src/bin/.
cp $BASEDIR/tools/appadmin.sh src/bin/.
cp $BASEDIR/tools/swaadmin.sh src/bin/.
cp $BASEDIR/tools/createxml.nawk src/bin/. 
cp $BASEDIR/tools/appmcpreg.sh src/bin/. 
cp $BASEDIR/tools/bckmasconfig.sh src/etc/.
cp $BASEDIR/tools/svcs_tools/Makefile .

cp $BASEDIR/bin/getTimeZone src/bin/.

echo "INFO: Copy Manifest."
cp $BASEDIR/tools/svcs_tools/MOBYmas.xml src/etc/.

echo "INFO: Copy install scripts."
cp $BASEDIR/tools/svcs_tools/postinstall proto/.
cp $BASEDIR/tools/svcs_tools/checkinstall proto/.
cp $BASEDIR/tools/svcs_tools/preinstall proto/.
cp $BASEDIR/tools/svcs_tools/postremove proto/.
cp $BASEDIR/tools/svcs_tools/preremove proto/.
cp $BASEDIR/tools/svcs_tools/pkginfo proto/.
cp $BASEDIR/tools/svcs_tools/request proto/.

# Copy the default applications
cp -r $BASEDIR/applications/* src/applications/.

echo "INFO: Copy live upgrade scripts."
cp $BASEDIR/lub/mas.conf src/lub/.
cp $BASEDIR/lub/mas_backup.sh src/lub/.
cp $BASEDIR/lub/mas_restore.sh src/lub/.

echo "INFO: Creating package"
cd $BASEDIR/$PKGNAME
make BASE=$BASEDIR
if [ $? -ne 0 ]; then
    abort "ERROR: Failed to make package."
fi
}


##
# Create the delivery file in BASEDIR
##
create_delivery() 
{
if [ -d $BASEDIR/$PKGNAME/built ]; then
    cd $BASEDIR/$PKGNAME/built
    FILENAME=$LABEL".mas0001.solaris10.tar"
    FILENAME=`echo $FILENAME | LC_ALL=C /usr/bin/tr -s '[:upper:]' '[:lower:]'`
    PKGFILE=$PKGNAME".pkg"
    if [ -f $PKFILE ]; then
        tar cvf $FILENAME $PKGFILE
        gzip $FILENAME
        mv $FILENAME.gz $BASEDIR
    else
        abort "ERROR: Could not find package file $PKGFILE"
    fi
else
    abort "ERROR: Could not find directory $BASEDIR/$PKGNAME/built"
fi
}


##
# Clean up the build
##
cleanup() {
    if [ -z $NO_CLEAN ]; then
        echo "INFO: Cleaning up files."
        cd $BASEDIR
        ant distclean
        rm -rf $PKGNAME
        rm $BASEDIR/tools/svcs_tools/pkginfo
    fi
    rm -rf $CFGFILE_TMPDIR
}


##
# Save the old config spec and set the new after the label
##
set_new_configspec()
{
    echo "INFO: save current config spec "
    cleartool catcs > $BASEDIR/tmpSavedConfigSpec 			 

    echo "element * CHECKEDOUT" > $BASEDIR/tmpNewConfigSpec 
    echo "element * $LABEL" >> $BASEDIR/tmpNewConfigSpec 
    echo "INFO: set new config spec" 
    cleartool setcs $BASEDIR/tmpNewConfigSpec
}


##
# Restore the old config spec
##
restore_configspec()
{
    if [ -f $BASEDIR/tmpSavedConfigSpec ]; then
        echo "INFO: restore saved config spec"
        cleartool setcs $BASEDIR/tmpSavedConfigSpec
        rm $BASEDIR/tmpSavedConfigSpec
        rm $BASEDIR/tmpNewConfigSpec
    fi
}

abort()
{
    echo "$@"
    restore_configspec
    rm -rf $CFGFILE_TMPDIR
    exit 1
}



########
# MAIN #
########

check_arguments $@

convert_cfg_files_to_unixformat
test -z "$PRIVATE" && make_platformaccess_javadoc

if [ -n "$BUILD_FROM_LABEL" ]; then
    ##
    #Build from a label
    ##    

    EXIST_LABEL=`cleartool lstype -s -kind lbtype | grep $LABEL`
    if [[ $EXIST_LABEL != $LABEL ]]
    then
        abort "ERROR: Label $LABEL is not found."
    fi
    
    set_new_configspec
    
else
    ##
    #Build from current view   
    ##
    
    get_build_number
    
    # Do not label files if build is private
    if [ -z "$PRIVATE" ]; then
        label_files
        set_new_configspec
    fi
fi

compile
create_package
create_delivery
restore_configspec
cleanup

echo "INFO: Build completed."
echo "INFO: Delivery file is $BASEDIR/$FILENAME.gz"
exit 0
