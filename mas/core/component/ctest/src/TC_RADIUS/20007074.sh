# Description and testcase number
test_description="Verify that the events \"accountingdebit\" and \"accountingrefund\" can be disabled"
test_id="20007074"

# Perform the actual test
function test_func {
	calling="7074"
	called="5678"
	murprefix_called="$murprefix`echo $called | tail +4c`"

	# Install the associated application
	install-vxml-application $prefix_dir/default.vxml.$test_id
	# Install the associated MAS trafficevents configuration
	install-mas-trafficevents-configuration $prefix_dir/trafficevents.xml.$test_id
	# Restart the MAS
	mas-restart

	# Make the SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
	
	# Get the MER event log
	get-mer-log-filter-for-mas-username $MAS_HOST $calling $called

	# Check for disabled (missing) MER events
	check-for-missing-mer-event "MAE-Event-Type" "28 (Debit)"
	check-for-missing-mer-event "MAE-Event-Type" "29 (Refund)"

	# Restore the MAS to its previous state
	restore-original-vxml-application
	restore-original-mas-trafficevents-configuration
	# Restart the MAS
	mas-restart
}
