#! /bin/sh

#
# ntf_ct_functions.sh --
#
#  Contains fommon functions for the automatic CT for NTF
#

# Defaults
send=/apps/tools/send
smscd=/apps/tools/smsc
ntfcfg=/apps/ntf/cfg/notification.cfg
smslen=28
smslenx2=56

die() {
    echo "ERROR: $*" >&2
    exit 1
}
help() {
    grep '^##' $0 | cut -c4-
    exit 0
}
test -n "$1" || help

absolute_path() {
    rpath=$1
    if echo $rpath| grep "^/" > /dev/null; then
        echo $rpath
    else
        echo $top/$rpath
    fi
}

# Save command line options
argv=$*

# Get configuration and check some items
test -r "$1" || die "Not readable [$1]"
cfg=`absolute_path $1`
. $cfg
test -n "$ntf_host" || die 'Not specified [$ntf_host]'
test -n "$mur_host" || die 'Not specified [$mur_host]'
test -n "$ms_host" || die 'Not specified [$ms_host]'
test -n "$uid" || die 'Not specified [$uid]'
test -n "$phonenumber" || die 'Not specified [$phonenumber]'
test -n "$uid2" || die 'Not specified [$uid2]'
test -n "$phonenumber2" || die 'Not specified [$phonenumber2]'
test -n "$mail_domain" || die 'Not specified [$mail_domain]'
test -x "$send" || die "Not executable [$send]"

# Setup environment for the "send" script
TEST_MAILHOST=$ms_host; export TEST_MAILHOST
TEST_DOMAIN=$mail_domain; export TEST_DOMAIN


restart_smsc() {
    echo "Re-starting SMSC ..."
    cd $smscd/bin
    test -x ./rc.smsc || die "Not executable [$smscd/bin/rc.smsc]"
    ./rc.smsc stop
    rm $smslog
    ./rc.smsc start
    cd $top
}

prepare_mur() {
    echo "Prepare the MUR ..."
    $toolsd/restoremur $cfg
    $toolsd/preparemur $cfg
}

restore_ntpconfig() {
    if ssh $ntf_host test -r $ntfcfg.save; then
        echo " (backup [$ntfcfg.save] exists, restoring ...)"
        ssh $ntf_host mv -f $ntfcfg.save $ntfcfg
    fi    
}

restart_ntf() {
    echo "Re-starting NTF (and wait 8sec) ..."
    test "$1" != "norestore" && restore_ntpconfig
    ssh $ntf_host /etc/init.d/rc.ntf stop
    ssh $ntf_host rm -f /apps/ntf/logs/NotificationTrace.log
    ssh $ntf_host /etc/init.d/rc.ntf start
    sleep 8
}

wait_for_sms() {
    echo "Waiting for SMS ..."
    sleep 5
    for n in 1 2 3 4 5 6 7 8 9 10; do
        test -s $smslog || sleep 3
    done
    test -s $smslog || die "No SMS"
}

