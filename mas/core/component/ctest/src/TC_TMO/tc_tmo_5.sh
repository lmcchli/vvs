# Description and testcase number
test_description="MAS uses the value from P-Asserted-Identity field for setting the calling party number"
test_id="5"

# The test function
function test_func {
	header_p_asserted_id="302102007"
	header_from="302102008"
    header_remote_party_id="302102009"
	
	echo "SEQUENTIAL" > ./numbers_$$.csv
	echo "$header_p_asserted_id;$header_from;$header_remote_party_id" >> ./numbers_$$.csv
	make-sipp-call $prefix_dir/$base_filename.xml -csv ./numbers_$$.csv
	if [ $? -ne 0 ]; then rm ./numbers_$$.csv; return 1; fi
	rm ./numbers_$$.csv

	/usr/ucb/echo -n "Found Calling Party = \"<Uri = tel:$header_p_asserted_id\" ... " >> $temp_log
	get-mas-log-and-filter "com.mobeon.masp.callmanager.callhandling.CallFactory"
	tail -1 ./${base_filename}$$.tmp | grep "Uri = tel:$header_p_asserted_id" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $3 }'`
        echo "FAIL: Found the following tag \"$temp\"" >> $temp_log; let failed++
    fi
}
