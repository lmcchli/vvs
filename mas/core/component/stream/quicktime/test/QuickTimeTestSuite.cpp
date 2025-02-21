/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <cppunit/ui/text/TestRunner.h>
#include "QuickTimeTest.h"

int main( int argc, char **argv)
{
    //    Logger::init();
    CppUnit::TextUi::TestRunner runner;

    runner.addTest( QuickTimeTest::suite() );

    // Run the test.
    bool wasSucessful = runner.run( "" );
    
    // Return error code 1 if the one of test failed.
    return wasSucessful ? 0 : 1;
}
