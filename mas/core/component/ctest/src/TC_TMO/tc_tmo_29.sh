# Description and testcase number
test_description="Verify that MAS will ignore the privacy=history for all entries other than the second-to-last history-info entry"
test_id="29"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/"$base_filename"b.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"

	# Make sure no numbers are private or restricted
	/usr/ucb/echo -n "Checking for private/restricted numbers..." >> $temp_log
	tail -1 ./${base_filename}$$.tmp | grep " <PI = unknown>" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
        echo "FAIL: Found one or more private/restricted numbers" >> $temp_log; let failed++
    fi
}
