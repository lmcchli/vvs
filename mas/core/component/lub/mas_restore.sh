#!/bin/sh
#
# mas_restore.sh
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
                echo "$BACKUPDIR/$COMPONENT/etc/init.d/rc.$COMPONENT not found.."
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
fi

if [ "$KLINK" != "NULL" ]; then
        if [ -f $BACKUPDIR/$COMPONENT$KLINK ]; then
                if [ -h $KLINK ]; then
                        rm $KLINK
                fi
                echo "restoring symbolic link $KLINK pointing to /etc/init.d/rc.${COMPONENT}..\c"
                ln -s /etc/init.d/rc.$COMPONENT $KLINK
                echo " ok"
                ls -la $KLINK
                echo
        else
                echo "$KLINK not found .."
        fi
fi

#

        if [ -f $BACKUPDIR/$COMPONENT/etc/system ]; then
                if [ -f /etc/system ]; then
                        rm -f /etc/system
                fi
                echo "restoring $BACKUPDIR/$COMPONENT/etc/system to /etc/system \c"
                cp $BACKUPDIR/$COMPONENT/etc/system /etc/system
                echo " ok"
                chmod 644 /etc/system
                ls -la /etc/system
                echo
        else
                echo "$BACKUPDIR/$COMPONENT/etc/system not found.."
        fi


        if [ -d /etc/sfw/mobeon ]; then
                :
        else
                mkdir /etc/sfw/mobeon
        fi

        if [ -d /etc/sfw/mobeon/MOBYmas ]; then
                :
        else
                mkdir /etc/sfw/mobeon/MOBYmas
        fi

        if [ -f $BACKUPDIR/$COMPONENT/mas ]; then
                echo "restoring file $BACKUPDIR/$COMPONENT/mas to /etc/sfw/mobeon/MOBYmas/..\c"
                cp $BACKUPDIR/$COMPONENT/mas /etc/sfw/mobeon/MOBYmas/mas
                echo " ok"
                ls -la /etc/sfw/mobeon/MOBYmas/mas
        else
                echo "$BACKUPDIR/$COMPONENT/mas not found.."
        fi

        if [ -f $BACKUPDIR/$COMPONENT/snmpagent ]; then
                echo "restoring file $BACKUPDIR/$COMPONENT/snmpagent to /etc/sfw/mobeon/MOBYmas/..\c"
                cp $BACKUPDIR/$COMPONENT/snmpagent /etc/sfw/mobeon/MOBYmas/snmpagent
                echo " ok"
                ls -la /etc/sfw/mobeon/MOBYmas/snmpagent
        else
                echo "$BACKUPDIR/$COMPONENT/snmpagent not found.."
        fi

        if [ -d /var/svc/manifest/application/mobeon ]; then
                :
        else
                mkdir /var/svc/manifest/application/mobeon
        fi

        if [ -d /var/svc/manifest/application/mobeon/MOBYmas ]; then
                :
        else
                mkdir /var/svc/manifest/application/mobeon/MOBYmas
        fi
        if [ -f $BACKUPDIR/$COMPONENT/MOBYmas.xml ]; then
                echo "restoring file $BACKUPDIR/$COMPONENT/MOBYmas.xml to /var/svc/manifest/application/mobeon/MOBYmas/..\c"
                cp $BACKUPDIR/$COMPONENT/MOBYmas.xml /var/svc/manifest/application/mobeon/MOBYmas/MOBYmas.xml
                echo " ok"
                ls -la /var/svc/manifest/application/mobeon/MOBYmas/MOBYmas.xml

                svccfg import /var/svc/manifest/application/mobeon/MOBYmas/MOBYmas.xml
                svcadm enable application/mobeon/MOBYmas:mas
                svcadm enable application/mobeon/MOBYmas:snmpagent
        else
                echo "$BACKUPDIR/$COMPONENT/snmpagent not found.."
                echo "No svcs activating of component have been performed!!!"
        fi
