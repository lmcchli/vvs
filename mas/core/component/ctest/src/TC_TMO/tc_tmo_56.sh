# Description and testcase number
test_description="Verify that MAS will use the default service if service id in request URI does not match 1234 or abcd"
test_id="56"

# The test function
function test_func {
	calling="302102007"
	called="302102008"
	make-sipp-call $prefix_dir/$base_filename.xml $calling $called
	if [ $? -ne 0 ]; then return 1; fi
}
