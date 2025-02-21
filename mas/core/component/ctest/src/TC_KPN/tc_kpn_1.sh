# Description and testcase number
test_description="Verify that the default configuration value for sendprovisionalresponsesreliable is set in Callmanager entry in mas.xml during installation"
test_id="1"

# The test function
function test_func {
	ssh $MAS_USERNAME@$MAS_HOST "grep 'sendprovisionalresponsesreliable=\"sdponly\"' $MAS_PATH/cfg/mas.xml" > /dev/null
	if [ $? -eq 0 ]; then
		echo "PASS" >> $temp_log; let passed++
    else
		echo "FAIL" >> $temp_log; let failed++
    fi
}
