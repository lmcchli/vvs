#!/bin/sh
PKGNAME=__PKGNAME__

root_test(){
ROOTTEST="/bin/TestIfRoot783249845u95"
mkdir $ROOTTEST

if [ $? = 0 ]; then
    rm -r $ROOTTEST
else
    clear
    echo "You must be root to install an NTF Language!"
    exit 1
fi
}


########
#MAIN
########

INSTALL_DIR=`dirname $0`
cd $INSTALL_DIR
INSTALL_DIR=`pwd`
ADMIN_FILE="$INSTALL_DIR/adminfile"
root_test

echo "Starting NTF Language installation..."
pkgadd -a $ADMIN_FILE -d $INSTALL_DIR/$PKGNAME.pkg $PKGNAME
