#! /bin/sh

##
## ntf_ct_4001032.sh --
##
##. 4001032	Slamdown Sourceaddress	Missed Call Indicator-Slamdown	
##. 
##.    Verify that it's possible to set the calling party number as the
##.    source address.
##. 
##.    Action: Set the following parameters in notification.cfg:
##.      SlamdownList=Yes
##.      SmppBindType=Transceiver
##.      SlamdownTimeOfLastCall=No
##.      SourceAddress_slamdown=callers_number
##.      SlamdownMaxCallers=1
##. 
##.    Result: A slamdown notification is sent with the source address set
##.    to the calling party number.
##. 
## Syntax;
##
##  ntf_ct_4001032.sh cfg-file
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

testcase=4001032
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'
restore_ntpconfig
ssh $ntf_host "cp $ntfcfg $ntfcfg.save"
# Don't use SlamdownList here since it complicates the test
ssh $ntf_host "echo SlamdownList=No >> $ntfcfg"
ssh $ntf_host "echo SmppBindType=Transceiver >> $ntfcfg"
ssh $ntf_host "echo SlamdownTimeOfLastCall=No >> $ntfcfg"
ssh $ntf_host "echo SourceAddress_slamdown=callers_number >> $ntfcfg"
ssh $ntf_host "echo SlamdownMaxCallers=1 >> $ntfcfg"
restart_ntf norestore

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t slam -t voice -c 123400 $phonenumber
wait_for_sms

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || \
    die "Invalid SMSC Log length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^From: 0,0,1234" $smslog > /dev/null || \
    die "Invalid From: field"
grep "^Short message : You received a voice call from 1234" $smslog \
    > /dev/null || die "Invalid Short message"

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
