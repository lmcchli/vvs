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

./starfish_engine.sh TC_KPN/tc_kpn_1.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_2.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_3.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_4.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_5.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_6.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_7.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_8.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_9.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_10.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_11.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_12.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_13.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_14.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_15.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_16.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_17.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_18.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_19.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_20.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_21.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_22.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_23.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_24.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_25.sh | tee -a $RESULT_LOG
./starfish_engine.sh TC_KPN/tc_kpn_27.sh | tee -a $RESULT_LOG
