#!/usr/bin/sh

CLASSPATH=../lib/mobeon_numberanalyzer.jar:../lib/mobeon_execution_engine.jar:../lib/mobeon_configurationmanager.jar:../lib/mobeon_logging.jar:../lib/log4j-1.2.9.jar:../lib/dom4j-1.6.1.jar:../lib/commons-collections-3.1.jar 

java -classpath $CLASSPATH com.mobeon.masp.numberanalyzer.NumberAnalyzer -f $1 -r $2 -n $3 -a $4
