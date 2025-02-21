#!/bin/bash
echo 'executing remove.sh...'

BUNDLE_NAME=$1

# Remove the RPMs in the order listed below
osRpmRemove $BUNDLE_NAME *.rpm
if [ $? != 0 ]; then
    exit 1
fi



exit 0

