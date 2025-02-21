#!/usr/bin/perl

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
# Title:         restore_config.pl
# Description:   This script restores the config file backed up by the last MCQ backend run
##########################################################################################

my $backupdir = "/opt/backup/.mcq";

print "This script will restore the files the\nMCQ VVS backend backup up before its last run.\nAre you sure you want to replace your current config\nwith this backup? (y/n)";

my $answer = <>;

if ( ($answer =~ m"y$") || ($answer =~ m"Y$")){
   print "OK, restoring backup\n";
   system("cp -vrf $backupdir/opt/global/config /opt/global");
   print "Restored backup of /opt/global/config\n";
   system("cp -vrf $backupdir/cluster/etc/cluster.conf /cluster/etc/cluster.conf");
   print "Restored backup of /cluster/etc/cluster.conf\n";
}
else {
   print "OK, no action done\n";
   exit;
}
