#!/usr/bin/perl

###################################################################
#
#   The purpose of this script is to call all the scripts related to
#   the VoiceMail counters reporting in order to generate a CSV report
#
###################################################################

use strict;

use File::stat;
use Time::localtime;
use File::Basename;
use Sys::Hostname;

# Local constants
use constant USER                     => "reports";
use constant REPORT_NAME              => "/opt/global/perf/moip/aggregated/reports/vvs/VM-FinalReport-*.csv";
use constant INBOX_REPORT_FILE        => "./VM-Counter-Reports_Inbox.csv";
use constant ACTIVE_INBOX_REPORT_FILE => "./VM-Counter-Reports_ActiveInbox.csv";
use constant COUNTERS_REPORT_FILE     => "./VM-Counter-Reports_Counters.csv";
use constant LOCK_FILE                => "/opt/global/perf/moip/aggregated/reports/vvs/vmReport.LOCK";
use constant LOCK_FILE_EXP_SEC        => 7200;   # Two hours.
use constant GEO_SYSTEMS_CONF         => "/opt/global/config/common/geoSystems.conf";
use constant GEO_SYSTEMS_SETUP        => "/opt/global/config/common/geoSystems.setup";
use constant MOS_CONF                 => "/cluster/cfg/MOS.conf";


# Development specifics
use constant DEBUG => 0;
use constant SIMUL => 0;    # Used to test in dos environment.

use constant VMINBOX    => 1;    # =1 to validate call to vmInbox.pl
use constant VMACTIVE   => 1;    # =1 to validate call to vmActiveInbox.pl
use constant VMCOUNTERS => 1;    # =1 to validate call to vmCounters.pl

# Local sub-routines
sub lock;
sub unlock;
sub savetoFile;
sub createToFile;
sub deleteFile;
sub fileModifiedToday;
sub syntax;
sub runReportOnSC;
sub runReportGeo;
sub readiXmlFileAsOneString;
sub readFlatFileAsOneString;
sub readFlatFile;

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
 print "The script must run on a node of type OM. We are currently on the " .
$host . " node, of type $servertype\n";
 exit 1;
}

my $user = `whoami`;
chomp($user);
if ( SIMUL == 0 and !( $user eq USER ) ) {
 print("\nThe script must run by user " . USER . ". We are currently " . $user . ". Exiting.\n\n");
 exit 1;
}

if (!(-e MOS_CONF)) {
 print("\nCan't find file [" . MOS_CONF . "]. Can't determine if we are on the hosting opco. Exiting.\n\n");
 exit 1;
}
my @MOS_conf = readFlatFile(MOS_CONF);
my $currentOpco = "";
my $foundCurrentOpco = 0;
my $hostingOpco = "";
my $foundHostingOpco = 0;
foreach (@MOS_conf) {
 if ($_ =~ /^MASTEROPCO=(.*)/) {
  $hostingOpco = ${1};
  $hostingOpco =~ s/^\s+//;
  $hostingOpco =~ s/\s+$//;
  $foundHostingOpco = 1;
 } elsif ($_ =~ /^OPCO=(.*)/) {
  $currentOpco = ${1};
  $currentOpco =~ s/^\s+//;
  $currentOpco =~ s/\s+$//;
  $foundCurrentOpco = 1;
 }
}
if (($foundCurrentOpco == 0) or ($foundHostingOpco == 0)) {
 print("\nCould not find current opco or hosting opco in the file [" . MOS_CONF . "]. This script must run on the hosting opco. Exiting.\n\n");
 exit 1;
}
if ($currentOpco ne $hostingOpco) {
 print("\nThe script must run on the hosting opco. We are currently on opco [$currentOpco]. Hosting opco is [$hostingOpco]. Exiting.\n\n");
 exit 1;
}

my $isDetail = 0;
my $detailPath = "";
my $defaultDetailPath = "/opt/moip/reports/vvmDetailSub";

if ($ARGV[scalar(@ARGV)-1] =~ m/detail=(.+)/) {
 $isDetail = 1;
 $detailPath = $1;
} elsif ($ARGV[scalar(@ARGV)-1] eq "detail") {
 $isDetail = 1;
 $detailPath = $defaultDetailPath;
} else {
 $detailPath = $defaultDetailPath;
}

if ($isDetail){
 pop(@ARGV);
}

my $forceReport = 0;
if ($ARGV[scalar(@ARGV)-1] eq "-f") {
# if ($ARGV[0] eq "-f"){
 $forceReport = 1;
 pop(@ARGV);
 print("Forcing report.\n");
}

# Check the input parameters first: The target month from 1 to 12
if ( ( scalar(@ARGV) < 1 ) or ( scalar(@ARGV) > 2 ) ) {
 syntax();
 exit;
}

