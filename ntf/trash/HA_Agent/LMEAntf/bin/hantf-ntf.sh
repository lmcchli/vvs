#! /bin/sh
#
# hantf-ntf.sh, start script for the NTF component
#
BASEDIR=`pkgparam LMENtf BASEDIR`

if [ $# -ne 1 ]; then
	echo "ERROR: Syntax `basename $0` <ntf instance home>"
	exit 99
fi

JAVA_HOME=`pkgparam SUNWj5rt BASEDIR`

INSTANCEHOME=$1

LC_CTYPE=iso8859-1
LC_NUMERIC=iso8859-1
LC_COLLATE=iso8859-1
LC_MONETARY=iso8859-1
LC_MESSAGES=iso8859-1

#
# Make sure that the locale is properly initialized
#
export LC_CTYPE LC_NUMERIC LC_COLLATE LC_MONETARY LC_MESSAGES

CLASSPATH=$BASEDIR/lib/classes:\
$BASEDIR/bin/ntf.jar:\
$BASEDIR/bin/hsqldb.jar:\
$BASEDIR/bin/smpp.jar:\
$BASEDIR/bin/foundation.jar:\
$BASEDIR/bin/ldapjdk.jar:\
$BASEDIR/bin/ldapfilt.jar:\
$BASEDIR/bin/ldapsp.jar:\
$BASEDIR/bin/xercesImpl.jar:\
$BASEDIR/bin/xmlParserAPIs.jar:\
$BASEDIR/bin/mail.jar:\
$BASEDIR/bin/activation.jar:\
$BASEDIR/bin/radiusapi.jar:\
$BASEDIR/bin/ess_api_java.jar:\
$BASEDIR/bin/apache-activemq-4.1.1.jar

if cd $INSTANCEHOME/logs; then
    $JAVA_HOME/bin/java -server -cp $CLASSPATH -mx256m -DntfHome=$INSTANCEHOME -DconfigFile=$INSTANCEHOME/cfg/notification.cfg com.mobeon.ntf.NtfMain > $INSTANCEHOME/logs/NotificationProcess.log 2>&1 &
fi
