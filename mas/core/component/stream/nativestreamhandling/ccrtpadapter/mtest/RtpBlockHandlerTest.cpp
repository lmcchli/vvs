#include "RtpBlockHandlerTest.h"

#include "rtpblockhandler.h"

#include "MockRtpPacket.h"

void 
RtpBlockHandlerTest::setUp()
{
}

void 
RtpBlockHandlerTest::tearDown()
{
}

void
RtpBlockHandlerTest::testInitialize()
{
    // Setting it up
    RtpBlockHandler blockHandler;

    unsigned payloadSize(160*127);
    unsigned packetSize(32);
    unsigned packetCount(127);

    // Verifying that the constructor has done its job properly
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler.getAudioPayloadBlockSize());
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler.getHeapSize());
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler.getAllocateCount());
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler.getDeallocateCount());
    CPPUNIT_ASSERT_EQUAL(true, blockHandler.isEmpty());

    // Intializing the block handler
    blockHandler.initialize(packetCount, payloadSize, 0, 0, 0, packetSize);

    // Verifying that he block handler is intialized properly
    CPPUNIT_ASSERT_EQUAL(packetCount*12+payloadSize, blockHandler.getAudioPayloadBlockSize());
    CPPUNIT_ASSERT_EQUAL(packetCount*packetSize, blockHandler.getHeapSize());
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler.getAllocateCount());
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler.getDeallocateCount());
    CPPUNIT_ASSERT_EQUAL(true, blockHandler.isEmpty());
}

    
void 
RtpBlockHandlerTest::testPayloadHandling()
{
    // Setting it up
    RtpBlockHandler blockHandler;

    char audioPayloadDataInMedia[3][5] = {
        "ABCD",
        "EFGH",
        "IJKL"
    };

     char videoPayloadDataInMedia[2][11] = {
        "MNOPQRSTYW",
        "mnopqrstyw"
    };

     char videoPayloadHeaderInMedia[2][3] = {
        "ZX",
        "zx"
    };

    unsigned rtpPacketHeader(8);
    unsigned packetSize(32);
    unsigned audioPacketCount(3);
    unsigned videoPacketCount(2);
    unsigned audioPayloadSize(5);
	unsigned tstampInc(160);
	unsigned timeDelta(20);
    unsigned videoPayloadHeaderSize(3);
    unsigned videoPayloadSize(11);
    unsigned totalAudioPayloadSize(audioPacketCount*audioPayloadSize);
    unsigned totalVideoPayloadSize(videoPacketCount*(videoPayloadHeaderSize+videoPayloadSize));
    unsigned audioRtpPacketSize(rtpPacketHeader+audioPayloadSize);
    unsigned videoRtpPacketSize(rtpPacketHeader+videoPayloadHeaderSize+videoPayloadSize);


    // Intializing the block handler
    blockHandler.initialize(audioPacketCount, totalAudioPayloadSize, 
                            videoPacketCount, totalVideoPayloadSize, 
                            rtpPacketHeader, packetSize);

    // Adding payload data
    blockHandler.addVideoPayload(17, videoPayloadHeaderInMedia[0], videoPayloadHeaderSize, 
                                     videoPayloadDataInMedia[0], videoPayloadSize);
    blockHandler.addAudioPayload(audioPayloadDataInMedia[0], audioPayloadSize, tstampInc, timeDelta);
    blockHandler.addAudioPayload(audioPayloadDataInMedia[1], audioPayloadSize, tstampInc, timeDelta);
    blockHandler.addVideoPayload(42, videoPayloadHeaderInMedia[1], videoPayloadHeaderSize, 
                                     videoPayloadDataInMedia[1], videoPayloadSize);
    blockHandler.addAudioPayload(audioPayloadDataInMedia[2], audioPayloadSize, tstampInc, timeDelta);

    // Adding payload data
    // Retreiving payload data
    unsigned size;	
    char* data;

    CPPUNIT_ASSERT_EQUAL(true, blockHandler.getNextAudioPayload(data, size, tstampInc, timeDelta));
    CPPUNIT_ASSERT_EQUAL(audioRtpPacketSize, size);
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader, audioPayloadDataInMedia[0], 4));
    CPPUNIT_ASSERT_EQUAL(true, blockHandler.getNextAudioPayload(data, size, tstampInc, timeDelta));
    CPPUNIT_ASSERT_EQUAL(audioRtpPacketSize, size);
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader, audioPayloadDataInMedia[1], 4));
    CPPUNIT_ASSERT_EQUAL(true, blockHandler.getNextAudioPayload(data, size, tstampInc, timeDelta));
    CPPUNIT_ASSERT_EQUAL(audioRtpPacketSize, size);
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader, audioPayloadDataInMedia[2], 4));
    CPPUNIT_ASSERT_EQUAL(false, blockHandler.getNextAudioPayload(data, size, tstampInc, timeDelta));
    CPPUNIT_ASSERT_EQUAL((unsigned)0, size);
    CPPUNIT_ASSERT_EQUAL((char*)0, data);

    unsigned frameTime(0);
    CPPUNIT_ASSERT_EQUAL(true, blockHandler.getNextVideoPayload(frameTime, data, size));
    CPPUNIT_ASSERT_EQUAL(unsigned(17), frameTime);
    CPPUNIT_ASSERT_EQUAL(videoRtpPacketSize, size);
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader, videoPayloadHeaderInMedia[0], 2));
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader+videoPayloadHeaderSize, videoPayloadDataInMedia[0], 10));
    CPPUNIT_ASSERT_EQUAL(true, blockHandler.getNextVideoPayload(frameTime, data, size));
    CPPUNIT_ASSERT_EQUAL(unsigned(42), frameTime);
    CPPUNIT_ASSERT_EQUAL(videoRtpPacketSize, size);
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader, videoPayloadHeaderInMedia[1], 102));
    CPPUNIT_ASSERT_EQUAL(0, strncmp(data+rtpPacketHeader+videoPayloadHeaderSize, videoPayloadDataInMedia[1], 10));
    CPPUNIT_ASSERT_EQUAL(false, blockHandler.getNextVideoPayload(frameTime, data, size));
    CPPUNIT_ASSERT_EQUAL((unsigned)0, size);
    CPPUNIT_ASSERT_EQUAL((char*)0, data);
}
    
