#!/bin/sh
#
# ntf_backup.sh
#
# Copyright (C) 2007 Mobeon AB
#

LUBCKDIR=/apps/MOBYlubck

. $LUBCKDIR/conf/lu.conf


if [ -f /etc/init.d/rc.$COMPONENT ]; then
        echo "copying /etc/init.d/rc.$COMPONENT to $BACKUPDIR/$COMPONENT/etc/init.d..\c"
        cp /etc/init.d/rc.$COMPONENT $BACKUPDIR/$COMPONENT/etc/init.d/
        echo " ok"
else
        if [ "$RCSCRIPT" != "NULL" ]; then
                echo "$RCSCRIPT not found .."
                exit
        else
                echo "no rc script configured in $COMPONENT.conf file"
        fi
fi

if [ -h $SLINK ]; then
        echo "copying $SLINK to $BACKUPDIR/$COMPONENT/etc/rc3.d..\c"
        cp $SLINK $BACKUPDIR/$COMPONENT/etc/rc3.d
        echo " ok" 
else
        if [ "$SLINK" != "NULL" ]; then
                echo "$SLINK not found .."
        else
                echo "no S(tart) script configured in $COMPONENT.conf file"
        fi
fi

