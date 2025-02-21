#! /bin/sh

##
## ntf_ct_4005001.sh --
##
##. 4005001	Outdial 	Notification Number
##. 
##.    Verify that Outdial notification works with Notification Number.
##. 
##.    Action: Send a message that will get an Outdial notification to a
##.    users notification number.
##. 
##.    Result: The Outdial notification will be delivered to the
##.    notification number set on the user.
##
##  NOTE: Only the "type0" SMS is checked, NOT the outdial itself!!
##     We can check that the "outdial sequence has started" and prone
##     numbers. But the actual outdial might fail without beeing detected.
##
## Syntax;
##
##  ntf_ct_4005001.sh cfg-file
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

testcase=4005001
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;ODL;s,;1;;;;;default;;'

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber
wait_for_sms


# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || die "Unexpected length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^SMS length: 0" $smslog > /dev/null || die "Invalid SMS length"

echo "Testcase $testcase PASSED"
