#! /bin/sh

##
## ntf_ct_4001003.sh --
##
##. 4001003	SMS	Event		
##. 
##.    Verify that sms notification will generate a correct MER event.
##. 
##.    Action: Send a message that will generate a SMS notification.
##. 
##.    Result: Check in /opt/mer/rep/C* or by using the Eventparser that
##.    the correct MER event,SMS (1), is sent.
##
##  NOTE: This test will try to get the correct event log-line from
##     /apps/mer/rep/C* file (note the typo above!) with "tail -1", but
##     that is a bit unsecure. Further it will use the "Eventparser" if it
##     is specified in the cfg-file, otherwise the log-record is analyzed
##     "manually" in this script.
##
##  NOTE2: The MER must be configured in etc/mer.cnf with;
##     <attr>
##       <mer name="FLUSH_REPOSITORY" value="1"/>
##     </attr>
##
## Syntax;
##
##  ntf_ct_4001003.sh cfg-file
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

# First test 4001001 is executed to get a event log record;

testcase=4001003
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# We need the mer_host for this test
test -n "$mer_host" || die "Not defined [mer_host]"

# Prepare for test
restart_smsc
prepare_mur
restart_ntf
$toolsd/setuserattr $cfg emFilter '1;y;a;evf;SMS;s,;1;;;;;default;;'

# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber
wait_for_sms

# Check the outcome part#1.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || die "Unexpected length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^Short message : You have a new voice message regarding" $smslog \
    > /dev/null || die "Invalid Short message:"

# Get the last line from the mer-log
logline=`ssh $mer_host "tail -1 /apps/mer/rep/C*"`
echo "Event log [$logline]"

if test -n "$eventparser"; then
    echo "Using [$eventparser] for analyzis ..."
    die "The Eventparser does not terminate so it can't be used"
    test -x "$eventparser" || die "Not executable [$eventparser]"
    #echo $logline | $eventparser -c `dirname $eventparser` &
    #sleep 1
    
else
    echo "Analysis without Eventparser ..."
    echo $logline | grep "un=$phonenumber@$mail_domain" > /dev/null ||\
        die "Invalid event (un)"
    echo $logline | grep "/pt=1/" > /dev/null || die "Invalid event (pt)"
fi

echo "Testcase $testcase PASSED"
