# Description and testcase number
test_description="Verify that it is possible to change the configuration for the sendprovisionalresponsesreliable in mas.xml to \"no\""
test_id="7"

# The test function
function test_func {
	# Install the configuration files
	install-mas-configuration $prefix_dir/mas.xml.sprr_no
	# Reload the MAS configuration
	mas-reload-config
	# Verify that there is nothing in the MAS log like provisional
	ssh $MAS_USERNAME@$MAS_HOST "egrep -i provisional $MAS_PATH/log/mas.log" > ./${base_filename}$$.tmp
	cat ./${base_filename}$$.tmp | grep -i provisional > /dev/null
	if [ $? -eq 0 ]; then
		echo "FAIL" >> $temp_log; let failed++
	else
		echo "PASS" >> $temp_log; let passed++
	fi

	# Restore the original MAS state
	restore-original-mas-configuration
	# Reload the MAS configuration
	mas-reload-config
}
