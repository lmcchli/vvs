#! /bin/sh

##
## ntf_ct_4018001.sh --
##
##. 4018001	SMS 	SourceAddress per CoS	voice
##. 
##.    Verify that is is possible to set SourceAddress_<service>_<CoS-name>.
##. 
##.    Action: 4 CoS'es shall be available with at least one user in each
##.    CoS having a filter with sms notification on all messages
##.    types. Set up notification.cfg according to
##.    Source_Address_voice_cos1=+1111, Source_Address_voice_cos2=+2222
##.    Source_Address_voice=+3333 and Source_Address=+9999. Send a
##.    notification on a voice message to one user in cos1, cos2,
##.    cos3. Send a notification on a videomessage to the user in cos4.
##. 
##.    Result: The SourceAddress shall be correct according to the
##.    configuration on the 4 notifications that arrives in the SMSC.
##
##  NOTE: The automatic test uses only 2 CoS'es, not 4. On the other
##     hand, voice, fax and video types are tested.
##
##  NOTE: This test is timing sensitive. Sometimes it gives a false
##     fail indication (sms-c log length invalid). Try to re-run the test.
##
## Syntax;
##
##  ntf_ct_4018001.sh cfg-file
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

testcase=4018001
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
restore_ntpconfig
cmd="cp $ntfcfg $ntfcfg.save;
echo sourceaddress_voice_segcos1=+2222 >> $ntfcfg;
echo sourceaddress_voice_segcos2=+3333 >> $ntfcfg;
echo sourceaddress_fax_segcos1=+4444 >> $ntfcfg;
echo sourceaddress_fax_segcos2=+5555 >> $ntfcfg;
echo sourceaddress_video_segcos1=+6666 >> $ntfcfg;
echo sourceaddress_video_segcos2=+7777 >> $ntfcfg"
ssh $ntf_host $cmd
restart_ntf norestore

$toolsd/setuserattr $cfg emFilter '1;y;a;mvf;SMS;s,;1;;;;;default;;'
$toolsd/setattr $cfg uniqueidentifier=$uid2,$community \
    emFilter '1;y;a;mvf;SMS;s,;1;;;;;default;;'

# Execute test for "voice"
echo "Execute for voice-type (wait 5sec) ..."
$send -t voice $phonenumber
$send -t voice $phonenumber2
wait_for_sms
sleep 3

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslenx2 || die "Unexpected len (expected $smslenx2)"

if head -$smslen $smslog | grep "^To: 1,1,20901000" > /dev/null; then
    sms1="head -$smslen $smslog"
    sms2="tail -$smslen $smslog"
else
    sms1="tail -$smslen $smslog"
    sms2="head -$smslen $smslog"
fi
$sms1 | grep "^From: 0,0,+2222" > /dev/null || die "Invalid From:"
$sms1 | grep "^Data coding: 0" > /dev/null || die "Invalid Data coding"
$sms1 | grep "^Short message : You have a new voice message regarding" \
    > /dev/null || die "Invalid Short message"
$sms2 | grep "^From: 0,0,+3333" > /dev/null || die "Invalid From:"
$sms2 | grep "^Data coding: 0" > /dev/null || die "Invalid Data coding"
$sms2 | grep "^Short message : You have a new voice message regarding" \
    > /dev/null || die "Invalid Short message"
echo "Test for voice PASSED"
sleep 8

# Execute test for "fax"
echo "Execute for fax-type (wait 5sec) ..."
restart_smsc
restart_ntf norestore
$send -t fax $phonenumber
$send -t fax $phonenumber2
wait_for_sms
sleep 3

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslenx2 || die "Unexpected len (expected $smslenx2)"

if head -$smslen $smslog | grep "^To: 1,1,20901000" > /dev/null; then
    sms1="head -$smslen $smslog"
    sms2="tail -$smslen $smslog"
else
    sms1="tail -$smslen $smslog"
    sms2="head -$smslen $smslog"
fi
$sms1 | grep "^From: 0,0,+4444" > /dev/null || die "Invalid From:"
$sms1 | grep "^Data coding: 0" > /dev/null || die "Invalid Data coding"
$sms1 | grep "^Short message : You have a new fax message regarding" \
    > /dev/null || die "Invalid Short message"
$sms2 | grep "^From: 0,0,+5555" > /dev/null || die "Invalid From:"
$sms2 | grep "^Data coding: 0" > /dev/null || die "Invalid Data coding"
$sms2 | grep "^Short message : You have a new fax message regarding" \
    > /dev/null || die "Invalid Short message"
echo "Test for fax PASSED"
sleep 8


# Execute test for "video"
echo "Execute for video-type (wait 5sec) ..."
restart_smsc
restart_ntf norestore
$send -t video $phonenumber
wait_for_sms

# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || die "Unexpected len (expected $smslen)"
sms1="head -$smslen $smslog"
$sms1 | grep "^To: 1,1,20901000" > /dev/null || die "Invalid To:"
$sms1 | grep "^From: 0,0,+6666" > /dev/null || die "Invalid From:"
$sms1 | grep "^Data coding: 0" > /dev/null || die "Invalid Data coding"
$sms1 | grep "^Short message : You have a new video message regarding" \
    > /dev/null || die "Invalid Short message"

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
