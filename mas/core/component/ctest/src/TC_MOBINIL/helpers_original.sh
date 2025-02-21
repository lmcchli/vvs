#
# Functions required by the PlatformAccess test suite to install and uninstall
# applications in the MAS
#

function setup() {
    # Load custom configuration for the PlatformAccess tests
	source $prefix_dir/../custom.conf
	
	# Replace parameters in the VXML application
	cp $prefix_dir/$test_id.vxml.template $prefix_dir/$test_id.vxml
	if [ -n "$A_NUMBER" ]; then
		replace ___A_NUMBER___ $A_NUMBER $prefix_dir/$test_id.vxml
			replace ___PHONE_NUMBER___ $A_NUMBER $prefix_dir/$test_id.vxml
	fi
	if [ -n "$B_NUMBER" ]; then
		replace ___B_NUMBER___ $B_NUMBER $prefix_dir/$test_id.vxml
	fi
	if [ -n "$C_NUMBER" ]; then
		replace ___C_NUMBER___ $C_NUMBER $prefix_dir/$test_id.vxml
	fi
	if [ -n "$MAIL_HOST" ]; then
		replace ___MAIL_HOST___ $MAIL_HOST $prefix_dir/$test_id.vxml
	fi
	if [ -n "$ADMIN_UID" ]; then
		replace ___ADMIN_UID___ $ADMIN_UID $prefix_dir/$test_id.vxml
	fi
	if [ -n "$COS_NAME" ]; then
		replace ___COS_NAME___ $COS_NAME $prefix_dir/$test_id.vxml
	fi
	
	# Create the configuration files from templates
	cp $prefix_dir/../default.ccxml.template $prefix_dir/../default.ccxml.$test_id
    cp $prefix_dir/../default.xml.template $prefix_dir/../default.xml.$test_id
    replace __TESTCASE__ $test_id $prefix_dir/../default.ccxml.$test_id
    replace __TESTCASE__ $test_id $prefix_dir/../default.xml.$test_id

    # Install the files
    ssh $MAS_USERNAME@$MAS_HOST "mv $MAS_PATH/applications/default.ccxml $MAS_PATH/applications/default.ccxml.PRE-$test_id"
    ssh $MAS_USERNAME@$MAS_HOST "mv $MAS_PATH/applications/default.xml $MAS_PATH/applications/default.xml.PRE-$test_id"
	scp $prefix_dir/../default.ccxml.$test_id $MAS_USERNAME@$MAS_HOST:$MAS_PATH/applications/default.ccxml > /dev/null
	scp $prefix_dir/../default.xml.$test_id $MAS_USERNAME@$MAS_HOST:$MAS_PATH/applications/default.xml > /dev/null
	scp $prefix_dir/$test_id.vxml $MAS_USERNAME@$MAS_HOST:$MAS_PATH/applications/ > /dev/null
	# Restart the MAS
	mas-restart
}

function cleanup() {
	rm -f $prefix_dir/../default.ccxml.$test_id
	rm -f $prefix_dir/../default.xml.$test_id
	rm -f $prefix_dir/$test_id.vxml
	ssh $MAS_USERNAME@$MAS_HOST "mv $MAS_PATH/applications/default.ccxml.PRE-$test_id $MAS_PATH/applications/default.ccxml"
	ssh $MAS_USERNAME@$MAS_HOST "mv $MAS_PATH/applications/default.xml.PRE-$test_id $MAS_PATH/applications/default.xml"
	ssh $MAS_USERNAME@$MAS_HOST "rm -f $MAS_PATH/applications/$test_id.vxml"
	# Restart the MAS
	mas-restart
}
