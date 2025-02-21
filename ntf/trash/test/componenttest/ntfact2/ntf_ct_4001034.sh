#! /bin/sh

##
## ntf_ct_4001034.sh --
##
##. 4001034	SMS	Sourceaddress	smesourceaddress
##. 
##.    Verify that the parameter smesourceaddress works.
##. 
##.    Action: Set smesourceaddress to "1,1,smesourceaddress" in
##.    notification.cfg. Restart ntf. Send an message that will get an sms
##.    as notification.
##. 
##.    Result: Check the sms-c to see that the values of the sms is the
##.    same as the ones set in notification.cfg.
##
## Syntax;
##
##  ntf_ct_4001034.sh cfg-file
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

testcase=4001034
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'
restore_ntpconfig
ssh $ntf_host "cp $ntfcfg $ntfcfg.save"
ssh $ntf_host "echo smesourceaddress=0,0,smesourceaddress >> $ntfcfg"
restart_ntf norestore

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice -c 123400 $phonenumber
wait_for_sms

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || \
    die "Invalid SMSC Log length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^From: 0,0,smesourceaddress" $smslog > /dev/null || \
    die "Invalid From: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^Short message : You have a new voice message regarding" $smslog \
    > /dev/null || die "Invalid Short message"

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
