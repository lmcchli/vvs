# Description and testcase number
test_description="Verify that MAS will send the 183 message as a reliable response if early media is used by the service application and the invite includes a supported header with 100rel and sendprovisionalresponsesreliable is set to \"no\""
test_id="21"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	
	echo "SEQUENTIAL" > ./numbers_$$.csv
	echo "$calling;$called" >> ./numbers_$$.csv
	
	# Change the MAS component configuration as needed
	cp $prefix_dir/ComponentConfig.xml $prefix_dir/ComponentConfig.xml.$test_id.tmp
	replace ___MAS_HOST___ $MAS_HOST $prefix_dir/ComponentConfig.xml.$test_id.tmp
	# Install the required configuration files
	install-mas-configuration $prefix_dir/mas.xml.sprr_no
	install-mas-component-configuration $prefix_dir/ComponentConfig.xml.$test_id.tmp
	# Install the early media files
	scp -r $prefix_dir/earlymedia_1*.xml $MAS_USERNAME@$MAS_HOST:$MAS_PATH/applications/vva0001.1/ > /dev/null
	# Remove the temporary ComponentConfig.xml file
	rm -f $prefix_dir/ComponentConfig.xml.$test_id.tmp
	# Restart the MAS
	mas-restart

	# Start a call trace
	start_call_trace trace.pcap "host $MAS_HOST and port 5060"

	# Make the call (can't figure out why the regular make-sipp-call 
	# function doesn't play nice with dumpcap)
	./sipp -sf $prefix_dir/$base_filename.xml -inf ./numbers_$$.csv -recv_timeout 10000 -m 1 -r 1 -rp 1000 -l 1 $MAS_HOST > /dev/null 2>&1
	if [ $? -ne 0 ]; then return 1; fi
	rm -f ./numbers_$$.csv

	# Stop the trace 
	stop_call_trace 
	# Fix the resulting trace dump file
	format_call_trace trace.pcap

	# Verify the results by examining the call trace	
	echo -n "Checking call trace for: \"Require: 100rel\"... " >> $temp_log
	grep -a 183 trace.pcap | grep "Require: 100rel" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
	else
		echo "FAIL" >> $temp_log; let failed++
	fi

	# Verify the results by examining the MAS log
	get-mas-log-and-filter "com.mobeon.masp.callmanager.sip.SipMessageSenderImpl"
	extract-session-id-from-mas-log
	echo -n "Checking MAS log for: \"...SIP 183 response is sent reliably...\"... " >> $temp_log
	cat ./${base_filename}$$.tmp | \
		grep "$mas_session" | \
		grep "SIP 183 response is sent reliably" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
	else
		echo "FAIL" >> $temp_log; let failed++
	fi

	# Remove the call trace file
	rm -f trace.pcap
	# Restore the original MAS state
	restore-original-mas-configuration
	restore-original-mas-component-configuration
	# Restart the MAS
	mas-restart
}
