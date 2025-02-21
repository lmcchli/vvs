#include <cppunit/ui/text/TestRunner.h>

#include "recordjobtest.h"
#include "logger.h"

#include <memory>

int main( int argc, char **argv)
{
    Logger::init("stream.log.properties");
    std::auto_ptr<Logger> logger(Logger::getLogger("ccrtpadapter_test"));
    LOGGER_DEBUG(logger.get(), "Starting test run");
    CppUnit::TextUi::TestRunner runner;
    
    runner.addTest( RecordJobTest::suite() );
    
    // Run the test.
    bool wasSucessful = runner.run( "" );
    
    // Return error code 1 if the one of test failed.
    return wasSucessful ? 0 : 1;
}
