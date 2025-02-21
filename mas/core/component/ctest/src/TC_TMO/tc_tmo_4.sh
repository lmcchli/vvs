# Description and testcase number
test_description="Verify that MAS will use the P-Asserted-Identity header value if the value is a valid user and the From header also includes a valid user different from the user in the P-Asserted-Identity header"
test_id="4"

# The test function
function test_func {
	calling="302102007"
	called="999999"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	/usr/ucb/echo -n "Found Calling Party = \"<Uri = tel:$calling\" ... " >> $temp_log
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	tail -1 ./${base_filename}$$.tmp | grep "Uri = tel:$calling" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $3 }'`
        echo "FAIL: Found the following tag \"$temp\"" >> $temp_log; let failed++
    fi
}
