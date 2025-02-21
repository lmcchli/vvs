#! /bin/sh

##
## ntf_ct_4003011.sh --
##
##. 4003011	MWI	MWIOff	Group
##. 
##.    Verify that a MWI off messages is sent to all users in the MWI off
##.    phone numbers list in the notification message body.
##. 
##.    Action: Send a group MWI off notification message with the send
##.    script. Use more than one phone number.
##. 
##.    Result: A MWI off SMS shall be sent to the SMS-C to all users.
##
## Syntax;
##
##  ntf_ct_4003011.sh cfg-file
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

testcase=4003011
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS,MWI;s,;1;;;;;default;;'
$toolsd/setattr $cfg uniqueidentifier=$uid2,$community \
    emFilter '1;y;a;evf;SMS,MWI;s,;1;;;;;default;;'

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -n 2 -t groupoff $phonenumber..$phonenumber2
wait_for_sms


# Check the outcome. We are checking;
#  1. That 56 lines are logged. This is supposed to be two SMS'es.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslenx2 || \
    die "Unexpected length (expected $smslenx2 lines)"

if head -$smslen $smslog | grep "^To: 1,1,$phonenumber" > /dev/null; then
    tail -$smslen $smslog | grep "^To: 1,1,$phonenumber2" > /dev/null ||\
        die "No SMS To: [$phonenumber2]"
else
    head -$smslen $smslog | grep "^To: 1,1,$phonenumber2" > /dev/null ||\
        die "No SMS To: [$phonenumber2]"
    tail -$smslen $smslog | grep "^To: 1,1,$phonenumber" > /dev/null ||\
        die "No SMS To: [$phonenumber]"
fi

head -$smslen $smslog | grep "^Data coding: 192 (MWI OFF)" > /dev/null ||\
    die "Invalid Data coding in SMS 1"
tail -$smslen $smslog | grep "^Data coding: 192 (MWI OFF)" > /dev/null ||\
    die "Invalid Data coding in SMS 2"

echo "Testcase $testcase PASSED"
