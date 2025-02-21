#!/bin/sh
TARGET=`mktemp -d`
DESTPATH=apps/dist/mas-libs
mkdir -p ${TARGET}/${DESTPATH}
RTSAFE_LIBDIR=$1
LOGGING_3PP_LIBDIR=$2
SSH_HOST=$3
cp ${RTSAFE_LIBDIR}/*.so ${TARGET}/${DESTPATH}
cp ${LOGGING_3PP_LIBDIR}/*.so ${TARGET}/${DESTPATH}
cp  ../lib/*.so ${TARGET}/${DESTPATH}
tar cvf - -C ${TARGET}/ apps | ssh root@${SSH_HOST} tar xvf -
rm -rf ${TARGET}

