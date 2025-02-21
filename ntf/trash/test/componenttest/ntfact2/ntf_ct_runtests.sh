#! /bin/sh

##
## ntf_ct_runtests.sh --
##
##  Run the automated CT for NTF.
##
## Syntax;
##
##  ntf_ct_runtests.sh cfg-file [keeplog]
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


top=`pwd`
toolsd=`dirname $0`
. $toolsd/ntf_ct_functions.sh
logfile=/tmp/ntf_ct_runtests_log_$$.txt
keeplog="no"
test "$2" = "keeplog" && keeplog="yes"

testcases="
    4001001 4001002 4001003 4001005 4001010 4001011 4001015 4001023
    4001032 4001034 4001035 4001037 4001039 4001072 4002001 4002002
    4003011 4005001 4005002 4007001 4007002 4009001 4009004
    4018001"

echo "Test start `date`"
echo "Test start `date`" > $logfile
echo "Logging to [$logfile]"

for n in $testcases; do
    echo "Running TC $n ..."
    tscript=$toolsd/ntf_ct_$n.sh
    test -x $tscript || die "Not executable [$tscript]"
    if $tscript $cfg >> $logfile 2>&1; then
        echo "PASSED TC: [$n]"
    else
        echo "==========> FAILED TC: [$n] !!"
    fi
done

echo "Test ended `date`" > $logfile
test "$keeplog" != "yes" && rm -f $logfile
echo "Test ended `date`"
