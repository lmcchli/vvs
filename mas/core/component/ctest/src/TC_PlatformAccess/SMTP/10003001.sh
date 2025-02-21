# Description and testcase number
test_description="PlatformAccess - SMTP - messageForward"
test_id="10003001"

# The test function
function test_func {
	# Load helper functions
	load_config $prefix_dir/../helpers.sh

	# Install required files
	setup

	# Delete all messages
	imap_delete_all_messages $MS_HOST 143 $A_NUMBER $password
	# Create a single message
	imap_create_message $MS_HOST 143 $A_NUMBER $password "Fredd Foobar <$A_NUMBER@lab.mobeon.com>" "Fredd Foobar <$A_NUMBER@lab.mobeon.com>" "A subject" "The message body"
	
	INBOX_BEFORE=`./imap_client.pl $MS_HOST $A_NUMBER $password "STATUS inbox (unseen)" | grep EXIST | awk '{print $2}'`
	
	# Make the SIPP call
	num=`echo $test_id | cut -c5-`
	make-sipp-call $prefix_dir/../platformaccess.xml $num $num
	if [ $? -ne 0 ]; then return 1; fi

	get-mas-log-and-filter "TC $test_id"
	tail -1 ./${base_filename}$$.tmp | grep "TC $test_id PASSED" > /dev/null
	if [ $? -eq 0 ]; then
		sleep 20
		INBOX_AFTER=`./imap_client.pl $MS_HOST $A_NUMBER $password "STATUS inbox (unseen)" | grep EXIST | awk '{print $2}'`
		if [ `expr $INBOX_BEFORE + 1` -eq $INBOX_AFTER ]; then
			echo "PASS" >> $temp_log; let passed++
		else
			echo "FAIL: found $INBOX_AFTER messages. Should have been `expr $INBOX_BEFORE + 1`" >> $temp_log; let passed++
		fi
	else
		result=`tail -1 ./${base_filename}$$.tmp | awk -F' ' '{ print $10, $11, $12 }'`
		echo "FAIL: the test case result found in the MAS log was \"$result\"" >> $temp_log; let failed++
	fi
	
	# Restore/clean up
	cleanup
}
