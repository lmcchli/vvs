# Description and testcase number
test_description="Verify that the called party information is retrieved from the request URI"
test_id="1"

# The test function
function test_func {
	calling="302102007"
	called="999999"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	
	/usr/ucb/echo -n "Found Called Party = \"<Uri = sip:111111@...\" ... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $8 }' | \
		cut -f3- -d '=' | \
		grep "sip:111111@.*" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | \
			awk -F, '{ print $8 }' | \
			cut -f3- -d '=' | \
			sed -e 's/>//g'`
        echo "FAIL: Found the following tag \"$temp\"" >> $temp_log; let failed++
    fi
}
