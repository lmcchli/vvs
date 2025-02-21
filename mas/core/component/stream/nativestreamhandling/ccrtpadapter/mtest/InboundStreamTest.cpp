#include "InboundStreamTest.h"

#include "Callback.h"

#include "MockMediaStream.h"
#include "MockMediaObject.h"
#include "MockStackEventNotifier.h"
#include "MockStreamContentInfo.h"

#include <cc++/thread.h>


#include <iostream>

using std::cout;
using std::endl;

extern int g_localPort;

void 
InboundStreamTest::setUp()
{
    static bool initialized(false);

    if (!initialized) {
       mockNativeStreamHandling.initialize();
       mockCCRTPSessionProxy.initConfiguration(&mockStreamConfiguration);
       initialized = true;
    }
}

void 
InboundStreamTest::tearDown()
{
}

void
InboundStreamTest::testDelete()
{
    MockStreamContentInfo mockStreamContentInfo;
    MockStackEventNotifier mockStackEventNotifier; 


    MockMediaStream inboundStream;

    SessionSupport* inboundSession = 
        mockCCRTPSessionProxy.createInboundSession(&inboundStream);

    inboundStream.setCallSessionId("Nisse");

    mockCCRTPSessionProxy.create(&mockStreamContentInfo, 
        &mockStackEventNotifier, 
        g_localPort, g_localPort+2,
        (long)inboundSession);

    CPPUNIT_ASSERT_EQUAL(0, inboundStream.releasePortsCounter);
    mockCCRTPSessionProxy.destroy(inboundSession, -1);
    for (int counter(0); 
        counter < 10 && inboundStream.releasePortsCounter == 0;
        counter++) {
            ost::Thread::sleep(10);
    }
    CPPUNIT_ASSERT_EQUAL(1, inboundStream.releasePortsCounter);
}
