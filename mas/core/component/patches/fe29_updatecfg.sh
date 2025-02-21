#!/bin/sh

PRG_NAME=$0

if [ $# != 1 ];then
  echo "Usage: $PRG_NAME <MAS HOME>"
  exit 1
fi

MAS_HOME=$1
CFG_FILE=$MAS_HOME/cfg/mas.xml

if [ ! -d $MAS_HOME ];then
  echo $MAS_HOME does not exist.
  exit 1
fi

if [ ! -f $CFG_FILE ];then
  echo $CFG_FILE does not exist.
  exit 1
fi


params="-c stream.silencedetectionmode=0"
params=$params" -s stream.silencedetectionmode=0"
params=$params" -c stream.silencethreshold=0"
params=$params" -s stream.silencethreshold=0"
params=$params" -c stream.initialsilenceframes=40"
params=$params" -s stream.initialsilenceframes=40"
params=$params" -c stream.signaldeadband=10"
params=$params" -s stream.signaldeadband=10"
params=$params" -c stream.silencedeadband=150"
params=$params" -s stream.silencedeadband=150"
params=$params" -c stream.detectionframes=10"
params=$params" -s stream.detectionframes=10"
params=$params" -c profilemanager.attributemap.emusersd.writelevel=user"
params=$params" -s profilemanager.attributemap.emusersd.writelevel=user"

$MAS_HOME/bin/CfgTool $params $CFG_FILE

echo "Config updated!"

