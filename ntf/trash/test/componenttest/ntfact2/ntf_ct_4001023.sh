#! /bin/sh

##
## ntf_ct_4001023.sh --
##
##. 4001023	SMS	Sourceaddress	SMSType0
##. 
##.    Verify that the parameter sourceaddress_smstype0 works.
##. 
##.    Action: Set sourceaddress_smstype0 to ""1,1,SMSType0"" in
##.    notification.cfg. Verify that outdialwaitforphoneon is set to yes
##.    in the same file. Restart ntf. Send an outdial.
##. 
##.    Result: Check the sms-c to see that the values of the smstype0 is
##.    the same as the ones set in notification.cfg. (Also check for
##.    outdial message in NotificationTrace.log)
##
## Syntax;
##
##  ntf_ct_4001023.sh cfg-file
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

testcase=4001023
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;ODL;s,;1;;;;;default;;'
restore_ntpconfig
ssh $ntf_host "cp $ntfcfg $ntfcfg.save"
ssh $ntf_host "echo sourceaddress_smstype0=1,1,SMSType0 >> $ntfcfg"
restart_ntf norestore

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice -c 123400 $phonenumber
wait_for_sms

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || \
    die "Invalid SMSC Log length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^From: 1,1,SMSType0" $smslog > /dev/null || \
    die "Invalid From: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^SMS length: 0" $smslog > /dev/null || die "Invalid SMS length"

echo "Testcase $testcase PASSED"
