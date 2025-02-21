# Description and testcase number
test_description="MAS will use the value from the Remote-Party-ID to set the calling party number"
test_id="13"

# The test function
function test_func {
	header_from="302102007"
	header_remote_party_id="302102008"
	
	echo "SEQUENTIAL" > ./numbers_$$.csv
	echo "$header_remote_party_id;$header_from" >> ./numbers_$$.csv
	make-sipp-call $prefix_dir/$base_filename.xml -csv ./numbers_$$.csv
	if [ $? -ne 0 ]; then rm ./numbers_$$.csv; return 1; fi
	rm ./numbers_$$.csv

	/usr/ucb/echo -n "Found Calling Party = \"<Uri = sip:$header_remote_party_id\" ... " >> $temp_log
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	tail -1 ./${base_filename}$$.tmp | grep "Uri = sip:$header_remote_party_id" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $3 }'`
		echo "FAIL: Found the following tag \"$temp\"" >> $temp_log; let failed++
    fi
}
