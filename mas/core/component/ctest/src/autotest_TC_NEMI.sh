#!/usr/bin/bash

curdate=`date '+%y%m%d-%H%M%S'` 		# Get the current date and time
MAIN_LOG="log/`basename $0 .sh`_mainlog_$curdate.log"	# The main logfile
ERROR_LOG="log/`basename $0 .sh`_errorlog_$curdate.log"	# Logfile for failed tests
RESULT_LOG="result/`basename $0 .sh`_results_$curdate.log" # Logfile for results
export MAIN_LOG
export ERROR_LOG
mas_host=`grep MAS_HOST ./starfish_engine.conf | awk -F= '{print $2}'`
mas_version=`ssh root@$mas_host '/etc/init.d/rc.mas version' | awk '{print $2}'`
echo "MAS: $mas_version: `date '+%y-%m-%d %H:%M:%S'`" | tee -a $RESULT_LOG

# Find all testcases and run them
TESTCASES=`find TC_NEMI/PlatformAccess -name "*.sh"`
for i in $TESTCASES; do
	./starfish_engine.sh $i | tee -a $RESULT_LOG
done
#	./starfish_engine.sh TC_NEMI/RADIUS/10002003.sh | tee -a $RESULT_LOG


# Append a summary
PASSED_TESTS=`grep "Result from test: Passed:" $RESULT_LOG | wc -l`
FAILED_TESTS=`grep "Result from test: Failed:" $RESULT_LOG | wc -l`
echo
echo "*****************************************************" >> $RESULT_LOG
echo "PASSED TESTS: $PASSED_TESTS" >> $RESULT_LOG
echo "FAILED TESTS: $FAILED_TESTS" >> $RESULT_LOG
echo "*****************************************************" >> $RESULT_LOG
