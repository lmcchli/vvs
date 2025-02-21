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
# Title:         align_config.pl
# Description:   This script updates services configuration parameters that cannot be handled
#                by the generic "installsurveyxml" tag. Note that adding to this script
#                should be a "last resort" since most configuration parameters can be
#                changed generically. 
##########################################################################################

use lib '/cluster/software/install/automation_network/lib';
use File::Find;
use XML::Simple;
use ApplicationConfigSetter;
use G_Funct;
use SurveyDataAccessor;
use Data::Dumper;

use constant TRUE => 1;
use constant FALSE => 0;

use Inline (
    Java => 'STUDY',
    STUDY => ['java.util.HashMap', 'java.util.Collections', 'java.util.ArrayList', 'java.util.Iterator'],
    AUTOSTUDY => 1,
    J2SDK => '/opt/java',
    DIRECTORY => '/tmp',
);


#
# Must have the MCQ XML file passed in from cmd-line
# Might have the config directory passed in from cmd-line
#
if ($#ARGV < 0 || $#ARGV > 1) {
    print "Error: Command-line format incorrect\n";
    print "   usage:  perl $0 <MiO Configuration Questionaire file>\n";
    print "   example:  perl $0 MCQ.xml\n";
    exit 1;
}


#
# Parse MCQ XML file and set the config directory to search through
#
my $xmlFile = shift @ARGV;
my $surveyParser = XMLin( $xmlFile, ForceArray => 1, NoAttr => 1 );
@ARGV = "/opt/global/config" unless @ARGV;


#
# Figure out if "HA" or "SA" system that is being configured
#
my $isHA = SurveyDataAccessor::isHA($xmlFile);

print "This survey has isHA = $isHA\n";


updateClusterConf();
#
# find(\&wanted, @directories);
# 
# find() does a depth-first search over the given @directories in the order they are given. 
# For each file or directory found, it calls the &wanted subroutine. 
#

find (\&processFile, @ARGV); 

# Now update cluster.conf with MSS routes


sub processFile {

    unless (isMatched()) {
        return;
    }

    G_Funct::backupConfigurationFile($File::Find::dir, $File::Find::name);
   
    # print "Will now process file $File::Find::name" 
    unless (updateNonStandardMiOConfigurationFile()){
	updateIfIsStandardMiOConfigurationFile();
	return;
    } 
}
 
sub updateClusterConf(){
    print "Update cluster.conf\n";

    my $clusterconf = "/cluster/etc/cluster.conf";  
    G_Funct::backupConfigurationFile("/cluster/etc",$clusterconf);
    my $vvsConfigSection =
		$surveyParser->{'my:MioConfigurationSection'}->[0]
		->{'my:VvsConfigurationSection'}->[0]->{'my:VvsConfiguration'}->[0];
    my $mssRtpIp =
		$vvsConfigSection->{'my:MssRtpIp'}->[0];

    my $foundALine = G_Funct::fileContainsSubstring($clusterconf, "network RTP_GW");

    if ($foundALine =~ m"0$"){
	print "/cluster/etc/cluster.conf did not contain RTP_GW, so might have to update cluster.conf\n";
        if ($mssRtpIp ne ""){
	    G_Funct::addLineAfterSpecificLineInFile($clusterconf, "network internal", "network RTP_GW $mssRtpIp/32");
        }
	else {
	    print "No MSS defined, no network to add to cluster.conf\n";
	}
    }
    else {
	print "/cluster/etc/cluster.conf already contains a network for RTP_GW, will not update cluster.conf\n";
    }

    # Add route for each traffic node:
    #    route <MOIP_TN_NODE_ID> RTP_GW gateway <CSW_RTP_VRRP IP>
    my $foundARouteLine = G_Funct::fileContainsSubstring($clusterconf, "RTP_GW gateway");
    if ($foundARouteLine =~ m"0$"){
	print "$clusterconf did not contain routes for RTP_FW; might have to add\n";
        if ($mssRtpIp ne ""){
	    print "Adding routes to $clusterconf for each moip-tn\n";
	    my @allnodeids = G_Funct::collectAllTrafficNodeIds("moip-tn-");
	    my $routeTextBlock = "";

	    my $CSW_RTP_VRRP_IP;
	    if ($isHA eq "1"){
		$CSW_RTP_VRRP_IP = $surveyParser->{'my:MioNetworkIpRanges'}->[0]->{'my:RTP_VIP_IP'}->[0];
	    }
	    else {
		$CSW_RTP_VRRP_IP = $surveyParser->{'my:MioNetworkIpRanges'}->[0]->{'my:RTP_SW00_IP'}->[0];	
	    }

	    foreach (@allnodeids){
		$routeTextBlock = $routeTextBlock."\n"."route $_ RTP_GW gateway $CSW_RTP_VRRP_IP";
	    }
	    open outgoingContentFH, ">>", $clusterconf;
	    print outgoingContentFH ($routeTextBlock);
	    close(outgoingContentFH);
	}
	else {
	    print "No MSS defined, no routes to add to cluster.conf\n";
	}
    }
    else {
	print "$clusterconf already contains the routes for the RTP_GW\n";
    }
}

