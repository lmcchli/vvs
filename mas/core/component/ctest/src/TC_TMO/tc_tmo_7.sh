# Description and testcase number
test_description="MAS will set the calling party as unknown"
test_id="7"

# The test function
function test_func {
	header_p_asserted_id="vvnd332"
	header_from="302102008"
    header_remote_party_id="302102009"
	
	echo "SEQUENTIAL" > ./numbers_$$.csv
	echo "$header_p_asserted_id;$header_from;$header_remote_party_id" >> ./numbers_$$.csv
	make-sipp-call $prefix_dir/$base_filename.xml -csv ./numbers_$$.csv
	if [ $? -ne 0 ]; then rm ./numbers_$$.csv; return 1; fi
	rm ./numbers_$$.csv

	/usr/ucb/echo -n "evt.connection.remote.number resulted in ''... " >> $temp_log
	get-mas-log-and-filter "PhoneNumber_GetAnalyzedNumber.*evt.connection.remote.number"
	tail -1 ./${base_filename}$$.tmp | grep "resulted in ''" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		temp=`tail -1 ./${base_filename}$$.tmp | awk -F, '{ print $5 }' | awk '{ print $4 }'`
        echo "FAIL: Found the following tag \"evt.connection.remote.number resulted in $temp\"" >> $temp_log; let failed++
    fi
}
