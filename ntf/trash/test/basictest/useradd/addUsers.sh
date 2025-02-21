#!/bin/ksh
BASEDIR=/vobs/ipms/ntf/bin
CLASSPATH=$BASEDIR/ldapfilt.jar:$BASEDIR/ldapjdk.jar:$BASEDIR/ldapsp.jar:.

javac -d . -classpath $CLASSPATH UserAdd.java 
java -cp $CLASSPATH useradd.UserAdd $1
