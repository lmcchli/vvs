#! /bin/sh

##
## ntf_ct_4007002.sh --
##
##. 4007002	SMS/MWI
##. 
##.    Verify that it is possible to send MWI on and sms in seperate
##.    messages to one smsc using splitmwiandsms=true
##
## Syntax;
##
##  ntf_ct_4007002.sh cfg-file
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

testcase=4007002
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
ssh $ntf_host "test -r $ntfcfg.save || cp $ntfcfg $ntfcfg.save"
ssh $ntf_host "echo splitmwiandsms=true >> $ntfcfg"
restart_ntf norestore
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS,MWI;s,;1;;;;;default;;'

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber
wait_for_sms


# Check the outcome. We are checking;
#  That 56 lines are logged. This is supposed to be two SMS'es.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslenx2 || \
    die "Unexpected length (expected $smslenx2 lines)"

# Now the problem is that we don't know the order of the SMS'es, which
# is first; the MWI or the "regular" SMS? Try to find out;
if head -$smslen $smslog | grep "^SMS length: 0" > /dev/null; then
    # We assume that the first SMS it the MWI message
    getmwi="head -$smslen $smslog"
    getsms="tail -$smslen $smslog"
else
    # We assume that the second SMS it the MWI message
    getmwi="tail -$smslen $smslog"
    getsms="head -$smslen $smslog"
fi

$getmwi | grep "^To: 1,1,$phonenumber" > /dev/null || die "Invalid To: field"
$getmwi | grep "^Data coding: 200 (MWI ON)" > /dev/null || \
    die "Invalid Data coding"
$getmwi | grep "^SMS length: 0" > /dev/null || die "Invalid SMS length"

$getsms | grep "^To: 1,1,$phonenumber" > /dev/null || die "Invalid To: field"
$getsms | grep "^Data coding: 0" > /dev/null || die "Invalid Data coding"
$getsms | grep "^Short message : You have a new voice message regarding" \
    > /dev/null || die "Invalid Short message"

# Clean-up
#ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
