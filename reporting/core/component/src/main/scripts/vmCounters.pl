################################################################################
# Create a monthly report of some VoiceMail counters
#
#
# This is for development use only and is not officially supported.
################################################################################

use strict;
use JavaExecution;
use constant DEBUG        => 0;
use constant SIMUL        => 0;
use constant NO_SITE      => "N/A";
use constant IMAP_REPORTER_PATH  => "/opt/msgcore/imapreporter";

sub readTopology();
sub compileCounters;
sub calculateDates;
sub addCounters;
sub generateReport();
sub callRemoteSystems();
sub getExecutionPath();
sub syntax();

# Check the input parameters first
if ( scalar(@ARGV) != 2 ) {
 syntax();
 exit;
}
if ( !( $ARGV[0] eq "local" ) && !( $ARGV[0] eq "all" ) ) {
 syntax();
 exit;
}

# The hash contaning the counter values
my %voicemailcounters = ();

my $line;
my @names;
my @values;

my $generateReportFor   = $ARGV[0];
my $month               = $ARGV[1];
my $countersPath        = "/opt/global/perf/moip/aggregated/";
my $vmCountersInputList = "vmCounters.txt";

# Get the path to be able to call the sub-perl command correctly
my $scriptPath = getExecutionPath();

# Set the path to the xmlpd files
if (SIMUL) {
 $countersPath = $scriptPath . "counters";
}
if (DEBUG) {
 printf("path to counters  $countersPath \n");
}

# Get the list of counters we want to track
my @validCounters = `cat ${scriptPath}${vmCountersInputList}`;
my %counters;
chomp(@validCounters);
foreach my $counter (@validCounters) {
 chomp($counter);
 $counter =~ s/\r//;
 $counter =~ s/\[//g;
 $counter =~ s/\]//g;
 $counters{ $counter } = 0;
 if (DEBUG) {
  print( "Counter selected: =" . ${counter} . "=\n" );
 }
}

# Get the list of xmlpd file releated to the month requested for the report
my @files          = glob $countersPath . "/A" . $month . "*";
my $first          = $files[0];
my $last           = $files[-1];
my $currentNode    = "";
my @remoteSiteList;
my @localSite;

# Get the start and end date
my ( $startdate, $enddate ) = calculateDates(@files);
if (DEBUG) {
 print("startdate=$startdate enddate=$enddate\n");
}

readTopology();

if (DEBUG) {
 print( "Nb lines " . scalar(@files) . "\n" );
}
if ( scalar(@files) > 0 ) {
 compileCounters(@files);
}
generateReport();

if ( $generateReportFor eq "all" ) {
 callRemoteSystems();
}

## End of main #############################

sub getdate {
 my ($line) = @_;
 $line =~ m/A(\d+)\..*$/;
 if(DEBUG){
  print"getdate() - $line  $1\n";
 }
 return $1;
}

sub calculateDates {
 my ( $start, $end ) = ( "", "" );
 if ( scalar(@_) > 0 ) {
  $start = getdate($_[0]);
  foreach my $line (@_) {
   chomp($line);
   $end = getdate($line);
  }
 }
 return ( $start, $end );
}

sub callRemoteSystems() {
 
 foreach my $remote (@remoteSiteList){
  my $ip = $remote;
  if (DEBUG) {
   print("remote=$remote  ip=$ip\n");
  }
  my $command = "ssh -o StrictHostKeyChecking=no reports\@$ip \"cd ${scriptPath}; perl ./vmCounters.pl local $month\"";
  if (DEBUG) {
   print("callRemoteSystems() command = $command\n");
  }
  my @result = `$command`;
  print("@result");
 }
}

sub compileCounters {
 my @lines;
 foreach $line (@_) {
  if (DEBUG) {
   print("Reading file : $line\n");
  }
  my @local = `perl $scriptPath/dumpcounters.pl $line filter`;
  chomp(@local);
  push( @lines, @local );
 }
 if (DEBUG) {
  print( "Found " . scalar(@lines) . " counter entries\n" );
#  foreach my $dump (@lines){
#   print "$dump\n";
#  }
 }
 addCounters(@lines);
}