sub updateNonStandardMiOConfigurationFile(){
    if ($File::Find::name =~ m"messagingservices.conf$" ){
        updateMessagingServices();
        return TRUE;
    }   
    if ($File::Find::name =~ m"componentservices.cfg" ){
        updateComponentServices();
        return TRUE;
    }   
   return FALSE;
}

sub updateMessagingServices(){
    my $filename = $File::Find::name;
    my $contents = "";
    if ($isHA eq "1"){
	print "Updating $filename for HA cabinet\n";
	my $opcovip = G_Funct::calculateOpcoBasedVip("192.168.63.51", $File::Find::name);
	$contents = "NTF_HOST=$opcovip\nNTF_PORT=8989\nMAS_HOST=$opcovip\nMAS_PORT=10401";
    }
    else {
	print "Updating $filename for SA cabinet\n";
	$contents = "NTF_HOST=192.168.51.1\nNTF_PORT=8989\nMAS_HOST=192.168.51.1\nMAS_PORT=10400\n";
    }
    open outgoingContentFH, ">", $filename;
    print outgoingContentFH ($contents);
    close(outgoingContentFH);
}


sub updateComponentServices(){
    if ($isHA eq "1"){
	print "Updating $File::Find::name with HA values\n";
        
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=mediaaccessserver","componentname","mas"); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=Notification","componentname","ntf"); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=OutdialNotification","componentname","vva"); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=MWINotification","componentname","vva"); 

	my $opcovip = G_Funct::calculateOpcoBasedVip("192.168.63.51", $File::Find::name);

	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=mediaaccessserver","hostname",$opcovip); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=Notification","hostname",$opcovip); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=OutdialNotification","hostname",$opcovip); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=MWINotification","hostname",$opcovip); 

	my $mcdopcovip = G_Funct::calculateOpcoBasedVip("192.168.63.67", $File::Find::name);
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=MessagingCommonDirectory","hostname",$mcdopcovip); 

	my $opcoindex = int(G_Funct::extractOpcoNumber($File::Find::name)) - 1;

	my $opcoVipSettingsSection = $surveyParser->{'my:OpcoVipSection'}->[0];
	my $opcoVipSettings = $opcoVipSettingsSection->{'my:OpcoVipSettings'}->[$opcoindex];
	my $paVip = $opcoVipSettings->{'my:PaVip'}->[0];

	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=ProvisioningAgent","hostname","$paVip"); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=ProvisioningAgent","provisioninghostname","$paVip"); 
	G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=ProvisioningAgent","sessioncontrolhostname","$paVip"); 

	return;
    }
    print "Updating $File::Find::name with SA values";
    # This is SA
    my $plname = G_Funct::findSinglePLForService("moip");
    print "plname is $plname\n";
    G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=mediaaccessserver","componentname","mas1\@$plname"); 
    G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=Notification","componentname","ntf1\@$plname"); 
    G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=OutdialNotification","componentname","vas1\@$plname"); 
    G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=MWINotification","componentname","vva1\@$plname"); 

    # In an SA context, the default value for hostnames (moip-tn-00) will 
    # always be good, so no need to replace
    #my $standardname = G_Funct::findSingleStandardTrafficNodeForService("moip");
    #G_Funct::replaceBlockParameterInFile($File::Find::name,"servicename=mediaaccessserver","hostname",$standardname."hello"); 

}

