# Description and testcase number
test_description="PlatformAccess - IMAP - mailboxGetMessageSubList"
test_id="10002005"

# The test function
function test_func {
	# Load helper functions
	load_config $prefix_dir/../helpers.sh

	# Install required files
	setup

	# Delete all messages
	imap_delete_all_messages $MS_HOST 143 $A_NUMBER 12345678
	# Create 10 new messages
	imap_create_messages $MS_HOST 143 $A_NUMBER 12345678 \
		fred@foobar.org barney@foobar.org 10
	# "Read" three of the messages
	imap_read_message $MS_HOST 143 $A_NUMBER 12345678 1
	imap_read_message $MS_HOST 143 $A_NUMBER 12345678 8
	imap_read_message $MS_HOST 143 $A_NUMBER 12345678 9

	# Make the SIPP call
	num=`echo $test_id | cut -c5-`
	make-sipp-call $prefix_dir/../platformaccess.xml $num $num
	if [ $? -ne 0 ]; then return 1; fi

	get-mas-log-and-filter "TC $test_id"
	tail -1 ./${base_filename}$$.tmp | grep "TC $test_id PASSED" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
	else
		result=`tail -1 ./${base_filename}$$.tmp | awk -F' ' '{ print $10, $11, $12 }'`
		echo "FAIL: the test case result found in the MAS log was \"$result\"" >> $temp_log; let failed++
	fi

	# Restore/clean up
	cleanup
}
