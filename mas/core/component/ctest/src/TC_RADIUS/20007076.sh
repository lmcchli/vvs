# Description and testcase number
test_description="Verify that MAS uses the backup solution configured if MCR is not to be used"
test_id="20007076"

# Perform the actual test
function test_func {
	calling="7075"
	called="5678"
	murprefix_called="$murprefix`echo $called | tail +4c`"

	# Start the XMP servers
	start-xmp-server $XMP_HOST $XMP_USERNAME $XMP_PATH $XMP_PORT
	start-xmp-server $XMP_ALT_HOST $XMP_ALT_USERNAME $XMP_ALT_PATH $XMP_ALT_PORT
	# Register an XMP server with the "accounting" service in MCR
	register-accounting-xmp-server-in-mcr $MCR_HOST "$MCR_BIND_DN" $MCR_PASSWORD $XMP_HOST $XMP_PORT
	
	# Install the associated application
	install-vxml-application $prefix_dir/default.vxml.mcr.service.accounting.txt
	# Copy the MAS configuration and change it according to the settings
	cp $prefix_dir/mas.xml.$test_id $prefix_dir/mas.xml.$test_id.tmp
	replace ___XMP_HOST___ $XMP_ALT_HOST $prefix_dir/mas.xml.$test_id.tmp
	replace ___XMP_PORT___ $XMP_ALT_PORT $prefix_dir/mas.xml.$test_id.tmp
	# Install the associated MAS configuration
	install-mas-configuration $prefix_dir/mas.xml.$test_id.tmp
	# Remove the MAS configuration
	rm -f $prefix_dir/mas.xml.$test_id.tmp
	# Restart the MAS
	mas-restart

	# Make the first SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	# Get the XMP log
	get-xmp-log-at $XMP_ALT_HOST $XMP_ALT_USERNAME $XMP_ALT_PATH/server.log

	# Validate XMP events 
	check-xmp-events "transaction-id=123" "client-id=5555" "service-id=OutdialNotification" "number=67890" "mailbox-id=12345"

	# Stop the backup XMP server
	kill-process-by-port $XMP_ALT_HOST $XMP_ALT_USERNAME java $XMP_ALT_PORT
	
	# Make the second SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	# Get the XMP log
	get-xmp-log-at $XMP_HOST $XMP_USERNAME $XMP_PATH/server.log

	# Verify that no event is sent to the XMP server
	check-for-empty-xmp-log

	# Stop the XMP server
	kill-process-by-port $XMP_HOST $XMP_USERNAME java $XMP_PORT
	
	# Unegister the XMP server from MCR
	unregister-accounting-xmp-server-from-mcr $MCR_HOST "$MCR_BIND_DN" $MCR_PASSWORD $XMP_HOST

	# Restore the MAS to its previous state
	restore-original-vxml-application
	restore-original-mas-configuration
	# Restart the MAS
	mas-restart
}
