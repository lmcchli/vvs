# Description and testcase number
test_description="Verify that MAS will use the information from the Diversion header if History-Info also includes redirection information"
test_id="42"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"

	# Check for "...<Redirecting Party = <Uri = sip:12341234@host.com>"
	# as specified in the Diversion header
	/usr/ucb/echo -n "Found ...<Redirecting Party = <Uri = sip:12341234@host.com>... " >> $temp_log
	tail -1 ./${base_filename}$$.tmp | \
		awk -F, '{ print $11 }' | \
		cut -f3- -d '=' | \
		sed 's/[ >]//g' | \
		grep "sip:12341234@host.com" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | \
			awk -F, '{ print $11 }' | \
			cut -f3- -d '=' | \
			sed 's/[ >]//g'`
		echo "FAIL: Found : \"$temp\"" >> $temp_log; let failed++
    fi
}
