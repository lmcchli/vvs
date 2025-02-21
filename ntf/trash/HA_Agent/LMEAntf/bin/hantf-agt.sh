#! /bin/sh
#
BASEDIR=`pkgparam LMENtf BASEDIR`

if [ $# -ne 1 ]; then
	echo "ERROR: Syntax `basename $0` <ntf instance home>"
	exit 99
fi

INSTANCEHOME=$1
PORT=`$BASEDIR/bin/getconfig snmpagentport $INSTANCEHOME/cfg/notification.cfg`
CONTEXT=`$BASEDIR/bin/getconfig MCR_INSTANCE_NAME $INSTANCEHOME/cfg/notification.cfg`
CONTEXT=`echo "$CONTEXT" | cut -f2- -d@ | sed 's%//.*%%'`

if [ -z "$CONTEXT" ];then
	CONTEXT="public"
fi

if [ -z "$PORT" ]; then
	PORT="18001"
fi

if cd $INSTANCEHOME/logs; then 
    $BASEDIR/bin/ntfagent -d $INSTANCEHOME $PORT $CONTEXT > $INSTANCEHOME/logs/ntfagent.log 2>&1
fi