void 
RtpBlockHandlerTest::testPacketHandling()
{
    // Setting it up
    RtpBlockHandler* blockHandler(new RtpBlockHandler);

    unsigned payloadSize(0);
    unsigned packetSize(sizeof(MockRtpPacket));
    unsigned packetCount(3);

    // Intializing the block handler
    blockHandler->initialize(packetCount, payloadSize, 0, 0, 0,  packetSize);
    blockHandler->reset();

    // Allocate some objects ...
    MockRtpPacket* rtpPkt1(new MockRtpPacket);
    CPPUNIT_ASSERT_EQUAL(false, blockHandler->isEmpty());
    CPPUNIT_ASSERT_EQUAL(unsigned(1), blockHandler->getAllocateCount());
    CPPUNIT_ASSERT_EQUAL(unsigned(0), blockHandler->getDeallocateCount());
    CPPUNIT_ASSERT_EQUAL(unsigned(1), rtpPkt1->id);
    MockRtpPacket* rtpPkt2(new MockRtpPacket);
    CPPUNIT_ASSERT_EQUAL(unsigned(2), blockHandler->getAllocateCount());
    CPPUNIT_ASSERT_EQUAL(unsigned(2), rtpPkt2->id);
    MockRtpPacket* rtpPkt3(new MockRtpPacket);
    CPPUNIT_ASSERT_EQUAL(unsigned(3), blockHandler->getAllocateCount());
    CPPUNIT_ASSERT_EQUAL(unsigned(3), rtpPkt3->id);

    // Allocating the objects
    delete rtpPkt1;
    CPPUNIT_ASSERT(RtpBlockHandler::getSingleton() != 0);
    // If the intent is to check the 'heap' byte, then this is how it should look
    CPPUNIT_ASSERT_EQUAL((char)0, *((char*)rtpPkt1-4));
    CPPUNIT_ASSERT_EQUAL(unsigned(2), rtpPkt2->id);
    CPPUNIT_ASSERT_EQUAL(unsigned(3), rtpPkt3->id);
    CPPUNIT_ASSERT_EQUAL(false, blockHandler->isEmpty());
    CPPUNIT_ASSERT_EQUAL(unsigned(3), blockHandler->getAllocateCount());
    delete rtpPkt2;
    CPPUNIT_ASSERT(RtpBlockHandler::getSingleton() != 0);
    CPPUNIT_ASSERT_EQUAL(unsigned(3), rtpPkt3->id); 
    CPPUNIT_ASSERT_EQUAL(false, blockHandler->isEmpty());
    delete rtpPkt3;
    CPPUNIT_ASSERT(RtpBlockHandler::getSingleton() == 0);
    blockHandler->release();
}

void 
RtpBlockHandlerTest::testNativeAllocation()
{
    //    RtpBlockHandler::release();
    MockRtpPacket* rtpPkt1(new MockRtpPacket);

    delete rtpPkt1;
}