sub updateIfIsStandardMiOConfigurationFile(){
    my $filename = $File::Find::name;
    my $opco = G_Funct::extractOpcoNumber($filename);
    my $cfgSetter = ApplicationConfigSetter->new($filename);
    my $standardtrafficnode = G_Funct::extractTrafficNodeStandardName($File::Find::name);
    unless (defined $cfgSetter) {
        # No problem, not a standard file, just leave
	return FALSE;
    }
    if ( $filename =~ m"masSpecific.conf$" ) {
       my $paramname = "Cm.executionEngineHostname";
       my $trafficnodebinterface = G_Funct::getIPForStandardNodeNameAndType($standardtrafficnode,"b");
       my $trafficnodetinterface = G_Funct::getIPForStandardNodeNameAndType($standardtrafficnode,"t");
       my $paramvalue = "xmp:$trafficnodebinterface;sip:$trafficnodetinterface";
       return updateStandardConfigurationFile($filename,$cfgSetter, $paramname, $paramvalue);
    }
    if ( $filename =~ m"stream.conf$" ) {
       my $paramname = "Cm.localHostName";
       # Figure out which IP this is (RTP IP for this traffic node)
       my $trafficnoderinterface = G_Funct::getIPForStandardNodeNameAndType($standardtrafficnode,"r");
       my $paramvalue = "$trafficnoderinterface";
       return updateStandardConfigurationFile($filename,$cfgSetter, $paramname, $paramvalue);
    }
    if ( $filename =~ m"callManager.conf$" ) {
        my $paramname = "Cm.contactUriOverride";
        my $startport = 5061;
        my $tnindex = G_Funct::extractTrafficNodeStandardIndex($File::Find::name);

        my $port = $startport + int($tnindex);
        my $tnip = "";

        if ($isHA eq "1"){
	    my $opcoTrafficVip =
	         $surveyParser->{'my:OpcoVipSection'}->[0]
	           ->{'my:OpcoVipSettings'}->[$opcoindex]->{'my:TrafficVip'}->[0];
            if ($opcoTrafficVip ne ""){
               $tnip = $opcoTrafficVip;
            }
        }
        else {
           $tnip = &G_Funct::getIPForStandardNodeNameAndType($standardtrafficnode, "t");
        }

        my $paramvalue = "sip:mas\@$tnip:$port";

        updateStandardConfigurationFile($filename,$cfgSetter, $paramname, $paramvalue);
        
	my $vvsConfigSection =
	  $surveyParser->{'my:MioConfigurationSection'}->[0]
	  ->{'my:VvsConfigurationSection'}->[0]->{'my:VvsConfiguration'}->[0];
	my $mssSipIp =
	  $vvsConfigSection->{'my:MssSipIp'}->[0];

       if ($mssSipIp =~ m"[0-9]+$"){
            # An mss defined
            print "MMS IP is $mssSipIp in survey file, configuring mss values in RemotePartySipProxyHost and Port\n";
            updateStandardConfigurationFile($filename,$cfgSetter, "Cm.remotePartySipProxyHost", $mssSipIp);
 	    my $mssSipPort = $vvsConfigSection->{'my:MssSipPort'}->[0];
            updateStandardConfigurationFile($filename,$cfgSetter, "Cm.remotePartySipProxyPort", $mssSipPort);
       }
       else {
            print "No MSS IP defined in survey file, configuring hardcoded SA values\n";
            my $dialogicIp = "192.168.19.217";
            my $dialogicPort = "5060";
            updateStandardConfigurationFile($filename,$cfgSetter, "Cm.remotePartySipProxyHost", $dialogicIp);
            updateStandardConfigurationFile($filename,$cfgSetter, "Cm.remotePartySipProxyPort", $dialogicPort);
      }
  }
}

sub updateStandardConfigurationFile(){
    my $filename = shift;
    my $cfgSetter = shift;
    my $paramname = shift;
    my $paramvalue = shift;
    print "$filename  set $paramname to $paramvalue\n";
    $cfgSetter->setStringParameter($paramname, $paramvalue);
    $cfgSetter->commit();
    return TRUE;
}

#
# Check to see if directory is matched according to the following example:
#              /opco#/moip/PL-#/
#
# Returns true if all above criteria are met, false otherwise.
#
sub isMatched {
    my $match = FALSE;
    if (    $File::Find::name =~ m"messagingservices.conf$" ||
            $File::Find::name =~ m"masSpecific.conf$" || 
            $File::Find::name =~ m"stream.conf$" ||
	    $File::Find::name =~ m"messagingservices.conf$" ||
	    $File::Find::name =~ m"callManager.conf$" ||
            $File::Find::name =~ m"componentservices.cfg$"
       ) {
               # Don't bother checking for xsd, not all moip files are standard
               $match = TRUE;
    }
    return $match;
}


exit 0;
