# Description and testcase number
test_description="Verify that it's possible to change mapping for \"ownername\" and \"sssportype\" to other attributes in RADIUS-MA"
test_id="20007073"

# Perform the acual test
function test_func {
	calling="1234"
	called="5678"
	murprefix_called="$murprefix`echo $called | tail +4c`"

	# Install the associated application
	install-vxml-application $prefix_dir/default.vxml.$test_id
	# Install the associated MAS configuration
	install-mas-configuration $prefix_dir/mas.xml.$test_id
	# Restart the MAS
	mas-restart

	# Make the SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	# Get the MER event log
	get-mer-log-filter-for-mas-username $MAS_HOST $calling

	# Check for the required MER events
	check-for-mer-event "MAE-Event-Description" "prepaid"
	check-for-mer-event "MAE-Message-Type" "30 (Image)"

	# Restore the MAS to its previous state
	restore-original-vxml-application
	restore-original-mas-configuration
	# Restart the MAS
	mas-restart
}
