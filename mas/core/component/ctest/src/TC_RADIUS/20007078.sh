# Description and testcase number
test_description="Verify that MAS switches between available service instances"
test_id="20007078"

# Perform the actual test
function test_func {
	calling="7075"
	called="5678"
	murprefix_called="$murprefix`echo $called | tail +4c`"

	# Start the XMP servers
	start-xmp-server $XMP_HOST $XMP_USERNAME $XMP_PATH $XMP_PORT
	start-xmp-server $XMP_ALT_HOST $XMP_ALT_USERNAME $XMP_ALT_PATH $XMP_ALT_PORT
	# Register two XMP servers having the "accounting" service in MCR
	register-accounting-xmp-server-in-mcr $MCR_HOST "$MCR_BIND_DN" $MCR_PASSWORD $XMP_HOST $XMP_PORT
	register-accounting-xmp-server-in-mcr $MCR_HOST "$MCR_BIND_DN" $MCR_PASSWORD $XMP_ALT_HOST $XMP_ALT_PORT
	
	# Install the associated application
	install-vxml-application $prefix_dir/default.vxml.mcr.service.accounting.txt
	# Restart the MAS
	mas-restart

	# Make the first SIPP call
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi

	# Get the XMP log at the first server
	get-xmp-log-at $XMP_HOST $XMP_USERNAME $XMP_PATH/server.log
	# If it is not empty, kill the server
	if [ -s "./${base_filename}$$.tmp" ]; then
		kill-process-by-port $XMP_HOST $XMP_USERNAME java $XMP_PORT
	
		# Make the second SIPP call
		make-sipp-call $prefix_dir/$base_filename.xml $calling $called
		if [ $? -ne 0 ]; then return 1; fi
	
		# Get the XMP log
		get-xmp-log-at $XMP_ALT_HOST $XMP_ALT_USERNAME $XMP_ALT_PATH/server.log

		# Validate XMP events
		check-xmp-events "transaction-id=123" "client-id=5555" "service-id=OutdialNotification" "number=67890" "mailbox-id=12345"
		# Kill the remaining XMP server
		kill-process-by-port $XMP_ALT_HOST $XMP_ALT_USERNAME java $XMP_ALT_PORT
	else
		# Otherwise, kill the other XMP server
		kill-process-by-port $XMP_ALT_HOST $XMP_ALT_USERNAME java $XMP_ALT_PORT
		# Make the second SIPP call
		make-sipp-call $prefix_dir/$base_filename.xml $calling $called
		if [ $? -ne 0 ]; then return 1; fi
	
		# Get the XMP log
		get-xmp-log-at $XMP_HOST $XMP_USERNAME $XMP_PATH/server.log

		# Validate XMP events
		check-xmp-events "transaction-id=123" "client-id=5555" "service-id=OutdialNotification" "number=67890" "mailbox-id=12345"
		# Kill the remaining XMP server
		kill-process-by-port $XMP_HOST $XMP_USERNAME java $XMP_PORT
	fi
	
	# Unregister the XMP servers from MCR
	unregister-accounting-xmp-server-from-mcr $MCR_HOST "$MCR_BIND_DN" $MCR_PASSWORD $XMP_HOST
	unregister-accounting-xmp-server-from-mcr $MCR_HOST "$MCR_BIND_DN" $MCR_PASSWORD $XMP_ALT_HOST
	
	# Restore the MAS to its previous state
	restore-original-vxml-application
	# Restart the MAS
	mas-restart
}
