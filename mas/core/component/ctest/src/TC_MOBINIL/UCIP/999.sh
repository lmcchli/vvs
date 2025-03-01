test_description="UCIP - GetAccountDetails testcase-collection"
test_id="999"
function test_func {
 
	# Load helper functions
	load_config $prefix_dir/../helpers.sh

	# Update MAS config with necessary changes
#	replace-mas-conf-param useragentwithphoneinsipuributnouserparameter empty

	# Install required files
	setup

	# Clear the mas.log
	ssh $MAS_USERNAME@$MAS_HOST "cat /dev/null > $MAS_PATH/log/mas.log"

	# Make the SIPP call
	num=`echo $test_id | cut -c5-`
	make-sipp-call $prefix_dir/$test_id.xml $A_NUMBER $B_NUMBER $C_NUMBER
#	if [ $? -ne 0 ]; then return 1; fi

	# Verify that MAS has picked up correct values

# This loop �r en specialare
test_id="20016011"
while [  $test_id -lt 20016018 ]; do
  
  if [ $test_id = "20016011" ]; then
  	test_description="UCIP - GetAccountDetails - firstIVRCallFlag"
  elif [ $test_id = "20016012" ]; then
  	test_description="UCIP - GetAccountDetails - supervisionExpiryDate"
  elif [ $test_id = "20016013" ]; then
  	test_description="UCIP - GetAccountDetails - serviceFeeExpiryDate"
  elif [ $test_id = "20016014" ]; then
  	test_description="UCIP - GetAccountDetails - serviceClass"
  elif [ $test_id = "20016015" ]; then
  	test_description="UCIP - GetAccountDetails - languageIDCurrent"
  elif [ $test_id = "20016016" ]; then
  	test_description="UCIP - GetAccountDetails - temporaryBlockedFlag"
  elif [ $test_id = "20016017" ]; then
  	test_description="UCIP - GetAccountDetails - serviceClassCurrent"
  else
  	test_description="Dont know"
  fi

passed=0
failed=0

	get-mas-log-and-filter "TC $test_id"
			tail -1 ./${base_filename}$$.tmp | grep "TC $test_id PASSED" > /dev/null
			if [ $? -eq 0 ]; then
				echo "PASS" >> $temp_log; let passed++
			else
					result=`tail -1 ./${base_filename}$$.tmp`
					echo "FAIL: the test case result found in the MAS log was $result" >> $temp_log; let failed++
			fi
			
			echo -n "Result from test: " | tee -a $MAIN_LOG
( [ $failed -eq 0 ] && [ $passed -gt 0 ] ) && ( echo -n "Passed: " | tee -a $MAIN_LOG ) || ( echo -n "Failed: " | tee -a $MAIN_LOG )
echo "$test_description: $test_id" | tee -a $MAIN_LOG
if [ $failed -ne 0 ]; then
	echo "Find additional information in $MAIN_LOG"
fi

     	let test_id=test_id+1 
done

test_id="999"
test_description="UCIP - GetAccountDetails"

	# Restore the old MAS config
#	restore-mas-conf

	# Restore/clean up
	cleanup
		
}