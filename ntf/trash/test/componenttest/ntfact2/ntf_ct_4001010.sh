#! /bin/sh

##
## ntf_ct_4001010.sh --
##
##. 4001010	SMS	Template	Language Dependent
##. 
##.    Verify that the correct language is used in the SMS notification. 
##. 
##.    Action: Change preferred language on the user and then send a SMS. 
##. 
##.    Result: The language in the SMS should be the same as the language
##.    set as preferred language and displaying the correct content
##.    (according to .phr file used).
##
## Syntax;
##
##  ntf_ct_4001010.sh cfg-file
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

testcase=4001010
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'
$toolsd/setuserattr $cfg preferredLanguage sv

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
test "0$lnc" -eq $smslen || die "Unexpected length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^Short message : Du har ett nytt röstmeddelande angående" $smslog \
    > /dev/null || die "Invalid Short message:"

echo "Testcase $testcase PASSED"
