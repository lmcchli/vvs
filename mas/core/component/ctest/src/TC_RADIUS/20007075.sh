# Description and testcase number
test_description="Verify that MAS uses the backup solution configured if MCR lookup fails"
test_id="20007075"

# Perform the actual test
function test_func {
	calling="7075"
	called="5678"
	murprefix_called="$murprefix`echo $called | tail +4c`"

	# Install the associated application
	install-vxml-application $prefix_dir/default.vxml.mcr.service.accounting.txt
	# Install the associated MAS configuration
	install-mas-configuration $prefix_dir/mas.xml.$test_id
	# Restart the MAS
	mas-restart

	# Make the SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	# Get the XMP log
	get-xmp-log-at $XMP_HOST $XMP_USERNAME $XMP_PATH/server.log

	# Validate XMP events 
	check-xmp-events "transaction-id=123" "client-id=5555" "service-id=OutdialNotification" "number=67890" "mailbox-id=12345"

	# Restore the MAS to its previous state
	restore-original-vxml-application
	restore-original-mas-configuration
	# Restart the MAS
	mas-restart
}
