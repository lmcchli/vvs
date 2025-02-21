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

./starfish_engine.sh TC_TMO/tc_tmo_3.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_4.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_5.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_6.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_7.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_8.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_9.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_10.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_11.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_12.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_13.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_17.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_18.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_19.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_20.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_21.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_22.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_23.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_24.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_25.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_27.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_28.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_29.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_30.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_31.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_41.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_42.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_46.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_47.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_48.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_49.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_50.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_51.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_52.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_53.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_54.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_55.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_TMO/tc_tmo_56.sh | tee -a $RESULT_LOG

