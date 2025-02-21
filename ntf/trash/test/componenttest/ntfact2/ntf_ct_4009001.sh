#! /bin/sh

##
## ntf_ct_4009001.sh --
##
##. 4009001	Slamdown	Slamdown
##. 
##.    Verify that slamdown calls are notified individually if slamdown
##.    list is not set
##. 
##.    Action: Configure NTF for regular slamdown by
##.    SlamdownList=No. Configure subscriber B for Slamdown Notification
##.    by the COS. The mobile phone for subscriber B is busy. Subscriber A
##.    tries to dial subscriber B but he got busy tone and A will be
##.    redirected to MoIP. Subscriber A hangs up the call before the
##.    record prompt is played.
##. 
##.    Result: A slamdown notification is sent to subscriber B for each
##.    slamdown call until he is no longer busy.
##
##  NOTE: This is basically the same test as TC-4001032 !!
##
## Syntax;
##
##  ntf_ct_4009001.sh cfg-file
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

testcase=4009001
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'
ssh $ntf_host "test -r $ntfcfg.save || cp $ntfcfg $ntfcfg.save"
ssh $ntf_host "echo SlamdownList=No >> $ntfcfg"
ssh $ntf_host "echo SmppBindType=Transceiver >> $ntfcfg"
ssh $ntf_host "echo SlamdownTimeOfLastCall=No >> $ntfcfg"
ssh $ntf_host "echo SourceAddress_slamdown=callers_number >> $ntfcfg"
ssh $ntf_host "echo SlamdownMaxCallers=1 >> $ntfcfg"
restart_ntf norestore

# Execute the test
echo "Execute test (wait 5sec) ..."
caller1=112200
caller2=223300
$send -t slam -t voice -c $caller1 $phonenumber
$send -t slam -t voice -c $caller2 $phonenumber
wait_for_sms
sleep 4

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslenx2 || \
    die "Invalid SMSC Log length (expected $smslenx2 lines)"

# Check the SMS order and fix caller values. The "send" script seems
# to add some (random?) number to the caller number, so we don't
# compare the last 2 digits.
caller1=1122
caller2=2233
if head -$smslen $smslog | grep "^From: .,.,$caller2" > /dev/null; then
    caller1=2233
    caller2=1122
fi

# Analyze SMS 1
head -$smslen $smslog | grep "^To: 1,1,$phonenumber" > /dev/null ||\
    die "Invalid To: field SMS1"
head -$smslen $smslog | grep "^From: 0,0,$caller1" > /dev/null || \
    die "Invalid From: field SMS1"
head -$smslen $smslog | grep "^Short message : You received a voice call from $caller1"\
    $smslog > /dev/null || die "Invalid Short message SMS1"

# Analyze SMS 2
tail -$smslen $smslog | grep "^To: 1,1,$phonenumber" > /dev/null ||\
    die "Invalid To: field SMS2"
tail -$smslen $smslog | grep "^From: 0,0,$caller2" > /dev/null || \
    die "Invalid From: field SMS2"
tail -$smslen $smslog | grep "^Short message : You received a voice call from $caller2"\
    $smslog > /dev/null || die "Invalid Short message SMS2"

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
