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

./starfish_engine.sh TC_RADIUS/20007070.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007071.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007072.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007073.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007074.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007075.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007076.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007077.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_RADIUS/20007078.sh | tee -a $RESULT_LOG
