# Description and testcase number
test_description="Verify that MAS will will handle more than one privacy parameter"
test_id="51"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	
	# Check the ANI privacy
	/usr/ucb/echo -n "Found ANI privacy = restricted... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $6 }' | \
		sed -e 's/[ <PI=>]*//g' | \
		grep "restricted" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | \
			awk -F, '{ print $6 }' | \
			sed -e 's/[ <PI=>]*//g'`
		echo "FAIL: Found the following ANI privacy: \"$temp\"" >> $temp_log; let failed++
    fi
	
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
