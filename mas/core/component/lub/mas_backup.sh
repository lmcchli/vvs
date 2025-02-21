#!/bin/sh
#
# mas_backup.sh
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
        echo "copying $SLINK to $BACKUPDIR/$COMPONENT/etc/${SDIR}..\c"
        cp $SLINK $BACKUPDIR/$COMPONENT/etc/$SDIR
        echo " ok"
else
        if [ "$SLINK" != "NULL" ]; then
                echo "$SLINK not found .."
        fi
fi

if [ -h $KLINK ]; then
        echo "copying $KLINK to $BACKUPDIR/$COMPONENT/etc/${KDIR}..\c"
        cp $KLINK $BACKUPDIR/$COMPONENT/etc/$KDIR
        echo " ok"
else
        if [ "$KLINK" != "NULL" ]; then
                echo "$KLINK not found .."
        fi
fi

if [ -f /etc/system ]; then
        echo "copying /etc/system to $BACKUPDIR/$COMPONENT/etc/system\c"
        cp /etc/system $BACKUPDIR/$COMPONENT/etc/system
        echo " ok"
else
        echo "no /etc/system file found"
fi

#


for file in /etc/sfw/mobeon/MOBYmas/mas /etc/sfw/mobeon/MOBYmas/snmpagent /var/svc/manifest/application/mobeon/MOBYmas/MOBYmas.xml
do
        if [ -f $file ]; then
                echo "copying $file to $BACKUPDIR/$COMPONENT/..\c"
                cp $file $BACKUPDIR/$COMPONENT
                echo " ok"
        else
                echo "$file not found please check!!"
                echo
        fi
done

