#! /bin/sh
host=<message host>
port=25
sender=senser
telnum=46700000000
#file=examplefile_mwioff.txt
#file=examplefile_slamdown.txt
function=mwioff
#function=slamdown
/usr/local/j2se/bin/java -cp .:mail.jar:activation.jar MWIOffSender -h $host -p $port -sender $sender -telnum $telnum -function $function
