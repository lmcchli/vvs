#include "MockJavaVM.h"
#include "MockMediaObject.h"
#include "MockMediaLength.h"
#include "MockStreamConfiguration.h"
#include "MockStreamContentInfo.h"
#include "MockStackEventNotifier.h"
#include "MockMediaStream.h"
#include "MockRTPPayload.h"
#include "MockMediaProperties.h"
#include "MockMimeType.h"
#include "MockMediaObjectNativeAccess.h"
#include "MockMediaObjectIterator.h"
#include "MockByteBuffer.h"
#include "MockLengthUnit.h"

#include "TestUtil.h"

#include "MovAudioChunkContainerTest.h"
#include "MediaBufferTest.h"
#include "MediaObjectTest.h"
#include "MediaObjectReaderTest.h"
#include "MediaObjectWriterTest.h"
#include "WavBuilderTest.h"
#include "WavReaderTest.h"
#include "WavParserTest.h"
#include "MovReaderTest.h"
#include "MovParserTest.h"
#include "MovBuilderTest.h"
#include "AmrBuilderTest.h"
#include "AmrParserTest.h"
#include "ByteUtilitiesTest.h"
#include "MediaHandlerTest.h"

#include "jniutil.h"

#include "logger.h"

#include <cppunit/ui/text/TestRunner.h>
#include <iostream>

using std::cout;
using std::endl;

extern void eraseJNIUtil();

void setUpJVM();

int main(int argc, char** argv)
{
    try {
        Logger::init("stream.log.properties");
    } catch (std::exception& e) {
        std::cout << "Caught exception when initializing the logger ..." 
                  << e.what() << std::endl;
    }


    try {
        setUpJVM();
    } catch (std::exception& e) {
        std::cout << "Caught exception when initializing the JVM ..." 
                  << e.what() << std::endl;
    } catch (...) {
        std::cout << "Caught exception when initializing the JVM ..." 
                  << std::endl;
    }


    
    CppUnit::TextUi::TestRunner runner;

    runner.addTest( ByteUtilitiesTest::suite() );
    runner.addTest( MovAudioChunkContainerTest::suite() );
    runner.addTest( MediaObjectTest::suite() );
    runner.addTest( MediaObjectReaderTest::suite() );
    runner.addTest( WavReaderTest::suite() );
    runner.addTest( MovReaderTest::suite() );
    runner.addTest( WavParserTest::suite() ); 
    runner.addTest( MovParserTest::suite() );
    runner.addTest( MediaBufferTest::suite() );
    runner.addTest( MediaObjectWriterTest::suite() );
    runner.addTest( MovBuilderTest::suite() );
    runner.addTest( WavBuilderTest::suite() );
    runner.addTest( MediaHandlerTest::suite() );

//  TODO: There is a bug in the AMR 3gpp file handling ...
//	runner.addTest( AmrParserTest::suite() );
//	runner.addTest( AmrBuilderTest::suite() );

    // Run the test.
    bool wasSucessful = runner.run( "" );

    TestUtil::cleanUp();
    eraseJNIUtil();
    Logger::cleanUp();
    
    // Return error code 1 if the one of test failed.
    return wasSucessful ? 0 : 1;
}

void setUpJVM()
{
    JNIUtil::init((JavaVM*)&(MockJavaVM::instance()));
    MockJavaVM::instance().addObject("IMediaObject", new MockMediaObject());
    MockJavaVM::instance().addObject("medialibrary.MediaObject", new MockMediaObject());
    MockJavaVM::instance().addObject("IInboundMediaStream", new MockMediaStream());
    MockJavaVM::instance().addObject(new MockMediaObject());
    MockJavaVM::instance().addObject(new MockMediaLength());
    MockJavaVM::instance().addObject(new MockMediaObjectNativeAccess());
    MockJavaVM::instance().addObject(new MockMediaObjectIterator());
    MockJavaVM::instance().addObject(new MockStreamConfiguration());
    MockJavaVM::instance().addObject(new MockStreamContentInfo());
    MockJavaVM::instance().addObject(new MockStackEventNotifier());
    MockJavaVM::instance().addObject(new MockMediaStream());
    MockJavaVM::instance().addObject(new MockRTPPayload());
    MockJavaVM::instance().addObject(new MockMediaProperties());
    MockJavaVM::instance().addObject(new MockMimeType());
    MockJavaVM::instance().addObject(new MockByteBuffer());
    MockJavaVM::instance().addObject(new MockLengthUnit());
}
