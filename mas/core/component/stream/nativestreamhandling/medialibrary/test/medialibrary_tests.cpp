/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <cppunit/ui/text/TestRunner.h>
#include "movaudiochunkcontainertest.h"
#include "mediabuffertest.h"
#include "mediaobjecttest.h"
#include "mediaobjectreadertest.h"
#include "mediaobjectwritertest.h"
#include "wavreadertest.h"
#include "wavparsertest.h"
#include "movreadertest.h"
#include "movparsertest.h"
#include "movbuildertest.h"
#include "byteutilitiestest.h"
#include "logger.h"

int main( int argc, char **argv)
{
    Logger::init("stream.log.properties");
    CppUnit::TextUi::TestRunner runner;

    runner.addTest( MovAudioChunkContainerTest::suite() );

#ifndef PURIFY
    runner.addTest( MovBuilderTest::suite() );
    runner.addTest( MovReaderTest::suite() );
    runner.addTest( MovParserTest::suite() );
    runner.addTest( ByteUtilitiesTest::suite() );
    runner.addTest( MediaBufferTest::suite() );
    runner.addTest( MediaObjectTest::suite() );
    runner.addTest( MediaObjectReaderTest::suite() );
    runner.addTest( MediaObjectWriterTest::suite() );
    runner.addTest( WavReaderTest::suite() );
    runner.addTest( WavParserTest::suite() ); 
#endif

    // Run the test.
    bool wasSucessful = runner.run( "" );
    
    // Return error code 1 if the one of test failed.
    return wasSucessful ? 0 : 1;
}
