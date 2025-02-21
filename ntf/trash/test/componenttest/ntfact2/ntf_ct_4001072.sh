#! /bin/sh

##
## ntf_ct_4001072.sh --
##
##. 4001072	SMS	Expiration time	smstype0
##. 
##.    Verify that the parameter validity_smstype0 works. 
##. 
##.    Action: Set validity_smstype0 to 1 in notification.cfg. Verify that
##.    outdialwaitforphoneon is set to yes in the same file. Restart
##.    ntf. Send an outdial.
##. 
##.    Result: Check the sms-c to see that the expiration time of the
##.    smstype0 is the same as the one set in notification.cfg.
##
## Syntax;
##
##  ntf_ct_4001072.sh cfg-file
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

testcase=4001072
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
restart_smsc
prepare_mur
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;ODL;s,;1;;;;;default;;'
ssh $ntf_host "test -r $ntfcfg.save || cp $ntfcfg $ntfcfg.save"
cmd="echo validity_smstype0=1 >> $ntfcfg;
echo outdialwaitforphoneon=yes >> $ntfcfg"
ssh $ntf_host $cmd
restart_ntf norestore

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber
wait_for_sms

# Check the outcome. We are checking;
#  1. That 28 lines are logged. This is supposed to be one SMS.
#  2. That the "To:" address is "1,1,$phonenumber"
#  3. The "Validity period: 000000010000000R"
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq 28 || die ""
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^Validity period: 000000010000000R" $smslog > /dev/null || \
    die "Invalid Validity period"

# Clean-up
ssh $ntf_host mv -f $ntfcfg.save $ntfcfg

echo "Testcase $testcase PASSED"
