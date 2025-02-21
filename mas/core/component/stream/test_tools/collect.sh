#!/bin/bash
HERE=`dirname $0`
SELF=`basename $0`
if test "$#" -lt 2; then
	echo Usage: ${SELF} '<executable> <core-file>'
	exit 1
fi
TMP_CFG=`mktemp`
TMP_OUT=`mktemp`
TMP_FILELIST=`mktemp`

cat >${TMP_CFG} <<- END_OF_GDB_CONFIG
echo #begin shared\n
info sharedlibrary
echo #end shared\n
quit
END_OF_GDB_CONFIG
gdb -x ${TMP_CFG} `which $1` $2 >${TMP_OUT} 2>&1
awk '/#begin shared/,/#end shared/ { if($1!~/(#|From)/){ print $4}}' ${TMP_OUT} > ${TMP_FILELIST}
echo Out=${TMP_OUT} File list=${TMP_FILELIST}

while true; do
perl - ${TMP_FILELIST} << 'END_OF_PERL'
	use File::Basename;
	$file=$ARGV[0];
	local($^I, @ARGV) = ('.orig', $file);
	$found = 0;
	$lines_map = {};
	while (<>) {
		chomp $_;
		push @lines,$_;
		$lines_map->{$_}=1;
		if (eof) {
			foreach (@lines) {
				$maybe_sym = qx{stat --format="%N" $_};
				print "$_\n";
				if($maybe_sym =~ m{' -> `}) {
					($src,$dst) = split(m{' -> `},$maybe_sym);
					chomp $dst;
					$dst =~ s{'}{}g;
					$src =~ s{`}{}g;
					$abs_dest = dirname($src)."/$dst";
					if(!exists $lines_map->{$abs_dest}) {
						print "$abs_dest\n";
						$found = 1;
					}
				}
			}
		}
	}
	exit($found);
END_OF_PERL
if test "$?" -eq 0; then
	break
fi
done
echo `cd \`dirname $2\`;pwd`/`basename $2` >> ${TMP_FILELIST}
echo `which $1` >> ${TMP_FILELIST} 
tar --files-from=${TMP_FILELIST} -cvzf collected_$2.tgz > /dev/null 2>&1
rm ${TMP_FILELIST}
rm ${TMP_CFG}
rm ${TMP_OUT}
