#!/bin/bash
watch ps H -C 'mtest' -o cmd,sched,class,rtprio,nice
