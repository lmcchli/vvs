#! /bin/sh

##
## ntf_ct_4001070.sh --
##
##. 4001070	SMS	SMSCBackup	High level
##. 
##.    With backup. Send 1000 mail at top speed, start and stop both the
##.    primary and the backup SMSC repeatedly during the test and verify
##.    that all notifications arrive for the SMSCs.
##
##  NOTE: This test has to be executed manually. This script only
##     helps with setup/restore of the environment.
##
##     send -t voice -n 1000 20901000..20901999
##
##  NOTE: Wait *at least* 10 minutes before checking the sms-logs.
##     If the NTF fails to send a notification the message is re-queued
##     and retried after 10 minutes.
##
## Syntax;
##
##  ntf_ct_4001070.sh cfg-file [setup|restore]
##
## Description;
##
##  This script must be executed on the processor where SMSC runs. It
##  needs the "send" program (default /apps/tools/send).
##
## SSH;
##
##  Ssh is used to execute programs on other hosts (the ntf_host). To
##  avoid typing passwd all the time, create a ~/.ssh/authorized_keys
##  on that host.
##

testcase=4001070
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

test -n "$2" || help

mcrbase="emRegisterName=MessagingComponentRegister,o=config"
smsc2rdn="emComponentName=SMSC2,emServiceName=ShortMessage"

if test "$2" = "setup"; then

    echo "Re-starting SMSC2 ..."
    cmd="cd $smsc2d/bin;./rc.smsc stop;rm $smsc2log;./rc.smsc start"
    ssh $smsc2_host $cmd

    echo "Register SMSC2 in the MCR ..."
    ldapdelete -D 'cn=Directory Manager' -w emmanager -p 389 -h $mur_host\
        $smsc2rdn,$mcrbase
    ldapadd -D 'cn=Directory Manager' -w emmanager -p 389 -h $mur_host <<EOF
version: 1
dn: $smsc2rdn,$mcrbase
emhostname: $smsc2_host
emcomponentname: SMSC2
emhostnumber: $smsc2_ip
ipServicePort: 5016
emcomponenttype: smsc
emcomponentversion: V3.0
emrootoid: none
ipServiceProtocol: smpp
objectClass: top
objectClass: emcomponent
EOF

    # Prepare for test
    restart_smsc
    prepare_mur
    # Remove ODL
    ldapmodify -D 'cn=Directory Manager' -w emmanager -p 389 -h $mur_host <<EOF
dn: cos=2,$community
changetype: modify
replace: emFilter
emFilter: 1;y;a;evf;SMS;s,;1;;;;;default;;
EOF
    restore_ntpconfig
    ssh $ntf_host "cp $ntfcfg $ntfcfg.save"
    ssh $ntf_host "echo 'smscbackup=SMSC>SMSC2' >> $ntfcfg"
    restart_ntf norestore
    $toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'

elif test "$2" = "restore"; then

    # Restore after test
    echo "Restoring ..."
    ldapdelete -D 'cn=Directory Manager' -w emmanager -p 389 -h $mur_host\
        $smsc2rdn,$mcrbase
    restore_ntpconfig

else

    die "Invalid cmd [$2]"

fi