# Check the input is 6 digits
if ( length( $ARGV[0] ) != 6 or !( $ARGV[0] =~ m/^\d+$/ ) ) {
 syntax();
 exit;
}
my $date = $ARGV[0];
if (DEBUG) {
 print("Report for $date\n");
}

print("\n");
if (!($forceReport) and ((!runReportOnSC()) or (!runReportGeo()))) {
 print("\nReporting : No go.\n\n");
 exit 1;
}

print("\nReporting: Go.\n");
lock();

# Get the total inboxes and log the results
my @results = ();
if (VMINBOX) {
 @results = `perl ./vmInbox.pl`;
 savetoFile( INBOX_REPORT_FILE, @results );
}

# Get the active voice mail report and log the results
@results = ();    # Make sure it's empty
my $delay_for_active = "";
if (VMACTIVE) {
 $delay_for_active = $ARGV[1] if ( scalar(@ARGV) == 2 );
 @results = ` perl ./vmActiveInbox.pl all $date $delay_for_active detail=$detailPath`;
 savetoFile( ACTIVE_INBOX_REPORT_FILE, @results );
}


# Get the counters and log the results
@results = ();    # Make sure it's empty
if (VMCOUNTERS) {
 @results = `perl ./vmCounters.pl all $date`;
 savetoFile( COUNTERS_REPORT_FILE, @results );
}

# Call the final report
# Will generate accurate vvm activeInbox count based on detailed subscriber files stored under $detailPath
` perl ./vmFormatting.pl $date $delay_for_active detail=$detailPath`;

# Remove files under detailPath after generating reports
# Comment out if you are in debug mode
my $rmCommand = "rm -rf $detailPath/imapReportSubDetail-*";
`$rmCommand`;

unlock();
exit 0;


## END OF MAIN ############################################


##############################################################################
sub lock {
 print("\nCreating lock file " . LOCK_FILE . "\n");
 createToFile(LOCK_FILE, "This is a lock file created by vmReport on [" .  hostname . "].\n");
 print("\n");
}

##############################################################################
sub unlock {
 print("\nRemoving lock file " . LOCK_FILE . "\n");
 deleteFile(LOCK_FILE);
 print("\n");
}

##############################################################################
sub savetoFile {
 my ( $filename, @content ) = @_;
 open FILE, ">$filename" || die "ERROR Trying to open the file $filename. Verify permissions on the folder";
 foreach my $line (@content) {
  printf FILE $line;
 }
 close FILE;
}

##############################################################################
sub createToFile {
 my ( $filename, @content ) = @_;
 open FILE, ">$filename" || die "ERROR Trying to open the file $filename.
Verify permissions on the folder";
 foreach my $line (@content) {
  print FILE $line;
 }
 close FILE;
}

##############################################################################
sub deleteFile {
 my ( $filename ) = @_;
 if (unlink($filename) == 1) {
  print("File [" . basename($filename) . "] deleted successfully.\n");
  return 1;
 } else {
  print("File [" . basename($filename) . "] was not deleted.\n");
  return 0;
 }
}

##############################################################################
sub fileModifiedToday {
 my ( $filename ) = @_;
 my $fileTime = stat($filename)->mtime;
 my $fileTime_string = ctime($fileTime);
 my $fileTime_year = localtime($fileTime)->year;
 my $fileTime_mon = localtime($fileTime)->mon;
 my $fileTime_mday = localtime($fileTime)->mday;

 my $currentTime_string = ctime(time);
 my $currentTime_year = localtime()->year;
 my $currentTime_mon = localtime()->mon;
 my $currentTime_mday = localtime()->mday;

 if (($fileTime_year == $currentTime_year) and ($fileTime_mon == $currentTime_mon) and ($fileTime_mday == $currentTime_mday)) {
  print("  File [" . basename($filename) . "] was created today. File time [$fileTime_string] Current time [$currentTime_string].\n");
  return 1;
 } else {
  print("  File [" . basename($filename) . "] was not created today. File time [$fileTime_string]  Current time [$currentTime_string].\n");
  return 0;
 }
}

##############################################################################
sub syntax() {
 print("\nSyntax:\n\t$0 <year_month> [<active_inbox_delay>] [-f]\n");
 print("\t<year_month>         The year and month requested. Must be 6 digits.\n");
 print("\t<active_inbox_delay> The number of days for the active inbox. Default 30 days. Range [1,30].\n");
 print("\t-f                   Bypass file locking mechanism. Reporting can run on Geo-Redundant\n");
 print("\t                     and Geo-Distributed systems.\n\n");
 print("Example:\n\t$0 201009     # For September 2010 and last 30 days for active inbox.\n");
 print("Example:\n\t$0 201009 20  # For September 2010 and last 20 days for active inbox.\n\n");
}

