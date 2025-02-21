test_description="SMS notify MER event test"
test_id="40010003"

function test_func {
    TEST_DOMAIN=lab.mobeon.com; export TEST_DOMAIN
    TEST_MAILHOST=`hostname`.lab.mobeon.com; export TEST_MAILHOST    
    /apps/dist/ntfact/bin/send ntfact1
    sleep 2
    DATE=`date '+%Y%m%d%H'`
    file=`ls /apps/mer/rep/C$DATE*`
    case $txt in 
        "")
        echo "FAIL: The MER event was not generated starting with /aps/mer/rep/C$DATE" >> $temp_log; let failed++
        ;;
        *)
        echo "PASS" >> $temp_log; let passed++
        ;;
    esac
}