# For each counter make the sum in the hashtable
# Expected input formats:
## cdr-storage-used [Operator: opco8] = 0
## sum-of-number-of-MM4-outgoing-messages [Operator: opco8] = 0
## sum-of-number-of-recipients-in-MM7-incoming-messages [Operator: opco8] = 0
sub addCounters {
 my %componentList;
 my $opco = "N/A";
 foreach $line (@_) {
  chomp($line);
  $line =~ s/\r//;
  $line =~ s/\[//g;
  $line =~ s/\]//g;
  if ( $line eq "" ) {
   next;
  }
  my $v = substr( $line, 0, 2 );
  
  if ( "==" eq substr( $line, 0, 2 ) or "==" eq $v ) {
   next;
  }
  if(DEBUG) {
   print("$line\n");
  }
   
  if ( "----- " eq substr( $line, 0, 6 )) {
  	my $component = substr( $line, 6);
    if(DEBUG) {
     print("Current Component = $component\n");
    }
    $component = substr( $component, 0 , length($component) -2 );
    if(!exists $componentList{ $component }) {
    	my @opcoList = JavaExecution::javaExec(IMAP_REPORTER_PATH."/imapreporter_component.jar getOpcoForNode -service moip -productPrefix moip -nodeName $component");
    	$componentList{ $component } = $opcoList[0];
        if(DEBUG) {
         print("Current OpcoList = ");
         foreach my $valopco (@opcoList){
          print ("valopco -> $valopco\n");
         }
        }
    	$opco = $opcoList[0];
        if(DEBUG) {
         print("--> Opco = $opco from opcoList\n");
        }
        if(DEBUG) {
         print("Current Component = $component\n");
        }
    } else {
    	$opco = $componentList{ $component };
        if(DEBUG) {
         print("--> Opco = $opco from componentList\n");
        }
    }
  }
  if($opco eq "N/A"){
   next;
  }
  if(DEBUG) {
   print("Current Opco = $opco\n");
  }
  # Populate the hash with the new opco and the list of counters.
  foreach my $counter (@validCounters){
   my $key = $opco . "<>" . $counter;
   if( ! exists $voicemailcounters{$key} ){
     $voicemailcounters{$key} = 0;
   }
  } 

  my @keyvalue = split( "=", $line );
  if ( scalar(@keyvalue) != 2 ) {
   next;
  }
  $keyvalue[0] =~ s/\s+$//g;
  $keyvalue[1] =~ s/^\s+//g;
  
  if ( ! exists $counters{ $keyvalue[0] }) {
   if(DEBUG) {
    print("The counter $keyvalue[0] isn't part of the valid counters.\n");
   }
   next;
  }
  $keyvalue[0] = $opco."<>".$keyvalue[0];

  if ( exists $voicemailcounters{ $keyvalue[0] } ) {
   my $current = $voicemailcounters{ $keyvalue[0] };
   $voicemailcounters{ $keyvalue[0] } = $current + $keyvalue[1];
  } else {
    $voicemailcounters{ $keyvalue[0] } = $keyvalue[1];
  }
 }
}

# Output the results in the following format
#
# Counters report,opco = opco8,site = SA2B1,Period = 201009
# total-number-of-critical-alarms=190,maximum-number-of-subscribers-provisioned-system-wide=0,FW.masTotalConnectionsVoiceIncomingConnected=87,
sub generateReport() {
 print("Date start = $startdate,Date end = $enddate");
 my $opcoName = "";
 
 foreach my $counter ( sort keys %voicemailcounters ) {
  if ( $counter eq "" ) {
   if (DEBUG) {
    print "generateReport()  counter name is null or empty.\n";
   }
   next;
  }
  my @keyvalue = split( "<>", $counter );
  if(! ($opcoName eq $keyvalue[0])) {
   $opcoName = $keyvalue[0];
   print("\nCounters report,opco = $opcoName,site = $localSite[0],Period = $month\n"); 
  }
  my $counterDisplayName = $keyvalue[1];
  if (DEBUG) {
   print( "$counter : " + $voicemailcounters{$counter} . "\n" );
  }
  if ( $voicemailcounters{$counter} ne "" ) {
   print( "$counterDisplayName = " . $voicemailcounters{$counter} . "," );
  }
 }
 print("\n");
}

##############################################################################
# Read the topology file and fill in the hashtables:
# %remoteSiteList <host-name><host-ip>
sub readTopology() {
    @localSite = JavaExecution::javaExec(IMAP_REPORTER_PATH."/imapreporter_component.jar getLocalSiteName -service moip -productPrefix moip");
    @remoteSiteList = JavaExecution::javaExec(IMAP_REPORTER_PATH."/imapreporter_component.jar listRemoteSites -service moip -productPrefix moip");
}

###############################################################################
# Calculate and return the absolute path to the execution files
sub getExecutionPath() {
 my @patharray = split( "/", $0 );
 @patharray = reverse(@patharray);
 shift(@patharray);
 @patharray = reverse(@patharray);
 my $scriptPath = join( "/", @patharray );
 if ( !( $scriptPath =~ m/\// ) ) {
  my $local = `pwd`;
  chomp $local;
  $scriptPath = $local . "/" . $scriptPath;
 }
 $scriptPath =~ s/\.$//;
 if ( !( $scriptPath =~ m/\/$/ ) ) {
  $scriptPath .= "/";
 }
 return $scriptPath;
}

sub syntax() {
 printf("Syntax:\n\t$0 {local | all} <year><month>\n");
 printf("Example:\n\t$0 local 201009   # For September 2010\n");
}

__END__
