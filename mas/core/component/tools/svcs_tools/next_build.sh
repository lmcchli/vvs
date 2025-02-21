#!/bin/ksh

if [ $# -ne 1 ]; then
    echo "Prints the name of the next MAS build."
    echo
    echo "Usage: $0 <base version>"
    echo "Example: $0 MAS_P2A"
    exit 1
fi

VERSION=`echo $1 | LC_ALL=C /usr/bin/tr -s '[:lower:]' '[:upper:]'`
BASEVERSION=`echo $VERSION | /usr/bin/sed 's/_P/_R/' | cut -f1 -d.`
BRANCH=`cleartool lstype -s -kind lbtype | grep "START_$BASEVERSION"`

BASEDIR=/vobs/ipms/mas
NUMBER_FILE=$BASEDIR/tools/$BRANCH"_numbers"
num=`tail -1 $NUMBER_FILE`
(( num=num+1 ))
BUILD_NUM=$num
BUILD_NUM=`echo $BUILD_NUM | awk '{ printf "%03s", $0 }'`
LABEL=$VERSION"."$BUILD_NUM
echo "Next build is" $LABEL
