#! /bin/sh

##
## ntf_ct_4001005.sh --
##
##. 4001005	SMS	Connection to MUR
##. 
##.    Verify that notifications could still be sent if the connection to
##.    MUR has been lost.
##. 
##.    Action: Set the notification filter to notify for business hours
##.    only. Remove the billingnumber entry for one user and send 10
##.    messages to that user. Then send messages to other users. Check
##.    with the smsc if the notifications to the other users are sent.
##. 
##.    Result: The notifications to the other users should be sent to the
##.    smsc (without having to restart NTF).
##
## Syntax;
##
##  ntf_ct_4001005.sh cfg-file
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

testcase=4001005
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
$toolsd/setuserattr $cfg emFilter '1;y;b;evf;SMS;s,;1;;;;;default;;'
$toolsd/setattr $cfg uniqueidentifier=$uid2,$community \
    emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'

ldapdelete -D 'cn=Directory Manager' -w emmanager -p 389 -h $mur_host \
    billingnumber=$phonenumber,uniqueidentifier=$uid,$community


# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice -n 10 $phonenumber
$send -t voice $phonenumber2
wait_for_sms


# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || die "Unexpected length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber2" $smslog > /dev/null || die "Invalid To: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^Short message : You have a new voice message regarding" $smslog \
    > /dev/null || die "Invalid Short message:"

echo "Testcase $testcase PASSED"
