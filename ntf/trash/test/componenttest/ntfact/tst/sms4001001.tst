test_description="Simple SMS notify test"
test_id="40010001"

function test_func {
    TEST_DOMAIN=lab.mobeon.com; export TEST_DOMAIN
    TEST_MAILHOST=`hostname`.lab.mobeon.com; export TEST_MAILHOST    
    /apps/dist/ntfact/bin/send ntfact1
    TIME=`date '+%H:'`
    DATE=`date '+%Y/%m/%d'`
    echo "s@Short message : You have a new voice message regarding \"voice message [0-9\-]*\". Message received at $TIME[0-9]*, $DATE@@" > /tmp/sedstr
    sedstr=`cat /tmp/sedstr`
    sleep 2
    txt=`/apps/dist/ntfact/bin/diff-sms-log.sh /apps/smsc/logs/NTFACT/sms.log 1 1 5551001 0 0 123456 VMN 0 0 0 "" 000001000000000R 0 "0 (false)" 0  null 107 "$sedstr"`
    case $txt in 
        "")
        echo "PASS" >> $temp_log; let passed++
        ;;
        *)
        echo "FAIL: The sms was not received correctly by smsc" >> $temp_log; let failed++
        echo "Difference:" >> $temp_log
        echo $txt >> $temp_log
    esac
}


