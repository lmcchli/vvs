# Description and testcase number
test_description="PlatformAccess - Util - appendMediaObjects"
test_id="10002000"

# The test function
function test_func {
	# Load helper functions
	load_config $prefix_dir/../helpers.sh

	# Install required files
	setup

	# 1. Install a MCP package
	# 2. Create mediaobjects from MCP
	# 3. Append them
	# 4. Play he result and check that they are appended.

	# Clear the MAS log
	ssh $MAS_USERNAME@$MAS_HOST "cat /dev/null > $MAS_PATH/log/mas.log"

	# Make the SIPP call
	num=`echo $test_id | cut -c5-`
	make-sipp-call $prefix_dir/10002000.xml $num $num
#	if [ $? -ne 0 ]; then return 1; fi

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
