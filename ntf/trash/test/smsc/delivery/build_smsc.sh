#!/bin/sh
cd `dirname $0`
DELIVERY=`pwd`
echo "Creating delivery in $DELIVERY"
cd $DELIVERY
echo "Enter version of the SMSC simulator (e.g. R2A): \c"
read VERSION
echo "VERSION=$VERSION" >VERSION
VERSION_LOWER=`echo $VERSION | tr '[:upper:]' '[:lower:]'`
pwd
cd ../src/
pwd
echo "Building smsc..."
clearmake -V -C gnu || exit
cd $DELIVERY
pwd
echo "Emptying tar directory"
rm -f smsc/*/* smsc/Mobeon_SMSC_Simulator_manual.pdf
echo "Copying files to tar directory"
cp ../bin/* smsc/bin
cp ../cfg/* smsc/cfg
cp ../doc/Mobeon_SMSC_Simulator_manual.pdf smsc
cp VERSION smsc
echo "Making tar file"
tar cvf "smsc_$VERSION_LOWER.tar" smsc
echo "Labelling files"
cleartool mklbtype -nc Mobeon_SMSC_Simulator_$VERSION
cleartool mklabel -recurse -replace Mobeon_SMSC_Simulator_$VERSION ..
