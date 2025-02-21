# this function procecesstd input an writes to a file
# the file reaches 5Mb size, the file is moved to 
# a .1 file.
#
# Input param: Log filename 
# 
# Syntax 
# 	<process> | <path to logger>logger.sh <log filename> 2>&1
#	output.sh | /opt/moip/mas/etc/logger.sh process.log 2>&1


MAX_FILESIZE=5000000
LOGFILE=$1

count=1
while true
do
        # read from stdin
        read line
        if [ $? -gt 0 ]
        then
        	# The stdin is broken. 
        	# Java process is probably dead.
        	# Exit script
                exit 1
        fi

        
 
        # Write to file
        echo "$line" >> $LOGFILE
        count=`expr $count + 1`

        # for every 10 lines, check filesize    
        if [ $count -gt 10 ]
        then
                count=1
                result=`cksum $LOGFILE |awk '{ print $2}'`

                # the file is larger then 5Mb. Move file
                if [ $result -gt $MAX_FILESIZE ]
                then
                	# Move logfile
                        mv $LOGFILE $LOGFILE".1"
                fi
        fi

done
