test_description="SIP - GTD support - Get Redirect Reason (no reply) from Diversion part of SIP INVITE"
test_id="20010101"
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


	# Simulate a deposit call coming in from Safari gateway to MAS
#	make-sipp-call emarfro_testcases/100.xml $calling $called $redir
	
	# Verify that MAS has picked up correct telephone numbers and such from diversion header....
	get-mas-log-and-filter "This is the C-reason:"
	tail -1 ./${base_filename}$$.tmp | grep "This is the C-reason: no reply" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
	else
		result=`tail -1 ./${base_filename}$$.tmp`
#		result=`tail -1 ./${base_filename}$$.tmp | awk -F' ' '{ print $10, $11, $12 }'`
		echo "FAIL: the test case result found in the MAS log was $result" >> $temp_log; let failed++
	fi
	
	# Restore the old MAS config
#	restore-mas-conf

	# Restore/clean up
	cleanup
		
}