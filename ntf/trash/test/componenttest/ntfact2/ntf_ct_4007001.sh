#! /bin/sh

##
## ntf_ct_4007001.sh --
##
##. 4007001	SMS/MWI
##. 
##.    Verify that it is possible to send MWI on and sms in one message
##
## Syntax;
##
##  ntf_ct_4007001.sh cfg-file
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

testcase=4007001
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS,MWI;s,;1;;;;;default;;'

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber
wait_for_sms


# Check the outcome. We are checking;
#  1. That 28 lines are logged. This is supposed to be one SMS.
#  2. That the "To:" address is "1,1,$phonenumber"
#  3. That the message starts with "You have a new voice message regarding"
#     (which is supposed to match "templates/en.phr"
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq 28 || die "Unexpected length (expected 28 lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^Data coding: 216 (MWI ON WITH SMS)" $smslog > /dev/null ||\
    die "Invalid Data coding"
grep "^Short message : You have a new voice message regarding" $smslog \
    > /dev/null || die "Invalid Short message:"

echo "Testcase $testcase PASSED"
