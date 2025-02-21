#!/usr/bin/perl

#This script performs a clear of the specified vvs licensing alarms.
#It must be run on the active SC.

$alarm_table_oid=".1.3.6.1.4.1.193.91.1.3.4";
#use real port here
$port=8016;
$fmmapper_port=8165;

#list of alarms ids to clear
%voice_alarm_ids=( 	"28488", "WARN", 
					"28490", "MINOR",
					"28492", "MAJOR",
					"28494", "CRITICAL" );

%video_alarms_ids=( "28496", "WARN",
					"28498", "MINOR",
					"28500", "MAJOR",
					"28502", "CRITICAL");
					
%alarms_ids_to_clear=();

%fmmapper_clear_ids=( "28488", 51236,
					  "28490", 573,	
					  "28492", 20545,   
					  "28494", 28695,
					  "28496", 41581,
					  "28498", 56006,
					  "28500", 10442,
					  "28502", 57070 );

$level="";
$opco="";

if (! @ARGV) {
	print_help();
	exit;
}

foreach $arg (@ARGV) {
	$arg_lc = lc($arg);
	if ($arg_lc eq "-h" || $arg_lc eq "--h") {
	    print_help();
		exit;
	} elsif ($arg_lc eq "voice") {
		%alarms_ids_to_clear=%voice_alarm_ids;
	} elsif ($arg_lc eq "video") {
		%alarms_ids_to_clear=%video_alarms_ids;
	} 
	
	if ($arg_lc eq "critical" || $arg_lc eq "major" || $arg_lc eq "minor" || $arg_lc eq "warn") {
		$level=$arg_lc;
	} 
	
	if ($arg =~ m/^opco/) {
		$opco=(split(/=/, $arg))[1];
	}
}

if(! %alarms_ids_to_clear) {
	print "Please specify voice or video alarm\n";
	exit;
} 

if ($level eq "") {
	print "Please specify an alarm level: critical|major|minor|warn\n";
	exit;
}

if($opco eq "") {
	print "Please specify an opco: opco=opcoID\n";
	exit;
}

@result=`snmpbulkwalk localhost:$port -v 2c -c vvs -On  $alarm_table_oid`;

foreach $line (@result) {	
	@field = split(/=/, $line);
	$id = @field[0];
	#remove alarm table oid from id to make parsing earier
	$id =~ s/$alarm_table_oid//;
	@indexes = split (/\./, $id);	
	$col_num = @indexes[2];
	
	if ($col_num == '3') {
		#get the alarm id	    
		$alarm_id = remove_type_from_value(@field[1]);		
		if (check_alarm_for_clearing($alarm_id)) {
		    #match found - remove alarm			 			
			$active_flag_oid = $alarm_table_oid . "." . @indexes[1] . ".12." . @indexes[3];
			$instance_id= $alarm_table_oid . "." . @indexes[1] . ".4." . @indexes[3];	
		    clear_alarm($alarm_id, $active_flag_oid, $instance_id);
		}
	}
}

sub check_alarm_for_clearing($) {
	$id = $_[0];
	$alarm_level = lc($alarms_ids_to_clear{$id});
	if($level eq $alarm_level) {
		return 1;
	}			
	return 0;
}

sub clear_alarm($) {
	$alarm_id = $_[0];
	$oid = $_[1];
	$instance_id= $_[2];	
	$clear_id = $alarm_id + 1;	
	$instance=fetch_instance($instance_id);
	#extract opco id
	$opco_id=extract_opco_id($instance);
	#check if the opco of the instance matches
	if (check_clear_opco($opco_id)) {	
		print "match found - clearing alarm " . $alarm_id .  " on opco " . $opco_id . "\n";
		#clear alarm in oam
		`snmpset -v 2c -c vvs localhost:$port $oid u 1`;
		#clear alarm in fmmapper	
		$instance = rewrite_instance($instance);
		$fmmaper_id=$fmmapper_clear_ids{$alarm_id};
		`ntfsend -c 193,14921,$fmmaper_id -s 0 -n "$instance,MessagingServerId=moip"`;
	}	
}

sub fetch_instance($) {
	$instance_id = $_[0];
	$instance=`snmpbulkwalk localhost:$port -v 2c -c vvs -On $instance_id`;	
	return remove_type_from_value($instance);
}


#this is needed for fmmapper
sub rewrite_instance($) {
	$instance = $_[0];
	#remove double quotes and spaces	
	$instance =~ s/\s|"//g;
	
	$cn ="";
	$on = "";
	$dc = "";
	@instance_ids = split(/,/, $instance);
	foreach $attribute (@instance_ids) {
		@key_value = split(/=/, $attribute);
		$key = @key_value[0];
		$value = @key_value[1];
		if( $key eq "cn" ) {
			$cn = $value;
		} elsif ( $key eq "on") {
			$on = $value;
		} elsif ( $key eq "dc") {
			$dc = $value;
		} 
	}
		
	$rearranged_instance = "cn=" . $cn . ",on=" . $on . ",dc=" . $dc;
	return $rearranged_instance;
}

sub check_clear_opco($) {
	$opco_id = $_[0];
	if($opco_id eq $opco) {
	    return 1;
	}
	return 0;
}

sub extract_opco_id($) {
	$s = $_[0];
    if ($s =~ m/.*on=([[:alnum:]]*),.*/) {
		return $1;            
    }
	return "";	
}

#remove the Type from the string
#ex: 
#    remove_type_from_value("Gauge32: 286");
# returns "286"
sub remove_type_from_value($) {
    $s = $_[0];
	$s = trim((split(/:/, $s))[1]);
	return $s;
}

# Left trim function to remove leading and trailing whitespace
sub trim($) {
	$s = $_[0];
    $s =~ s/^\s+//;
	$s =~ s/\s+$//;
    return $s;
}

sub print_help {
	print "Usage: ./clear_license_alarms.pl <voice|video> <critical|major|minor|warn> opco=opcoId \n";
	print "       All parameters are mandatory. \n";
	print "Example: ./clear_license_alarms.pl video critical opco=opco3\n";
	print "         Clears only the critical alarm for the video licenses on opco3.\n";
}

