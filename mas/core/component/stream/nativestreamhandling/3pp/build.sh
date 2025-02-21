#!/bin/ksh

if [ "$1" == "keeplog" ]; then
    KEEPLOG="yes"
    shift
fi

ARGS=$*
BUILDOS=`uname -r`
LOGFILE=`pwd`/buildlog_`date +%Y%m%d%H%M%S`.log
echo $OS

build() {
    echo "Building $1"
    cd $1
    clearmake -C gnu -f $2 $ARGS | tee -a $LOGFILE
    if [ $? -ne 0 ]; then
	exit 1
    fi
    cd - >> /dev/null
}

if [ "$BUILDOS" == "5.8" ]; then
	build commoncpp2-1.3.19/src Makefile
	build ccrtp-1.3.6/src Makefile
fi

if [ "$BUILDOS" == "5.10" ]; then
	build commoncpp2-1.3.19/src Makefile.solaris10
	build ccrtp-1.3.4/src Makefile.solaris10
fi
 
if [ "$KEEPLOG" != "yes" ]; then
    rm $LOGFILE
fi
