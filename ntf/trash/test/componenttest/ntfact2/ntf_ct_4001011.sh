#! /bin/sh

##
## ntf_ct_4001011.sh --
##
##. 4001011	SMS	Template	Cos Dependent
##.   
##.   Verify that ntf can have different templates per CoS. Action: Create
##.   two phrase-files and name them according to
##.   <language>-x-<CoS-name>.cphr format, e.g. en-x-cos1.cphr and
##.   en-x-cos2.cphr. Result: Check that ntf uses the correct template for
##.   each cos specified.
##
## Syntax;
##
##  ntf_ct_4001011.sh cfg-file
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

testcase=4001011
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh
test -n "$enphr" || die 'Not defined [$enphr]'

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
f=`echo /apps/ntf/templates/en.cphr | sed -e 's,[^/]*$,en-x-segcos2.cphr,'`
ssh $ntf_host "sed -e 's,message,TOMAT-message,' < $enphr > $f"
$toolsd/setattr $cfg "uniqueidentifier=$uid2,$community" \
    emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber2
wait_for_sms
ssh $ntf_host "rm $f"

# Check the outcome. We are checking;
#  1. That 28 lines are logged. This is supposed to be one SMS.
#  2. That the "To:" address is "1,1,$phonenumber"
#  3. That the message starts with "You have a new voice message regarding"
#     (which is supposed to match "templates/en.phr"
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || die "Unexpected length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber2" $smslog > /dev/null || die "Invalid To: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^Short message : You have a new voice TOMAT-message regarding" $smslog \
    > /dev/null || die "Invalid Short message:"

echo "Testcase $testcase PASSED"
