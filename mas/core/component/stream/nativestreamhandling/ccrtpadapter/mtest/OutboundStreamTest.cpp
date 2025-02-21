#include "OutboundStreamTest.h"

#include "Callback.h"

#include "MockMediaStream.h"
#include "MockMediaObject.h"
#include "MockStackEventNotifier.h"
#include "MockStreamContentInfo.h"


#include <iostream>

using std::cout;
using std::endl;

extern int g_localPort;

void 
OutboundStreamTest::setUp()
{
    static bool initialized(false);

    if (!initialized) {
       mockNativeStreamHandling.initialize();
       mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);
       initialized = true;
    }
}

void 
OutboundStreamTest::tearDown()
{
}


void
OutboundStreamTest::testCreate()
{
}

void
OutboundStreamTest::testPlayFinished()
{
    const base::String address("localhost");
    int audioPort(g_localPort);
    int videoPort(audioPort+2);

    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 
    MockMediaStream outboundStream;

    SessionSupport* outboundSession = 
        mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

    outboundStream.setCallSessionId("Nisse");

    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        address.c_str(), audioPort, 
        address.c_str(), videoPort, 
        2000, (long)outboundSession, 0);

    MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
    MockObject mockObject("Object");

    mockCCRTPSessionProxy.play(&mockObject, 42, &mockMediaObject, 0, -1, outboundSession);

    Callback* cb(mockNativeStreamHandling.getCallback(0));
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(42L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::PLAY_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK, cb->status);
    delete cb;
}

void
OutboundStreamTest::testPlayStop()
{
    const base::String address("localhost");
    int audioPort(g_localPort);
    int videoPort(audioPort+2);

    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 


    MockMediaStream outboundStream;

    SessionSupport* outboundSession = 
        mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

    outboundStream.setCallSessionId("Nisse");

    cout << "Intializing outbound RTP session ..." << endl;
    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        address.c_str(), audioPort, 
        address.c_str(), videoPort, 
        2000, (long)outboundSession, 0);

    MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
    MockObject mockObject("Object");

    mockCCRTPSessionProxy.play(&mockObject, 43, &mockMediaObject, 0, -1, outboundSession);
    mockCCRTPSessionProxy.stop(&mockObject, outboundSession);

    Callback* cb(mockNativeStreamHandling.getCallback(0));
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(43L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::PLAY_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK_STOPPED, cb->status);
    delete cb;
}

void
OutboundStreamTest::testPlayCancel()
{
    const base::String address("localhost");
    int audioPort(g_localPort);
    int videoPort(audioPort+2);

    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 


    MockMediaStream outboundStream;

    SessionSupport* outboundSession = 
        mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

    outboundStream.setCallSessionId("Nisse");

    cout << "Intializing outbound RTP session ..." << endl;
    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        address.c_str(), audioPort, 
        address.c_str(), videoPort, 
        2000, (long)outboundSession, 0);

    MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
    MockObject mockObject("Object");

    mockCCRTPSessionProxy.play(&mockObject, 44, &mockMediaObject, 0, -1, outboundSession);
    mockCCRTPSessionProxy.cancel(&mockObject, outboundSession);

    Callback* cb(mockNativeStreamHandling.getCallback(0));
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(44L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::PLAY_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK_STOPPED, cb->status);
    delete cb;
}

void
OutboundStreamTest::testPlayDeleteWithCallback()
{
    const base::String address("localhost");
    int audioPort(g_localPort);
    int videoPort(audioPort+2);

    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 


    MockMediaStream outboundStream;

    SessionSupport* outboundSession = 
        mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

    outboundStream.setCallSessionId("Nisse");

    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        address.c_str(), audioPort, 
        address.c_str(), videoPort, 
        2000, (long)outboundSession, 0);

    MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
    MockObject mockObject("Object");

    mockCCRTPSessionProxy.play(&mockObject, 45, &mockMediaObject, 0, -1, outboundSession);
    mockCCRTPSessionProxy.destroy(outboundSession, 46);

    Callback* cb(mockNativeStreamHandling.getCallback(0));
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(45L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::PLAY_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK_STOPPED, cb->status);
    delete cb;
    cb = mockNativeStreamHandling.getCallback(0);
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(46L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::DELETE_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK, cb->status);
    delete cb;
}

void
OutboundStreamTest::testPlayDeleteWithoutCallback()
{
    const base::String address("localhost");
    int audioPort(g_localPort);
    int videoPort(audioPort+2);

    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 


    MockMediaStream outboundStream;

    SessionSupport* outboundSession = 
        mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

    outboundStream.setCallSessionId("Nisse");

    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        address.c_str(), audioPort, 
        address.c_str(), videoPort, 
        2000, (long)outboundSession, 0);

    MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
    MockObject mockObject("Object");

    mockCCRTPSessionProxy.play(&mockObject, 45, &mockMediaObject, 0, -1, outboundSession);
    mockCCRTPSessionProxy.destroy(outboundSession, 46);

    Callback* cb(mockNativeStreamHandling.getCallback(0));
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(45L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::PLAY_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK_STOPPED, cb->status);
    delete cb;
    cb = mockNativeStreamHandling.getCallback(0);
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT_EQUAL(46L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::DELETE_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK, cb->status);
    delete cb;
}

void
OutboundStreamTest::testPlayFailed()
{
}

void
OutboundStreamTest::testDelete()
{
    const base::String address("localhost");
    int audioPort(g_localPort);
    int videoPort(audioPort+2);

    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 


    MockMediaStream outboundStream;

    SessionSupport* outboundSession = 
        mockCCRTPSessionProxy.createOutboundSession(&outboundStream);

    outboundStream.setCallSessionId("Nisse");

    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        address.c_str(), audioPort, 
        address.c_str(), videoPort, 
        2000, (long)outboundSession, 0);

    MockMediaObject mockMediaObject(".", "tada", "wav", "audio/wav", 500);
    MockObject mockObject("Object");

    mockCCRTPSessionProxy.destroy(outboundSession, 47);

    Callback* cb(mockNativeStreamHandling.getCallback(0));
    CPPUNIT_ASSERT(cb != 0);
    CPPUNIT_ASSERT(-1L != cb->requestId); // Stray callback!
    CPPUNIT_ASSERT_EQUAL(47L, cb->requestId);
    CPPUNIT_ASSERT_EQUAL(Callback::DELETE_COMMAND, cb->command);
    CPPUNIT_ASSERT_EQUAL(Callback::OK, cb->status);
    delete cb;
}
