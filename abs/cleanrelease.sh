#!/bin/sh

LatestBuild=$1

BUILD_DIR=`pwd`

echo Cleaning release-pom.xml files with the plugin
mvn m4e-relpom:clean

echo Cleaning release-pom.xml files by hand because of bug with plugin ignore errors here
echo Please wait...
\rm `find .. -name release-pom.xml`
find $BUILD_DIR/.. -name release-pom.xml -exec rm {} \;
find $BUILD_DIR/../../vvsapps -name release-pom.xml -exec rm {} \;

echo Done

