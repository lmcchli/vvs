#!/usr/bin/perl

###################################################################
#
#   The purpose of this script is to get counters value of subscriber inboxes in the database
#    for this site.
#   What this script does:
#    - Get the counter values from the file contained in /opt/msgcore/mcd/counters/
#    - Filter all valuesrelated to subscribers base on the tag: num_subscriber_MOIP_profiles
#    - Output the result in the stdout.
#
###################################################################

use strict;
use JavaExecution;

# Local constants
use constant TOPLOGY_PATH => "/opt/mio/common/topology.conf";
use constant NO_SITE      => "N/A";
use constant DEBUG        => 0;
my $counterfilelocation = "/opt/msgcore/mcd/counters";
my $countertag          = "num_subscriber_MOIP_profiles";
my $imapReporterPath = "/opt/msgcore/imapreporter";

# $hostsuffix (-o) is for using the reserved network adapter for Operation and Maintenance.
# The traffic and backend adapters (-t and -b) must not be used for these operations.
my $hostsuffix = "-o";

# Local sub-routines
sub readTopology();

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
my %totalOpco      = ();
my $localSite      = NO_SITE;
my $currentTimestamp = 0;
my $lastTimestamp = 0;
my @localSiteResp;
my @opcoList;
my @paList;

# Read topology.conf file
# and search for the IP of each PA node per opco
readTopology();

# For each opco
foreach my $opco ( @opcoList ) {
 if (DEBUG) {
  print "Current opco is $opco \n";
 }
 $lastTimestamp = 0;
 ## For each PA
 @paList = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar listHostsOfTypeForOpco -opcoName $opco -compType provisioningagent -service msgcore -productPrefix moip");

 foreach my $node ( @paList ) {
  # if ( $node =~ /^da/ ) {
   if (DEBUG) {
    print "Current node is $node \n";
   }

   ### Get the file name
   my $commandname = "ssh -o StrictHostKeyChecking=no reports\@${node}${hostsuffix} find $counterfilelocation -name \"${opco}\*\"";
    if (DEBUG) {
     print "commandname is $commandname \n";
    }
   my $filename = `ssh -o StrictHostKeyChecking=no reports\@${node}${hostsuffix} find $counterfilelocation -name \"${opco}\*\"`;
   chomp $filename;
   if ( $filename ne "" ) {
    if (DEBUG) {
     print "filename is $filename \n";
    }
    my $command = "ssh -o StrictHostKeyChecking=no reports\@${node} stat -c %Y $filename";
    $currentTimestamp = `$command`;
    next if $currentTimestamp < $lastTimestamp ;

    ### Get the counter value
    my $command = "ssh -o StrictHostKeyChecking=no reports\@${node}${hostsuffix} grep -i $countertag ${filename} | sed -e 's/^.*=//'";
    if (DEBUG) {
     print "Command is $command \n";
    }
    my $partial = `  $command  `;
    chomp($partial);
    if (DEBUG) {
     print "partial value is $partial \n";
    }

    ### Add the value to the opco total
    if($partial =~ m/\d+/) {
        $totalOpco{$opco} = $partial;
	$lastTimestamp = $currentTimestamp;
    }
   }
  # }
 }

 ## Report the total per opco
 my $message = "Total subscriber inbox for opco [$opco] is [" . $totalOpco{$opco} . "]";
 if ( $localSite ne NO_SITE ) {
  $message .= " on site [${localSite}]";
 }
 print "$message\n";
}

##############################################################################
# Read the topology file and fill in the hashtables:
# %hostOpcoList <node-name><opco>
# %totalOpco <opco><value>
# %nodeHostList <node-name><host-name>
# %remoteSiteList <host-name><host-ip>
sub readTopology() {
 @opcoList = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar listOpco -service moip -productPrefix moip");
 # Initialise totalOpco to 0 for each opco
 foreach my $opco ( @opcoList ) {
        $totalOpco{$opco} = 0;
 }

 # Get the local site name
 @localSiteResp = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar getLocalSiteName -service msgcore -productPrefix msgcore");
 $localSite = $localSiteResp[0];

 if (DEBUG) {
  print("----------------------\n");
  foreach my $item ( @opcoList ) {
   print( "$item\n" );
  }
  print("----------------------\n");
  print("local site is: ". $localSite ."\n");
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
