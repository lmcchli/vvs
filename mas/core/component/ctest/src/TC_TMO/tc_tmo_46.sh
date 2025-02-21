# Description and testcase number
test_description="Verify that MAS will not have any restriction on RDNIS if the Diversion header includes a privacy header with the value \"off\""
test_id="46"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	
	# Check the RDNIS privacy
	/usr/ucb/echo -n "Found RDNIS privacy != restricted... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $14 }' | \
		sed -e 's/[ <PI=>]*//g' | \
		grep "restricted" > /dev/null
	if [ $? -eq 0 ]; then
		temp=`tail -1 ./${base_filename}$$.tmp | \
			awk -F, '{ print $14 }' | \
			sed -e 's/[ <PI=>]*//g'`
		
		echo "FAIL: Found the following RDNIS privacy: \"$temp\"" >> $temp_log; let failed++
    else
		echo "PASS" >> $temp_log; let passed++
    fi
}
