# Description and testcase number
test_description="Set \"operation\" event property"
test_id="20007072"

# Perform the acual test
function test_func {
	calling="1234"
	called="5678"
	murprefix_called="$murprefix`echo $called | tail +4c`"

	# Install the associated application
	install-vxml-application $prefix_dir/default.vxml.$test_id
	# Restart the MAS
	mas-restart
	
	# Make the SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $murprefix_called
	if [ $? -ne 0 ]; then return 1; fi
	
	# Get the MER event log
	get-mer-log-filter-for-mas-username $MAS_HOST $calling

	# Check for the required MER events
	check-for-mer-event "MAE-Event-Type" "28 (Debit)"
	check-for-mer-event "MAE-Event-Type" "29 (Refund)"
	
	# Restore the MAS to its previous state
	restore-original-vxml-application
	# Restart the MAS
	mas-restart
}
