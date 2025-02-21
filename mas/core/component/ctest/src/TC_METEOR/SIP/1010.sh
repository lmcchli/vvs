test_description="SIP - Silence Detection - Verify that parameters exist in mas.xml"
test_id="1010"
function test_func {
 
	# Load helper functions
	load_config $prefix_dir/../helpers.sh
	
	# Get the mas.xml and verify parameters
	ssh $MAS_USERNAME@$MAS_HOST "cat $MAS_PATH/cfg/mas.xml" > ./${base_filename}$$.tmp
	cat ./${base_filename}$$.tmp | grep silencedetectionmode= | grep silencethreshold= | grep initialsilenceframes= | grep signaldeadband= | grep silencedeadband= | grep detectionframes= | grep silencedetectiondebuglevel= > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
	else
		echo "FAIL: Silence Detection parameters in mas.mxl seems to be missing." >> $temp_log; let failed++
	fi


		
}