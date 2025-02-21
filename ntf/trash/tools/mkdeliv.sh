#!/bin/sh
#
# This script will create a delivery file (e.g 
# ntf_p4_01.crh109127.solaris8.tar) at the directory 
# /vobs/ipms/ntf/delivery/
#
# Remember to set a correct vobview before executing this script, e.g.
# ct setview nika_ntf_r4a
# 
# 

VOB=/vobs/ipms/ntf
VOBS_TO_LABEL="$VOB"
SELFDIAG=/proj/ipms/selfdiag/R1A.003

error(){
    echo $PGM: ERROR: "$*" 1>&2
    exit 1
}

usage(){
    echo "$PGM: Wrong parameters. Use: $PGM " 1>&2
    exit 1
}

test_checkouts(){
    cd $VOB
    cleartool lsco -all | grep checkout
    if [ $? = 0 ];then
        echo
        echo -n "There are checked-out files. Continue anyway?"
        answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
	if [ "$answer" = "n" ]; then
	    exit 1
 	fi
    fi
}

clean_build(){
    echo "Cleaning build."
    rm -rf $VOB/build/*
}

update_version(){
    echo "Writing version."
    cd $VOB/build/
    echo "VERSION=$MCRVERSION" > VERSION
}

compile(){
    echo
    echo "**************************************************"
    echo "Starting compilation of NTF. Please wait..."
    cd $VOB/src
    export VERSION
    clearmake -V -C gnu clean
    clearmake -V -C gnu all
    if [ $? != 0 ]; then
        echo "ERROR WHEN COMPILING NTF."
        exit 1
    fi
    echo ""
    echo "Done compiling."
    echo "**************************************************"
    echo "**************************************************"
    echo "Starting compilation of HA-AGENT. Please wait..."
    cd $VOB/HA_Agent/LMEAntf/src
    make pkg
    if [ $? != 0 ]; then
        echo "ERROR WHEN COMPILING HA-AGENT."
        exit 1
    fi
    echo ""
    echo "Done compiling HA-AGENT."
    echo "**************************************************"
}

makehapackage(){
    mkdir -p $VOB/delivery/HA_Agent
    /usr/bin/pkgtrans /vobs/ipms/ntf/HA_Agent/LMEAntf/pkg /vobs/ipms/ntf/delivery/HA_Agent/LMEAntf.pkg LMEAntf
}

copy_files(){
    echo "Copy files"
    cp -rp $VOB/instance_template $VOB/build/
    cp -rp $VOB/lib $VOB/build/
    cp -rp $VOB/bin $VOB/build/
    cp -rp $VOB/selfdiag $VOB/build/
    cp -rp $SELFDIAG/* $VOB/build/selfdiag
    cp -rp $VOB/lub $VOB/build/
    
    mkdir $VOB/build/uninstall
    mkdir $VOB/delivery/ntf_files

    cp $VOB/src/install/job_install_ntf $VOB/delivery/ntf_files/
    cp $VOB/src/install/install.sh $VOB/delivery/
    cp $VOB/src/install/adminfile $VOB/delivery/ntf_files/
    cp $VOB/instance_template/cfg/notification.cfg $VOB/delivery/LMENtf.response
    cp $VOB/src/install/adminfile $VOB/build/uninstall
    cp $VOB/src/install/uninstall.sh $VOB/build/uninstall
    cp $VOB/src/install/rc.ntf $VOB/build/uninstall
    
    chmod -R 777 $VOB/delivery/*
    chmod 777 $VOB/build/uninstall/*
    
    echo "Copy files done."
}

write_version(){
    echo "VERSION=$MCRVERSION" > $VOB/build/VERSION
}

makentfpackage(){
    cd $VOB/src/install
    clearmake -C gnu VERSION="$LABEL"
}

maketarfile() {
    echo "Creating delivery."
    cd $VOB/delivery/
    echo "Creating delivery file $FILE.gz ..."
    tar cf $FILE LMENtf.response install.sh ntf_files HA_Agent
    
    if [ -f $FILE.gz ]; then
        rm -f $FILE.gz
    fi
    gzip -c $FILE > $FILE.gz 
    rm -f $FILE
    rm -rf $VOB/delivery/LMENtf.response $VOB/delivery/install.sh $VOB/delivery/ntf_files $VOB/delivery/HA_Agent
    echo "Creating delivery done."
}


do_label(){
    if [ "$SHOULD_LABEL" = "y" ];then 
        echo
        echo "**************************************************"
        echo -n "Attaching label $LABEL on $VOBS_TO_LABEL. Please wait... "
        
        for VOB_NAME in $VOBS_TO_LABEL
          do
          cd $VOB_NAME
          cleartool lstype -s -kind lbtype | grep -w $LABEL > /dev/null
          if [ ! $? = 0 ];then
              cleartool mklbtype -c "Label for $LABEL files"  $LABEL > /dev/null
          fi
          cleartool lslock -s | grep -w $LABEL > /dev/null
          if [ $? = 0 ];then
              cleartool unlock lbtype:$LABEL > /dev/null
          fi
          cleartool mklabel -replace -recurse $LABEL . > /dev/null
          if [ ! $? = 0 ];then
              echo
              error "Labeling failed. Exiting."
          fi
#    cleartool lock lbtype:$LABEL > /dev/null
        done
        
        echo "Labeling done."
        echo "Labeling complete and label locked."
    fi
}

############################
#         MAIN             #
############################

PGM=`basename $0`
if [ $# -gt 1 ];then usage;fi
TOPDIR=`pwd`


echo -n "Enter NTF version <version> [<patch>] (e.g. \"R10C\" or \"R10C 7\"): "
read VERSION PATCH
export VERSION PATCH

if [ -z "$PATCH" ]; then
    LABEL=NTF_$VERSION
    MCRVERSION=$VERSION
    FILE=`echo ntf_${VERSION}.crh109127.solaris10.tar | tr '[:upper:]' '[:lower:]'`
else
    LABEL=${PATCH}_NTF_$VERSION
    MCRVERSION="$VERSION, $LABEL"
    FILE=`echo ${PATCH}_ntf_${VERSION}.crh109127.solaris10.tar | tr '[:upper:]' '[:lower:]'`
fi
echo -n "Label with $LABEL after a successful build?"
SHOULD_LABEL=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
test_checkouts
clean_build
compile
makehapackage
copy_files
write_version
makentfpackage
maketarfile
clean_build
do_label

echo
echo "**************************************************"
echo
echo "COMPLETED."
echo "File is ready for pickup at $VOB/delivery/"
echo
exit 0
