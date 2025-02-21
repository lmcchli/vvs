#!/bin/sh 
#
# This script will create a patch delivery file, e.g
# 1_ntf_r13a.crh109127.solaris10.tar
#
#
#

error(){

    echo "ERROR: $*" 1>&2
    exit 1
}

configure(){
    sed -e s%"$1"%"$2"% < $3 > $3.tmp
    mv $3.tmp $3
}

header(){
    clear
    echo "NTF - Create patch"
    echo "******************"
}

ask_continue() {
    echo "The used environment variables from file 'mkpatch.cfg' will be:"
    echo "\tPKG=$PKG"
    echo "\tPKGNAME=$PKGNAME"
    echo "\tVERSION=$VERSION"
    echo "\tPATCHID=$PATCHID"
    echo "\tPLATFORM=$PLATFORM"
    echo "\tOBSOLETES=$OBSOLETES"
    echo "\tREQUIRES=$REQUIRES"
    echo "\tINCOMPAT=$INCOMPAT"

    continue=`ckkeywd -Q -p "Continue: " -e "Not a valid choice" y n ` || exit $?
    if [ "$continue" = "n" ]; then
        exit 0
    fi

}

setup_builddir() {
    if [ -d $BUILDDIR ]; then
        rm -rf $BUILDDIR/*
    else
        mkdir -p $BUILDDIR
    fi

    cp $PATCHDIR/pkg/prototype.template $BUILDDIR/prototype
    cp $PATCHDIR/pkg/comp_checkinstall $BUILDDIR
    cp $PATCHDIR/pkg/comp_preinstall $BUILDDIR
    cp $PATCHDIR/pkg/comp_postinstall $BUILDDIR
    cp $PATCHDIR/pkg/comp_preremove $BUILDDIR
    cp $PATCHDIR/pkg/comp_postremove $BUILDDIR
    chmod 775 $BUILDDIR/*

}

make_patch_package(){
    export PKG
    export PKGNAME
    export BUILDDIR
    export BASEDIR
    export VERSION
    export PATCHFILE
    export PATCHID
    export OBSOLETES
    export REQUIRES
    export INCOMPAT
    cd $PKGDIR/patch
    ./makepatchpkg

}

get_config() {
    PARAMETER=$1
    echo "`cat $VOB/tools/mkpatch.cfg | grep $PARAMETER= | sed 's%'$PARAMETER'=\(.*\)%\1%'`"
}

set_parameters() {
    PWD=`pwd`
    PKGDIR=/proj/ipms/pkg/LATEST
    VOB=/vobs/ipms/ntf
    PATCHDIR=$VOB/patches/BuildPatchFiles
    PKG=`get_config PKG`
    PKGNAME=`get_config PKGNAME`
    BASEDIR=/apps
    VERSION=`get_config VERSION`
    PATCHID=`get_config PATCHID`
    VERSION_LOWER=`echo $VERSION | tr '[:upper:]' '[:lower:]'`
    PATCHID_LOWER=`echo $PATCHID | tr '[:upper:]' '[:lower:]'`
    BUILDDIR=$VOB/build/$PATCHID
    PATCHFILE=$VOB/delivery/${PATCHID_LOWER}.crh109127.solaris10.zip
    PLATFORM=`get_config PLATFORM`
    OBSOLETES=`get_config OBSOLETES`
    REQUIRES=`get_config REQUIRES`
    INCOMPAT=`get_config INCOMPAT`
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
}

move_readme(){
    version_without_buildnr=`echo $PATCHID_LOWER | sed 's%\(.*\)\..*%\1%'`
    README_FILE=$VOB/delivery/README_${version_without_buildnr}.crh109127.pa1.txt
    cp $VOB/patches/BuildPatchFiles/README_TEMPLATE $README_FILE
    chmod 666 $README_FILE
    configure "__VERSION__" "$PATCHID" "$README_FILE"
    configure "__VERSION_LOWER__" "${PATCHID_LOWER}" "$README_FILE"
    DATE=`date +%b-%d-%Y`
    cd $BUILDDIR
    FILES=`cat $PATCHDIR/pkg/prototype.template | nawk ' /f.*=.*/ {print $3;} ' | sed 's%\(.*\)=.*%\1%'`
    configure "__DATE__" "$DATE" "$README_FILE"
    for a in $FILES
    do
      file=`echo $a | sed 's%\./\(.*\)%\1%'`
      echo "                 $file" >> $README_FILE
    done
    echo ":------------------------------------------------------------------------------:" >> $README_FILE
    echo "" >> $README_FILE
}

ask_lable() {
    echo "\n\nLabel with $PATCHID after a successful build?\c"
    SHOULD_LABEL=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
}

lable_files() {

    if [ "$SHOULD_LABEL" = "y" ];then

        echo "\nAttaching label $PATCHID on all files in $VOB. Please wait... \c"

        cd $VOB
        cleartool lstype -s -kind lbtype | grep -w $PATCHID > /dev/null
        if [ ! $? = 0 ];then
            cleartool mklbtype -c "Label for $PATCHID files"  $PATCHID > /dev/null
        fi
        cleartool lslock -s | grep -w $PATCHID > /dev/null
        if [ $? = 0 ];then
            cleartool unlock lbtype:$PATCHID > /dev/null
        fi
        cleartool mklabel -replace -recurse $PATCHID . > /dev/null
        if [ ! $? = 0 ];then
            echo "Warning: Labeling failed of $file"
        fi

        cleartool lock lbtype:$PATCHID > /dev/null

        echo "Labeling done."
        echo "Labeling complete and label locked."
    fi
}

completed(){
echo
echo "**************************************************"
echo "COMPLETED."
echo "File is ready for pickup at $VOB/delivery/"
exit 0
}




############################
#         MAIN             #
############################

header
set_parameters
ask_continue
ask_lable
compile
setup_builddir
make_patch_package
lable_files
move_readme
completed

