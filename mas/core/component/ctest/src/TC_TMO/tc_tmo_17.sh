# Description and testcase number
test_description="Verify that MAS sets the redirection number (RDNIS) to the second-to-last and the cause to the last value from the history-info header"
test_id="17"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	/usr/ucb/echo -n "Found RDNIS = \"<Redirecting Party = ...<TelephoneNumber = $called...>\" ... " >> $temp_log
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	
	# Check for the correct RDNIS
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $13 }' | \
		sed -e 's/[^0-9]//g' | \
		grep "$called" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $13 }' | sed -e 's/[^0-9]//g'`
        echo "FAIL: Found the following RDNIS \"$temp\"" >> $temp_log; let failed++
    fi
}
