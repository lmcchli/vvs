################################################################################
# Call imapReporter for all system  
#
#
# This is for development use only and is not officially supported.
################################################################################

use strict;
use JavaExecution;
use constant DEBUG => 0; 
use constant SIMUL => 0; 
use constant SECINDAY => 86400;

my @daysPerMonthTable =
  ( 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 );
my @monthName =
  ( "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" );
  
my $isDetail = 0;
my $detailPath = "";
my $defaultDetailPath = "/opt/moip/reports/vvmDetailSub";
sub readTopology();
sub syntax();
sub callRemoteSystems();

if(scalar(@ARGV) > 4){
    syntax();
    exit;
}

if(!($ARGV[0] eq "local") && !($ARGV[0] eq "all")) {
    syntax();
    exit;
}

if ($ARGV[scalar(@ARGV)-1] =~ m/detail=(.+)/){
	$isDetail = 1;
        $detailPath = $1;   
} elsif ($ARGV[scalar(@ARGV)-1] == "detail"){
	$isDetail = 1;
        $detailPath = $defaultDetailPath;
} else {
   $detailPath = $defaultDetailPath;
}


if ($isDetail){
	pop(@ARGV);
}


my $date;
my $deltaDate=-1;
my $originalDeltaDate;

if(scalar(@ARGV) == 1) {
    $date = 30;
} elsif(scalar(@ARGV) == 2) {
    if(length($ARGV[1]) > 6){
        syntax();
        exit;
    }
    $date = $ARGV[1];
} else {
    if(length($ARGV[1]) != 6){
        syntax();
        exit;
    }
    if(length($ARGV[2]) > 3){
        syntax();
        exit;
    }
    $date = $ARGV[1];
    $originalDeltaDate = $ARGV[2];
    if($ARGV[2] > 0) {
        $deltaDate = $ARGV[2] - 1;
    } else {
        $deltaDate = 0;
    }
}


my $generateReportFor = $ARGV[0];
my $imapReporterPath = "/opt/msgcore/imapreporter";
my @remoteSiteList;
my @siteName;
my @remoteUser;
my @result;
my @opcoList;
my @detailSubFileList;

