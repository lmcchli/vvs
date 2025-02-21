# Description and testcase number
test_description="Verify that MAS sets the correct reason for the redirection when the redirection reason is 410"
test_id="25"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	# Check for the correct RDNIS
	/usr/ucb/echo -n "Found RDNIS = \"<Redirecting Party = ...<TelephoneNumber = $called...>\" ... " >> $temp_log
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
	
	# Check for the correct cause
	/usr/ucb/echo -n "Found cause = \"<Redirecting Party = ...<Cause = unknown (410)>>\" ... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $15 }' | \
		sed -e 's/ <Cause = //g' | \
		sed -e 's/>>//g' | \
		grep "unknown (410)" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | \
			awk -F, '{ print $15 }' | \
			sed -e 's/ <Cause = //g' | \
			sed -e 's/>>//g'`
        echo "FAIL: Found the following cause \"$temp\"" >> $temp_log; let failed++
    fi
}
