# Description and testcase number
test_description="PlatformAccess - IMAP - mailboxGetFolder"
test_id="10002002"

# The test function
function test_func {
	# Load helper functions
	load_config $prefix_dir/../helpers.sh

	# Install required files
	setup

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
