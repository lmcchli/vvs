#! /bin/sh

##
## ntf_ct_4009004.sh --
##
##. 4009004	Slamdown	SlamdownList
##. 
##.    Verify that slamdown calls are notified grouped together if
##.    slamdown list is set.
##. 
##.    Action: Configure NTF for slamdown list by
##.    SlamdownList=Yes. Configure subscriber B for Slamdown Notification
##.    by the COS. The mobile phone for subscriber B is busy. Subscriber A
##.    tries to dial subscriber B but he got busy tone and A will be
##.    redirected to MoIP. Subscriber A hangs up the call before the
##.    record prompt is played.
##. 
##.    Result: When subscriber B is no longer busy one or more
##.    slamdownlist notification/s is sent to him containing a list of all
##.    slamdows made.
##
## Syntax;
##
##  ntf_ct_4009004.sh cfg-file
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

testcase=4009004
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'
ssh $ntf_host "test -r $ntfcfg.save || cp $ntfcfg $ntfcfg.save"
ssh $ntf_host "echo SlamdownList=Yes >> $ntfcfg"
ssh $ntf_host "echo SmppBindType=Transceiver >> $ntfcfg"
ssh $ntf_host "echo SlamdownTimeOfLastCall=No >> $ntfcfg"
ssh $ntf_host "echo SlamdownMaxCallers=0 >> $ntfcfg"
restart_ntf norestore

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t slam -t voice -c 112233 $phonenumber
$send -t slam -t voice -c 223344 $phonenumber
$send -t slam -t voice -c 334455 $phonenumber
$send -t slam -t voice -c 445566 $phonenumber
wait_for_sms

# The first sms is a "type0". The SMS with the caller-list appears
# after 30 seconds, so now we wait ...
echo "Waiting 30 sec ..."
sleep 30
echo "Waiting for another 10 sec ..."
sleep 10

# Check the outcome.
# The caller-list SMS extra lines for the caller-list, so the expected length
# is $smslenx2 + 5;
explen=61
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $explen || \
    die "Invalid SMSC Log length (expected $explen lines)"

# Analyze the caller-list SMS
tail -33 $smslog | grep "^To: 1,1,$phonenumber" > /dev/null ||\
    die "Invalid To: field"
tail -33 $smslog | grep "^Short message : Callers:" > /dev/null ||\
    die "Invalid Short message"
tail -10 $smslog | grep "^1122" > /dev/null || die "Missing caller: 1122xx"
tail -10 $smslog | grep "^2233" > /dev/null || die "Missing caller: 2233xx"
tail -10 $smslog | grep "^3344" > /dev/null || die "Missing caller: 3344xx"
tail -10 $smslog | grep "^4455" > /dev/null || die "Missing caller: 4455xx"

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
