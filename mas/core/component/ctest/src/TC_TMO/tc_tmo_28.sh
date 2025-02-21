# Description and testcase number
test_description="Verify that MAS will handle privacy=history for the second-to-last entry"
test_id="28"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	# Check for the correct PI entry
	/usr/ucb/echo -n "Found PI = \"<Redirecting Party = ...<PI = restricted>...>\" ... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $14 }' | \
		grep " <PI = restricted>" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $15 }'`
        echo "FAIL: Found the following PI \"$temp\"" >> $temp_log; let failed++
    fi
}
