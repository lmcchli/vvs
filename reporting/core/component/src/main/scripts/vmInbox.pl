#!/usr/bin/perl

###################################################################
#
#   The purpose of this script is to collect, format and report the total number of
#   subscriber inboxes in the database for this site and consolidate with all other
#   sites in the geo-redundancy architecture.
#   What this script does:
#    - Get the system confoguration, including the list of sites
#    - Query the counters per opco and per site using and other Perl script
#    - Output the result in the stdout.
#
###################################################################

use strict;
use JavaExecution;

# Local constants
use constant NO_SITE            => "N/A";
use constant LOCAL_COUNT_SCRIPT => "vmInboxLocal.pl";
my $imapReporterPath = "/opt/msgcore/imapreporter";

# $hostsuffix (-o) is for using the reserved network adapter for Operation and Maintenance.
# The traffic and backend adapters (-t and -b) must not be used for these operations.
my $hostsuffix = "-o";


# Development specifics
use constant DEBUG       => 0;
use constant SIMULREMOTE => 0;

# Local sub-routines
sub readTopology;
sub getExecutionPath;
sub getLocalInboxCounters;
sub generateTotals();
sub outputResults();

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
 
 if ( $servertype !~ /OM/ ) {
  print "The script must run on a node of type OM. We are currently on the " . $host . " node, of type $servertype\n";
  exit 1;
 }

# Local variables
my @remoteSiteAddrNameList;
my $total          = 0;

# Get the path to be able to call the sub-perl comand correctly
my $scriptPath = getExecutionPath();

# Read topology.conf file  and search for the IP of each PA node per opco
readTopology();

# get the local inbox counters
my @listcount = ();
getLocalInboxCounters();

# For each external site
foreach my $site ( @remoteSiteAddrNameList ) {
 $site =~ m/(.*);(.*)/;
 my $ipRemote = $1;
 my $siteName = $2;
 if (DEBUG) {
  print("Site $siteName , IP $ipRemote\n");
 }
 # Get the inbox counters from the external
 if ($ipRemote !~ /^(\d+\.?){4}$/) {
  $ipRemote = $ipRemote . $hostsuffix;
 }
 my $command = "ssh -o StrictHostKeyChecking=no reports\@${ipRemote}  \"cd ${scriptPath}; perl $scriptPath" . LOCAL_COUNT_SCRIPT . "\"";
 if (DEBUG) {
  print "Executing $command\n";
 }
 my @temp = `$command`;
 chomp(@temp);
 if (DEBUG) {
  foreach my $line (@temp) {
   print "+++> $line\n";
  }
 }

 ## Add the result to the current list
 push @listcount, @temp;
 if (SIMULREMOTE) {
  push @listcount, "Total subscriber inbox for opco [opco3] is [3] on site [SA2B0]";
  push @listcount, "Total subscriber inbox for opco [opco1] is [1] on site [SA2B7]";
 }
}

# Parse the result list and make a sum per opco and site
if (DEBUG) {
 foreach my $line (@listcount) {
  print "===> $line\n";
 }
}

# Generate the totals per opco and site
my %totalpersite      = ();    # <site>,<value>
my %totalperopco      = ();    # <opco>,<value>
my %listofsiteperopco = ();    # <opco>,<<site1>,<site2>,...>
my %listofopcopersite = ();    # <site>,<<opco1>,<opco2>,...>
my $cumul             = 0;
my $tmp               = "";
my $totalinboxes      = 0;
generateTotals();

# Generate the output rreports
outputResults();

## End of main #############################

##############################################################################
# Generate the totals per opco and site
# Example of input list:
##    Total subscriber inbox for opco [opco2] is [4] on site [SA2B1]
##    Total subscriber inbox for opco [opco2] is [0] on site [SA2B0]
##    Total subscriber inbox for opco [opco1] is [0] on site [SA2B0]
##    Total subscriber inbox for opco [opco8] is [0] on site [SA2B7]
##    Total subscriber inbox for opco [opco2] is [0] on site [SA2B7]
##    Total subscriber inbox for opco [opco1] is [0] on site [SA2B7]
sub generateTotals() {
 foreach my $line (@listcount) {
  $line =~ m/^.*\[(.*)\].*\[(.*)\].*\[(.*)\].*$/;
  if (DEBUG) {
   print("\$1=$1  \$2=$2 \$3=$3 \n");
  }
  my $tmp = $listofopcopersite{$3};
  $listofopcopersite{$3} = $tmp . $line . ";";
  $totalinboxes += $2;
 }
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

###############################################################################
# Return the list of local inbox counters
sub getLocalInboxCounters() {
 if (DEBUG) {
	print("About to fetch local counters\n");
 }
 my $script    = LOCAL_COUNT_SCRIPT;
 @listcount = `perl $script`;
 if (DEBUG) {
  foreach my $line (@listcount) {
   print("--> ${line}\n");
  }
 }
}

##############################################################################
# Example:
## site=SA2B0
## opco3=3,opco6=3,
## site=SA2B1
## opco2=4,
## site=SA2B7
## opco1=1,opco4=1,
## number of inbox provisioned=12
## total per opco:,opco1=1,opco2=4,opco3=3,opco4=1,opco6=3,
sub outputResults() {
 foreach my $site ( sort keys %listofopcopersite ) {
  print("site=$site\n");
  my @listopco = split( ";", $listofopcopersite{$site} );
  foreach my $opco ( sort @listopco ) {
   $opco =~ m/^.*\[(.*)\].*\[(.*)\].*\[(.*)\].*$/;
   print( $1 . "=" . $2 . "," );
   my $tmp = $totalperopco{$1} + $2;
   $totalperopco{$1} = $tmp;
  }
  print("\n");
 }
# print "number of inbox provisioned=$totalinboxes\n";
# print "total per opco:,";
# foreach my $item ( sort keys %totalperopco ) {
#  print( "${item}=" . $totalperopco{$item} . "," );
# }
# print("\n");
}

##############################################################################
# Read the topology file and fill in the hashtables:
# @remoteSiteAddrNameList <host-ip>;<host-name>
sub readTopology() {

  @remoteSiteAddrNameList = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar getRemoteSiteAddressesAndNames -service msgcore -productPrefix msgcore");

 if (DEBUG) {
  print("----------------------\n");
  print("List of remote sites defined in the topology:\n");
  foreach my $item ( @remoteSiteAddrNameList ) {
    $item =~ m/(.*);(.*)/;
    print( "$2 : $1\n" );
  }
  print("----------------------\n");
 }
}


__END__

----------------------
moip-tn-00   =>   opco2
da-00   =>   opco2
om-01   =>   opco2
mms-tn-00   =>   opco2
om-00   =>   opco2
----------------------
mcdproxy-da-00   =>   da-00
provisioningagent-da-00   =>   da-00
mlm-om-01   =>   om-01
npc-om-00   =>   om-00
fmmapper-om-01   =>   om-01
relay-tn-00   =>   mms-tn-00
mas1   =>   moip-tn-00
sos1   =>   mms-tn-00
fmmapper-om-00   =>   om-00
npc-om-01   =>   om-01
oam-om-00   =>   om-00
mlm-om-00   =>   om-00
relay-tn-01   =>   mms-tn-01
oam-om-01   =>   om-01
ntf1   =>   moip-tn-00
----------------------
SA2B0   =>   172.30.241.156
SA2B7   =>   172.30.241.226
----------------------
