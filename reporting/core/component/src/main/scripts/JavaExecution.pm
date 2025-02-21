package JavaExecution;

use strict;

###############################################################################
# Executes the given java command and returns its standard output.
###############################################################################
sub javaExec {
    my ($cmd) = @_;

    my $javaHome = $ENV{JAVA_HOME};

    my $javaCmd = "\"$javaHome/bin/java\" -jar $cmd";

    if ( !$javaHome ) {
        $javaHome = `which java > /dev/null 2>&1`;

        if ( $? == 0 ) {
            $javaCmd = "\"$javaHome/bin/java\" -jar $cmd";
        }
        else {
            my $javaExec = `find /opt/jdk* -wholename '*bin/java' | head -1`;
            chomp($javaExec);
            
            if ($javaExec) {
                $javaCmd = "\"$javaExec\" -jar $cmd";
            }
        }
    }
    
    my @stdout = `$javaCmd`;

    foreach my $line (@stdout) {
        chomp($line);
    }

    return @stdout;
}

1;