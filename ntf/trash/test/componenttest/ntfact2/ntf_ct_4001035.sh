#! /bin/sh

##
## ntf_ct_4001035.sh --
##
##. 4001035	SMS	Sourceaddress	additional parameter
##. 
##.    Verify that it is possible to add an additional parameter in
##.    notification.cfg.
##. 
##.    Action: Set smesourceaddress_additional to "0,0,additional" in
##.    notification.cfg. In systemnotification.cfg add a parameter called
##.    additional and specify how to identify this parameter (eg subject
##.    or from). Set additional in en.phr to a string that you want to be
##.    shown in the sms. Restart ntf. Send a message that fulfil the
##.    characteristics of the parameter.
##. 
##.    Result: Check the sms-c to see that the values of the sms is the
##.    same as the ones set in notification.cfg.
##
## Syntax;
##
##  ntf_ct_4001035.sh cfg-file
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

testcase=4001035
top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh

# Prepare for test
test -n "$ntfsystemcfg" || die "Not defined [ntfsystemcfg]"
test -n "$enphr" || die "Not defined [enphr]"
ssh $ntf_host test -r $ntfsystemcfg.save && \
    die "Backup exists [$ntfsystemcfg.save]"
ssh $ntf_host test -r $enphr.save && \
    die "Backup exists [$enphr.save]"
restore_ntpconfig
cmd="cp $ntfcfg $ntfcfg.save; cp $ntfsystemcfg $ntfsystemcfg.save;
cp $enphr $enphr.save; chmod u+w $ntfsystemcfg $enphr;
echo sourceaddress_additional=0,0,additional >> $ntfcfg;
echo '[additional]' >> $ntfsystemcfg;
echo 'Subject=voice message' >> $ntfsystemcfg;
echo 'additional = {' >> $enphr;
echo '\"AN ADDITIONAL MESSAGE\"' >> $enphr;
echo '}' >> $enphr"
ssh $ntf_host $cmd
restart_smsc
prepare_mur
restart_ntf norestore


# Execute the test
echo "Execute test (wait 5sec) ..."
$send -t voice $phonenumber
wait_for_sms

# Restore config
cmd="mv -f $ntfsystemcfg.save $ntfsystemcfg;
mv -f $enphr.save $enphr; mv $ntfcfg.save $ntfcfg"
ssh $ntf_host $cmd


# Check the outcome.
echo "Analyzing [$smslog]"
lnc=`wc -l $smslog | cut -c-9 | tr -d ' '`
test "0$lnc" -eq $smslen || die "Unexpected length (expected $smslen lines)"
grep "^To: 1,1,$phonenumber" $smslog > /dev/null || die "Invalid To: field"
grep "^From: 0,0,additional" $smslog > /dev/null || die "Invalid From: field"
grep "^Data coding: 0" $smslog > /dev/null || die "Invalid Data coding"
grep "^Short message : AN ADDITIONAL MESSAGE" $smslog \
    > /dev/null || die "Invalid Short message:"

echo "Testcase $testcase PASSED"
