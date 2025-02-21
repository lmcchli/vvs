# Description and testcase number
test_description="ESS - Events - Check section essconfig in mas.xsd"
test_id="10002003"

# The test function
function test_func {
	# Load helper functions
#	load_config $prefix_dir/../helpers.sh

	# Install required files
#	setup
	
	# Clear the mas.log
#	ssh $MAS_USERNAME@$MAS_HOST "cat /dev/null > $MAS_PATH/log/mas.log"



	# Make the SIPP call
	num=`echo $test_id | cut -c5-`
	make-sipp-call $prefix_dir/../platformaccess.xml $num $num
#	if [ $? -ne 0 ]; then return 1; fi

#get-mas-configs-and-filter ""
	ssh $MAS_USERNAME@$MAS_HOST "egrep -e 'essconfig' $MAS_PATH/cfg/mas.xsd" > ./${base_filename}$$.tmp
	tail -1 ./${base_filename}$$.tmp | grep "essconfig" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
	else
#		result=`tail -1 ./${base_filename}$$.tmp | awk -F' ' '{ print $10, $11, $12 }'`
		result=`tail -1 ./${base_filename}$$.tmp`
		echo "FAIL: the test case result found in the MAS log was \"$result\"" >> $temp_log; let failed++
	fi

	# Restore/clean up
#	cleanup
}
