#!/bin/sh
#
# ntf_restore.sh
#
# Copyright (C) 2007 Mobeon AB
#

LUBCKDIR=/apps/MOBYlubck

. $LUBCKDIR/conf/lu.conf


if [ "$RCSCRIPT" != "NULL" ]; then
        if [ -f $BACKUPDIR/$COMPONENT/etc/init.d/rc.$COMPONENT ]; then
                if [ -f /etc/init.d/rc.$COMPONENT ]; then
                        rm /etc/init.d/rc.$COMPONENT
                fi
                echo "restoring $BACKUPDIR/$COMPONENT/etc/init.d/rc.$COMPONENT to /etc/init.d..\c"
                cp $BACKUPDIR/$COMPONENT/etc/init.d/rc.$COMPONENT /etc/init.d/
                echo " ok"
		chmod +x /etc/init.d/rc.$COMPONENT
                ls -la /etc/init.d/rc.$COMPONENT
                echo
        else
                echo "$BACKUPDIR/$COMPONENT/etc/init.d/rc.$COMPONENT not found - aborted.."
                exit 1
        fi
else
         echo "rc script have value=$RCSCRIPT, no restore of the rc script will be performed"
fi

if [ "$SLINK" != "NULL" ]; then
        if [ -f $BACKUPDIR/$COMPONENT$SLINK ]; then
                if [ -h $SLINK ]; then
                        rm $SLINK
                fi
                echo "restoring symbolic link $SLINK pointing to /etc/init.d/rc.${COMPONENT}..\c"
                ln -s /etc/init.d/rc.$COMPONENT $SLINK
                echo " ok"
                ls -la $SLINK
                echo
        else
                echo "$SLINK not found .."
        fi
else
        echo "Start script (synbolic link) with value=$SLINK, no restore will be performed"
fi

