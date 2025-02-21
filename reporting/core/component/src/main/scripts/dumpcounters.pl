################################################################################
# Dump to the console into a human-readable format the content of an MMC .xpd or
# .xmlpd performance data file.
#
# Author: Yves Canty (lmcyvca)
#
# This is for development use only and is not officially supported.
################################################################################

use strict;

use constant DEBUG        => 0;

my $line;
my %counters;
my $countersPath        = "/opt/global/perf/moip/aggregated/";
my $vmCountersInputList = "vmCounters.txt";
my @validCounters  = ();


# Check the input parameters first
if ( scalar(@ARGV) < 1 ) {
 	usage ();
}

sub usage {
	print "perl dumpcounters.pl <filename> [filter] \n ";
	print " filename is the filename to dump \n";
	print " filter is an option to filter the counters during the dump \n ";
	print " it expects the file vmCounters.txt file in the directory \n";
	exit;
}


if (DEBUG) {
	print " argument number @ARGV   \n";
}


my $fileName =  $ARGV[0];

# Check the input parameters first
if ( scalar(@ARGV) > 1 ) {

	my $arg = $ARGV[1];
	
	if ($arg eq "filter")  {

		 # Get the path to be able to call the sub-perl command correctly
 		my $scriptPath = getExecutionPath();


		 # Get the list of counters we want to track
 		@validCounters = `cat ${scriptPath}${vmCountersInputList}`;

 		chomp(@validCounters);
 		foreach my $counter (@validCounters) {
			chomp($counter);
   			$counter =~ s/\r//;
   			$counter =~ s/\[//g;
   			$counter =~ s/\]//g;
   			$counters{ $counter } = 0;
   			if (DEBUG) {
     			print( "Counter selected: " . ${counter} . "\n" );
   			}
 		}
 		
	}
}


my @names;
my @values;


open(FH, "<", $fileName) or die "cannot open < $fileName $!";

foreach $line (<FH>) {
    chomp($line);

    if ($line =~ m/<cbt>(.*)<\/cbt>/) {
        print("===== Collection Begin Time (CBT): " . formatTime($1) . " =====\n");
    }
    elsif ($line =~ m/<ts>(.*)<\/ts>/) {
        print("===== End Time Stamp (TS): " . formatTime($1) . " =====\n");
    }
    elsif ($line =~ m/<mts>(.*)<\/mts>/) {
        print("\n", "="x72, "\n");
        print("===== Measurement Time Stamp (MTS): " . formatTime($1) . "\n");
    }
    elsif ($line =~ m/<moid>(.*)<\/moid>/) {
        print("\n", "-"x40, "\n");
        print("----- ", $1, " :\n\n");
        @values = ();
    }
    elsif ($line =~ m/<mt>(.*)<\/mt>/) {
       push(@names, $1);
    }
    elsif ($line =~ m/<r>(.*)<\/r>/) {
        push(@values, $1);
    }
    elsif ($line =~ m/<sf>/) {
        my $i = 0;
        my $n;

        foreach $n (@names) {
  			if ( @validCounters > 0) {
				if (grep(/$n/,  @validCounters)) {
					print($n, " = ", $values[$i] . "\n");
				}
  			} else {
  	  			print($n, " = ", $values[$i] . "\n");
  			}
         	$i += 1;
        }
        print("\n");
        @values = ();
    }
    elsif ($line =~ m/<\/mi>/) {
        @names = ();
        @values = ();
    }
}

sub formatTime() {
    my($time) = @_;

    $time =~ m/(\d\d\d\d)(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)/;

    return $1 . "-" . $2 . "-" . $3 . " " . $4 . ":" . $5 . ":" . $6;
}

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
