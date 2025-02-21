#!/bin/bash
#
# This script runs old and new generate text with some (a lot of) combinations
# and produces a diff.

# Since there are a few combinations, all are run.
# Start by keeping fax constant etc..

case $# in 
'2')
;;
*)
echo "Usage: $0 <language> <'and' identifier>. Make sure phr.<language> is in this directory" 
exit 1
;;
esac

cp $1.phr ./old/templates/.
AND="$2"; export AND;
/vobs/ipms/ntf/tools/phr2cphr.pl $1.phr > ./new/templates/$1.cphr 

list="0 1 2 3"

let "total = 4 ** 4"
iterations="1"
failed="0"
for fcount in $list; do
    for vcount in $list; do 
        for ecount in $list; do 
            for mcount in $list; do 
                echo "testing ($vcount,$fcount,$ecount,$mcount) test $iterations of $total"
                oldoutput=`java -cp ./old/ntf.jar:/vobs/ipms/ntf_oem/jaf-1.0.2/activation.jar:/vobs/ipms/ntf_oem/javamail-1.3.2/mail.jar:/vobs/ipms/ntf/bin/foundation.jar -DntfHome=./old com.mobeon.ntf.text.TextCreator -d ./old -f $fcount -e $ecount -v $vcount -m $mcount -l $1 -c c > ./oldoutput 2>/dev/null`
                newoutput=`java -cp /vobs/ipms/ntf/bin/ntf.jar:/vobs/ipms/ntf_oem/jaf-1.0.2/activation.jar:/vobs/ipms/ntf_oem/javamail-1.3.2/mail.jar:/vobs/ipms/ntf/bin/foundation.jar -DntfHome=./new com.mobeon.ntf.text.TextCreator -d ./new -f $fcount -e $ecount -v $vcount -m $mcount -l $1 -c c > ./newoutput 2>/dev/null`
                diff oldoutput newoutput
                if [ $? -eq 1 ]; then let failed++; fi
                let iterations++
                let mcount++
            done
        let ecount++
        done
    let vcount++
    done
let fcount++
done

echo "**** RESULT *****"
echo "Failed: $failed of $total"
exit $failed
