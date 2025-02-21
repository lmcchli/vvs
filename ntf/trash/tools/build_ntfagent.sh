#!/bin/sh


if [[ "$VOB_HOME" == "" ]];then
    VOB_HOME=/vobs/ipms
fi

# Compilation script for ntfagent

die() {
    echo $1
    exit 1
}

cd $VOB_HOME/ntf/src/ntfagent
clearmake -V -C gnu clean all TOPLEVEL=$VOB_HOME/ntf || die "Compilation of ntfagent failed"

