#!/usr/bin/perl

###################################################################
#
#   This script is called by the cron-job.
#   Takes no parameter.
#   Has to run with root privileges.
#
#   Example of crontab setup for a call every month the 01 at 1:10 AM: 
#   10 1 1 * * /usr/bin/perl /opt/moip/reports/vmReportCronJob.pl
#
###################################################################

use strict;

# Local constants
use constant REPORT_BASE_FOLDER   => "/opt/moip/reports";
use constant REPORT_SCRIPT        => REPORT_BASE_FOLDER ."/vmReport.pl";
use constant USER                 => "root";

# Development specifics
use constant DEBUG => 0;   # Used to log information during development.
use constant SIMUL => 0;   # Used to test in dos environment.

# Sanity check
sub getServerType {
    #read serverType from /etc/hosttype.
    #return this value

    open(my $fh, "/etc/hosttype");
    my $serverType="";
    my $line="";
    my @serverList = <$fh>;
    close($fh);
    ($serverType)=@serverList;
    chomp $serverType;
    return uc $serverType;
}

 my $host = $ENV{HOSTNAME};
 my $servertype=getServerType($host);
 
if ( SIMUL == 0 and ( $servertype !~ /OM/ ) ) {
 print "The script must run on a node of type OM. We are currently on the " . $host . " node, of type $servertype\n";
 exit 1;
}

my $user = `whoami`;
chomp($user);
if ( SIMUL == 0 and !( $user eq USER) ) {
 print "The script must run by user ".USER.". We are currently " . $user . "\n";
 exit 1;
}

# Format the date for the request <year><month>
my (@datearray) = localtime();
my $year = 1900 + $datearray[5];
my $month = $datearray[4];     # range is 0 to 11
if($month == 0){
 # Case it's january then set to december last year.
 $month = 12;
 $year--;
}
my $date = sprintf( "%04d%02d", $year, $month);
if (DEBUG) {
 print("Report for $date\n");
}

# Call the report generator
my $command = "su reports -c \"cd " . REPORT_BASE_FOLDER . "; /usr/bin/perl ". REPORT_SCRIPT . " $date\"";
if (DEBUG) {
 print("$command\n");
}
`$command`; 


__END__
