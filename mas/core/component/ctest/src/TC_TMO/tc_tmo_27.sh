# Description and testcase number
test_description="Verify that MAS will handle the case when the INVITE includes History-Info with no reason without handling the call as a deposit"
test_id="27"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	# Check for the correct RDNIS
	/usr/ucb/echo -n "Found RDNIS = \"<Redirecting Party = null>\" ... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $11 }' | \
		grep " <Redirecting Party = null>" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $11 }'`
        echo "FAIL: Found the following RDNIS \"$temp\"" >> $temp_log; let failed++
    fi
}
