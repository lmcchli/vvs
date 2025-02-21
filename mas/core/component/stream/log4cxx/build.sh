#!/bin/ksh
if [ "$1" == "--dry-run" ]; then
DRY=-n
shift
fi
clearmake -C gnu ${DRY} REALTIME=yes $* && \
clearmake -C gnu ${DRY} $*
