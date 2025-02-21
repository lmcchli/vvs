#!/bin/bash

# COPYRIGHT (c) Abcxyz Communications Inc. Canada (LMC)
# All Rights Reserved.
#
# The copyright to the computer program(s) herein is the property
# of Abcxyz Communications Inc. Canada (LMC). The program(s) may
# be used and/or copied only with the written permission from
# Abcxyz Communications Inc. Canada (LMC) or in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
# ---------------------------------------------------------------------------------------
# Title:         align_config.sh
# Description:   Handles VVS specific configuration 
#########################################################################################

function usage { 
   echo "Error: Command-line format incorrect"
   echo "   usage:  `basename $0` <MiO Configuration Questionaire file>"
   echo "   example:  `basename $0` MCQ.xml"
} 

if [ $# -ne 1 ] && [ $# -ne 2 ]
then
   usage
   exit 1
fi

MCQ_SURVEY=$1
CONFIG_DIR=/opt/global/config

if [ $# -eq 2 ]
then
    CONFIG_DIR=$2
fi


#
# Ensure that the MCQ survey exists
# 
if [ ! -e "$MCQ_SURVEY" ]; then
   echo "Error: File $MCQ_SURVEY does not exist"
   exit 1
fi

#
# Update IMSm specific configuration parameters
#
BASE_DIR=$(dirname $0)
perl ${BASE_DIR}/align_config.pl ${MCQ_SURVEY} ${CONFIG_DIR}

exit 0
