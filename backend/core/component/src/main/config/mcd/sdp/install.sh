#!/bin/bash
echo 'executing install.sh...'

BUNDLE_NAME=$1

# Install the RPMs in the order listed below
osRpmInstall $BUNDLE_NAME *.rpm
if [ $? != 0 ]; then
    exit 1
fi



exit 0