# Get the path to be able to call the sub-perl command correctly
my @patharray = split("/", $0);
@patharray = reverse(@patharray);
shift(@patharray);
@patharray = reverse(@patharray);
my $scriptPath = join("/",@patharray);
if(! ($scriptPath =~ m/\//) ){
   my $local = `pwd`;
   chomp $local;
   $scriptPath = $local . "/" . $scriptPath;
}
$scriptPath =~ s/\.$//;
if(!($scriptPath =~ m/\/$/)){
  $scriptPath .= "/";
}

readTopology();

if($generateReportFor eq "all") {
	# make remote directory if not exists
    	mkdir $detailPath;
  	die "Cannot create directory $detailPath : $!\n" unless -d $detailPath;
   
	# clean remoteSite detail files before generating new detail file
	my $rmCommand = "rm -f $detailPath/imapReportSubDetail-*";
	my $result = `$rmCommand`;
 	callRemoteSystems();
}



@opcoList = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar listOpco -service moip -productPrefix moip");

my $opco;
push( @result , "site = $siteName[0]");
foreach $opco(@opcoList) {
        my @local;
        my $localDetailDir = "/opt/global/perf/$opco/moip/vvmDetailSub";
        my $localDetailFilePath = "$localDetailDir/imapReportSubDetail-$siteName[0]-$opco.txt";
        
        # clean $detailFileDir before generating detailFile
       	`rm -f $localDetailDir/imapReportSubDetail-*`;
        
        if($deltaDate == -1) { 
            if(length($date) > 2) {
                my $year  = substr( $date, 0, 4 );
                my $month = substr( $date, 4, 2 );
                my $day = $daysPerMonthTable[$month-1];
                my $activedate01 = sprintf("%04d-%02d-01",$year, $month);
                my $activedate = sprintf("%04d-%02d-%02d",$year, $month, $day);
               	@local = `perl $imapReporterPath/imapReporter.pl -from $activedate01 -to $activedate -service activeVmInbox -opco $opco -msgsvc moip -component mas -terse -detail $localDetailFilePath -tempdir=/opt/moip/logs`;
            } else {
               	@local = `perl $imapReporterPath/imapReporter.pl -days $date -service activeVmInbox -opco $opco -msgsvc moip -component mas -terse -detail $localDetailFilePath -tempdir=/opt/moip/logs`;
	    }
        } else {
            
            my $year  = substr( $date, 0, 4 );
            my $month = substr( $date, 4, 2 );
            
            my @endTime = `date -d'$monthName[$month-1] $daysPerMonthTable[$month-1] $year' +%s`;
            my $deltaTimeInSec = $deltaDate * SECINDAY;
            my $startTime = $endTime[0] - $deltaTimeInSec;
            my   ($sec,$min,$hour,$mday,$mon,$newYear,$wday,$yday,$isdst) = localtime($startTime);

            my $lastDay = $daysPerMonthTable[$month-1];
            my $activedatelast = sprintf("%04d-%02d-%02d",$year, $month, $lastDay);
            
            my $activedatefirst = sprintf("%04d-%02d-%02d",$newYear + 1900, $mon + 1, $mday);
            if(DEBUG) {
                print("This is from $activedatefirst to $activedatelast\n");
            }
           @local = `perl $imapReporterPath/imapReporter.pl -from $activedatefirst -to $activedatelast -service activeVmInbox -opco $opco -msgsvc moip -component mas -terse -detail $localDetailFilePath -tempdir=/opt/moip/logs`;
        }
        push( @result , "opco = $opco" );
        if(! ($local[0] =~ m/\d+/)){
         	push( @result , "activeVmInbox = 0" );
        } else {
         	push( @result , "activeVmInbox = $local[0]" );
         	push( @detailSubFileList, $localDetailFilePath);
        }
}

my $line;
foreach $line(@result) {
	chomp($line);
	if(!($line eq "")) {
		print("$line\n");
   	}
}
# Ouput detail subscriber file list as well if in detail mode
	foreach $line(@detailSubFileList) {
		chomp($line);
		if(!($line eq "")) {
			print("$line\n");
   		}
	}

sub callRemoteSystems() {
 
 foreach my $remote (@remoteSiteList){
  	my $ip = $remote;
 	if (DEBUG) {
   		print("remote=$remote  ip=$ip\n");
  	}
  	my $command;
  	if($deltaDate == -1) {
    		$command = "ssh -o StrictHostKeyChecking=no reports\@$ip \"cd $scriptPath; perl ./vmActiveInbox.pl local $date detail=$detailPath\"";
  	} else {
    		$command = "ssh -o StrictHostKeyChecking=no reports\@$ip \"cd $scriptPath; perl ./vmActiveInbox.pl local $date $originalDeltaDate detail=$detailPath\"";
  	}
 
  	if (DEBUG) {
   		print("callRemoteSystems() command = $command\n");
  	}
  	my @result = `$command`;
  	print("@result");
  
  	my $command;
  	my $result;
  	# extract detail sub file list from @result and scp each of them to $detailPath/ 
  	foreach(@result){
  		chomp;
  		# match the lines that have detail files
  		if (/imapReportSubDetail-(.+)-(.+)\.txt/){
  			$command = "scp -o StrictHostKeyChecking=no reports\@$ip:/$_ $detailPath/";
       			$result = `$command`;
       			if ($? != 0){
       				print "Failed to get $_ from remote site: $1. VVM active mailbox report on this site might be inaccurate\n";
       			}
       			# Remove detail file from remote site for clean
       			$command = "ssh -o StrictHostKeyChecking=no reports\@$ip \"rm -f $_\"";
			$result = `$command`;
  		}
  	}	
 } # for each remote site
}

sub readTopology() {

	@remoteSiteList = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar listRemoteSites -service moip -productPrefix moip");
	@siteName = JavaExecution::javaExec("$imapReporterPath/imapreporter_component.jar getLocalSiteName -service moip -productPrefix moip");

}

sub  syntax(){
    printf("Syntax:     $0 {local | all}  <yearmonth | days> <days>\n");
    printf("\tExample:\n");
    printf("\t\tFor the last 30 days \t $0 local 30\n");
    printf("\t\tFor September \t $0 local 201009\n");
    printf("\t\tFor last 20 days of September \t $0 local 201009 20\n");
}

__END__