##############################################################################
sub runReportOnSC{
 my $lockfile = LOCK_FILE;
 my $lockfile_basename = basename($lockfile);
 my $lockfile_time = "";
 my $lockfile_time_string = "";
 my $current_time = time;
 my $current_time_string = ctime(time);

 if (-e $lockfile) {
  print("File [$lockfile_basename] found.\n");
  $lockfile_time = stat($lockfile)->mtime;
  $lockfile_time_string = ctime(stat($lockfile)->mtime);
  print("File [$lockfile_basename] was updated on [$lockfile_time_string]. Current time is [$current_time_string].\n");
  if ($current_time - $lockfile_time > LOCK_FILE_EXP_SEC) {
   my $hours_3dec = sprintf '%.2f', LOCK_FILE_EXP_SEC/3600;
   print("File [$lockfile_basename] expired (more than $hours_3dec hours old). Will delete this lock file.\n");
   if (deleteFile($lockfile)) {
    # Recursively determine if reporting will run, now that the LOCK file was removed
    runReportOnSC();
   } else {
    # Could not delete the LOCK file, don't run report.
    print("Warning: ensure that no other process is holding the [$lockfile_basename] file. Reporting will exit.\n");
    return 0
   }
  } else {
   my $hours_3dec = sprintf '%.2f', LOCK_FILE_EXP_SEC/3600;
   print("File [$lockfile_basename] is valid (less than $hours_3dec hours old). Another reporting process is probably ongoing. This reporting process will exit.\n");
   print("You may delete this lock file if you are certain no other reporting process is ongoing on this system.\n");
   return 0;
  }
 } else {
  print("File [$lockfile_basename] not found.\n");
  my @filesFound = glob(REPORT_NAME);
  foreach my $file (@filesFound) {
   if (fileModifiedToday($file)) {
    print("  Report [" . basename($file) . "] has already been generated for today. Reporting will exit.\n");
    return 0;
   } else {
    print("  Report [" . basename($file) . "] was not generated today.\n");
   }
  }
  print("  No reports were generated today.\n");
  return 1;
 }
}

##############################################################################
sub runReportGeo {
 if ((-e GEO_SYSTEMS_CONF) and (-e GEO_SYSTEMS_SETUP)) {
  print("Geo setup detected.\n");
  my $line_conf = readXmlFileAsOneString(GEO_SYSTEMS_CONF);
  if ($line_conf =~ /(<Cm.ActiveMaster>)(.*)(<\/Cm.ActiveMaster>)/) {
   my $activeMaster = ${2};
   $activeMaster =~ s/^\s+//;
   $activeMaster =~ s/\s+$//;
   print("  Found Cm.ActiveMaster=[$activeMaster]\n");
   my $line_setup = readFlatFileAsOneString(GEO_SYSTEMS_SETUP);
   if ($line_setup =~ /(LocalSystemId:)(.*?)(\-)/) {
    my $localSystemId = ${2};
    $localSystemId =~ s/^\s+//;
    $localSystemId =~ s/\s+$//;
    print("  Found LocalSystemId=[$localSystemId]\n");
    if ($localSystemId eq $activeMaster) {
     print("  Cm.ActiveMaster matches LocalSystemId. This system is the master.\n");
     return 1;
    } else {
     print("  Cm.ActiveMaster does not match LocalSystemId. This system is not the master.\n");
     return 0;
    }
   } else {
    print("  Could not find LocalSystemId. Assume Geo not enabled.\n");
    return 1;
   }
  } else {
   print("  Could not find activeMaster. Assume Geo not enabled.\n");
   return 1;
  }
 } else {
  print("No Geo setup detected. Files [" . GEO_SYSTEMS_CONF . "] and [" . GEO_SYSTEMS_SETUP . "] not found\n");
  return 1
 }
}

##############################################################################
sub readXmlFileAsOneString {
 my $filename = shift;
 my $line;

 open(FILE, "< $filename" ) or die "Error: Can't open $filename : $!";

 while( <FILE> ) {
  next if /^(\s)*$/;    # skip blank lines
  chomp;                # remove trailing newline characters
  $line .= $_;          # add the data into the single string
 }

 close FILE;
 return $line;
}

##############################################################################
sub readFlatFileAsOneString {
 my $filename = shift;
 my $line;

 open(FILE, "< $filename" ) or die "Error: Can't open $filename : $!";

 while( <FILE> ) {
  s/#.*//;              # ignore comments by erasing them
  next if /^(\s)*$/;    # skip blank lines
  chomp;                # remove trailing newline characters
  $line .= $_;          # add the data into the single string
 }

 close FILE;
 return $line;
}


##############################################################################
sub readFlatFile {
 my $filename = shift;
 my @lines;

 open(FILE, "< $filename" ) or die "Error: Can't open $filename : $!";

 while( <FILE> ) {
  s/#.*//;              # ignore comments by erasing them
  next if /^(\s)*$/;    # skip blank lines
  chomp;                # remove trailing newline characters
  push @lines, $_;    # add the data into the array
 }

 close FILE;
 return @lines;
}



__END__
