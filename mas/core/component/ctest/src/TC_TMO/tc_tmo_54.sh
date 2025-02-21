# Description and testcase number
test_description="Verify that MAS will use the service 1234 if the request URI is 1234"
test_id="54"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	# Copy the MAS configuration and change it according to the settings
	cp $prefix_dir/ComponentConfig.xml $prefix_dir/ComponentConfig.xml.$test_id.tmp
	replace ___MAS_HOST___ $MAS_HOST $prefix_dir/ComponentConfig.xml.$test_id.tmp
	# "Save" the current ComponentConfig.xml file
	ssh $MAS_USERNAME@$MAS_HOST "mv $MAS_PATH/etc/ComponentConfig.xml $MAS_PATH/etc/ComponentConfig.xml.PRE-$test_id"
	# Install the files (ComponentConfig.xml, 1234.xml and 1234.ccxml)
	scp $prefix_dir/ComponentConfig.xml.$test_id.tmp $1 $MAS_USERNAME@$MAS_HOST:$MAS_PATH/etc/ComponentConfig.xml > /dev/null
	scp $prefix_dir/1234.xml $MAS_USERNAME@$MAS_HOST:$MAS_PATH/applications/vva0001.1/1234.xml > /dev/null
	scp $prefix_dir/1234.ccxml $MAS_USERNAME@$MAS_HOST:$MAS_PATH/applications/vva0001.1/1234.ccxml > /dev/null
	# Remove the temporary ComponentConfig.xml file
	rm -f $prefix_dir/ComponentConfig.xml.$test_id.tmp
	# Restart the MAS
	mas-restart
	
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	get-mas-log-and-filter "Trying to load service"
	
	# Check for "Trying to load service based on called party: 1234"
	/usr/ucb/echo -n "Found \"Trying to load service based on called party: 1234\"... " >> $temp_log
	grep "Trying to load service based on called party: 1234" ./${base_filename}$$.tmp > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		echo "FAIL" >> $temp_log; let failed++
    fi
	
	# "Restore" the original ComponentConfig.xml file
	ssh $MAS_USERNAME@$MAS_HOST "mv $MAS_PATH/etc/ComponentConfig.xml.PRE-$test_id $MAS_PATH/etc/ComponentConfig.xml"
	ssh $MAS_USERNAME@$MAS_HOST "rm -f $MAS_PATH/applications/vva0001.1/1234.*"
	# Restart the MAS
	mas-restart
}
