/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <MovParserTest.h>

#include "TestJNIUtil.h"
#include "MovFile.h"
#include "MockMediaObject.h"

#include "jniutil.h"
#include "byteutilities.h"
#include "logger.h"
#include "medialibraryexception.h"

#include "movparser.h"
#include "movreader.h"
#include "movrtppacket.h"
#include "java/mediaobject.h"
#include "mediaobject.h"
#include "mediahandler.h"
#include "rtpblockhandler.h"
#include "MediaValidator.h"

#include <ccrtp/outgoingrtppkt.h>
#include <ccrtp/outgoingrtppktlink.h>

#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>


#include <ostream>
#if defined(WIN32)
//#include <windows.h>
#include <io.h>
#else
#define O_BINARY 0
#include <unistd.h>
#endif
#include <algorithm>
#include <fstream>
#include <time.h>
#include <sstream>
#include <base_include.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <TestMedia.h>

using namespace std;
using namespace CppUnit;
 
static struct MediaData LOCAL_MEDIA_BAD_MOOV = MEDIA_TEST_PCMU_MOV.as("badMoov");
static struct MediaData LOCAL_MEDIA_BAD_MDAT = MEDIA_TEST_PCMU_MOV.as("badMdat");
static struct MediaData LOCAL_MEDIA_BAD_MOOV_HEADER = MEDIA_TEST_PCMU_MOV.as("badMoovHeader");
static struct MediaData LOCAL_MEDIA_BAD_MDAT_HEADER = MEDIA_TEST_PCMU_MOV.as("badMdatHeader");

static const jlong BUFFER_SIZE = 512;

MovParserTest::MovParserTest():
	mMovParser(0),
	pmMockMediaObject(0),
	pmMediaObject(0),
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")) {    
}

MovParserTest::~MovParserTest() {

}

void 
MovParserTest::setUp() {
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
}

void 
MovParserTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
	if(mMovParser != 0) {
		delete mMovParser; 
		mMovParser = 0;
	}
	if(pmMediaObject != 0) {
		delete pmMediaObject;
		pmMediaObject = 0;
	}
	if(pmMockMediaObject != 0) {
		delete pmMockMediaObject;
		pmMockMediaObject = 0;
	}
}

void MovParserTest::createParser(struct MediaData md) {
	pmMockMediaObject = md.createMock(BUFFER_SIZE);
    pmMediaObject = new java::MediaObject(pmEnv, (jobject)pmMockMediaObject);
    mMovParser = new MovParser(pmMediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", mMovParser != NULL);
}


void 
MovParserTest::testConstructor() {
    createParser(MEDIA_TEST_PCMU_MOV);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", mMovParser != NULL);
    
}

void 
MovParserTest::testParse() {
    createParser(MEDIA_TEST_PCMU_MOV);

    // Create parser object
    MovParser* parser = mMovParser;
    // Check validity
    CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check() == false);
    MovInfo& info((MovInfo&)parser->getMediaInfo());
    CPPUNIT_ASSERT_MESSAGE("Hint track", info.getHintTrack()->check() == false);
    CPPUNIT_ASSERT_MESSAGE("Video track", info.getVideoTrack()->check() == false);
    // Parse the MOV file/object
    parser->init();
    parser->parse();
    // Check validity
    CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check());
    parser->getMediaInfo();
    CPPUNIT_ASSERT_MESSAGE("No hint track", info.getHintTrack() != NULL);
    CPPUNIT_ASSERT_MESSAGE("No video track", info.getVideoTrack() != NULL);
    CPPUNIT_ASSERT_MESSAGE("Bad hint track", info.getHintTrack()->check());
    CPPUNIT_ASSERT_MESSAGE("Bad video track", info.getVideoTrack()->check());
    base::String codecName;
	MEDIA_TEST_PCMU_MOV.validate(*parser);
}

void
MovParserTest::testMultipleParse()
{
    for (int i(0); i < 100; i++) {
        // Create parser object
        MockMediaObject* mockMediaObject = MEDIA_TEST_PCMU_MOV.createMock(BUFFER_SIZE);
        java::MediaObject* mediaObject = new java::MediaObject(pmEnv, (jobject)mockMediaObject);
        MovParser* parser = new MovParser(mediaObject);
        CPPUNIT_ASSERT_MESSAGE("Failed to create a MovParser instance", parser != NULL);

        // Check validity
        CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check() == false);
        MovInfo& info((MovInfo&)parser->getMediaInfo());
        CPPUNIT_ASSERT_MESSAGE("Hint track", info.getHintTrack()->check() == false);
        CPPUNIT_ASSERT_MESSAGE("Video track", info.getVideoTrack()->check() == false);

        // Parse the MOV file/object
        parser->init();
        parser->parse();

        // Check validity
        CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check());
        parser->getMediaInfo();
        CPPUNIT_ASSERT_MESSAGE("No hint track", info.getHintTrack() != NULL);
        CPPUNIT_ASSERT_MESSAGE("No video track", info.getVideoTrack() != NULL);
        CPPUNIT_ASSERT_MESSAGE("Bad hint track", info.getHintTrack()->check());
        CPPUNIT_ASSERT_MESSAGE("Bad video track", info.getVideoTrack()->check());
        base::String codecName;
        codecName = parser->getAudioCodec();
        CPPUNIT_ASSERT_EQUAL_MESSAGE("No pcmu!", codecName, base::String("PCMU"));
        codecName = parser->getVideoCodec();
        CPPUNIT_ASSERT_EQUAL_MESSAGE("No H.263!", codecName, base::String("H263"));

        delete parser;
        delete mediaObject;
        delete mockMediaObject;
    }
}

void 
MovParserTest::testGetVideoFrames()
{
    createParser(MEDIA_TEST_PCMU_MOV);
    // Create parser object
    MovParser* parser = mMovParser;
    // Parse the MOV file/object
    parser->init();
    parser->parse();
    // Check validity
    CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check() == true);
    MovInfo& info((MovInfo&)parser->getMediaInfo());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of frames", 
				 19, 
				 info.getFrameCount());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of audio chunks", 
				 300, 
				 info.getAudioChunkCount());
    //    MovRtpPacketContainer rtpPackets;
    boost::ptr_list<MovRtpPacket> rtpPackets;
    parser->getFrame(rtpPackets, 3);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong packet count", 
				 (unsigned)1, 
				 rtpPackets.size());
    MovRtpPacket& rtpPacket(*rtpPackets.begin());
    CPPUNIT_ASSERT_MESSAGE("No length", rtpPacket.getLength() > 0);
    rtpPackets.clear();
    for (int j(0); j < info.getFrameCount(); j++) {
	parser->getFrame(rtpPackets, j);
	for (boost::ptr_list<MovRtpPacket>::iterator iter = rtpPackets.begin();
              iter != rtpPackets.end(); ++iter) {
	    MovRtpPacket& packet(*iter);
	}
	rtpPackets.clear();
    }
    unsigned chunkSize(0);
    for (int i(0); i < info.getAudioChunkCount(); i++) {
	const unsigned char* buf(parser->getAudioChunk(chunkSize, i));
	CPPUNIT_ASSERT_MESSAGE("Null buffer", buf != 0);
	CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong chunk size", 160, (int)chunkSize);
    }
}

void 
MovParserTest::testGetAudio()
{
    createParser(MEDIA_TEST_PCMU_MOV);
    // Create parser object
    MovParser* parser = mMovParser;
    // Parse the MOV file/object
    parser->init();
    parser->parse();
    // Check validity
    CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check() == true);
    MovInfo& info((MovInfo&)parser->getMediaInfo());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of audio chunks", 
				 300, 
				 info.getAudioChunkCount());
    unsigned chunkSize(0);
    int fd = open("test.au", O_RDWR | O_CREAT | O_BINARY, 00644);
    if (fd <= 0) printf("\nError !!!\n");
    for (int i(0); i < info.getAudioChunkCount(); i++) {
	const unsigned char* buf(parser->getAudioChunk(chunkSize, i));
	CPPUNIT_ASSERT_MESSAGE("Null buffer", buf != 0);
	CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong chunk size", 160, (int)chunkSize);
	write(fd, buf, chunkSize);
    }
    close(fd);
}

void
MovParserTest::testParseError()
{
    bool gotException(false);
   long hugeAtomLength(1234567890);


    // Copy reference file
	MovFile referenceFile(MEDIA_TEST_PCMU_MOV.getCanonicalFilename().c_str());
	referenceFile.copyTo(LOCAL_MEDIA_BAD_MDAT.getCanonicalFilename().c_str());
	referenceFile.copyTo(LOCAL_MEDIA_BAD_MOOV.getCanonicalFilename().c_str());
	referenceFile.copyTo(LOCAL_MEDIA_BAD_MDAT_HEADER.getCanonicalFilename().c_str());
	referenceFile.copyTo(LOCAL_MEDIA_BAD_MOOV_HEADER.getCanonicalFilename().c_str());

    // Corrupt files
    // Corrupt the MDAT atom
    MovFile badMdatFile(LOCAL_MEDIA_BAD_MDAT.getCanonicalFilename().c_str());
    badMdatFile.open(MovFile::OPEN_AS_IO);
    CPPUNIT_ASSERT_MESSAGE("No MDAT atom!", badMdatFile.find(quicktime::MDAT));
    badMdatFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    badMdatFile.writeDW(quicktime::BAJS);
    badMdatFile.open(MovFile::OPEN_AS_INPUT);
    CPPUNIT_ASSERT_MESSAGE("No BAJS!", badMdatFile.find(quicktime::BAJS));
    badMdatFile.close();

    // Corrupt the MDAT atom header
	MovFile badMdatHeaderFile(LOCAL_MEDIA_BAD_MDAT_HEADER.getCanonicalFilename().c_str());
    badMdatHeaderFile.open(MovFile::OPEN_AS_IO);
    CPPUNIT_ASSERT_MESSAGE("No MDAT atom!", badMdatHeaderFile.find(quicktime::MDAT));
    badMdatHeaderFile.seek(8, quicktime::AtomReader::SEEK_BACKWARD);
    badMdatHeaderFile.writeDW(hugeAtomLength);
    badMdatHeaderFile.open(MovFile::OPEN_AS_INPUT);
    badMdatHeaderFile.close();

    // Corrupt the MOOV atom
	MovFile badMoovFile(LOCAL_MEDIA_BAD_MOOV.getCanonicalFilename().c_str());
    badMoovFile.open(MovFile::OPEN_AS_IO);
    CPPUNIT_ASSERT_MESSAGE("No MOOV atom!", badMoovFile.find(quicktime::MOOV));
    badMoovFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    badMoovFile.writeDW(quicktime::BAJS);
    badMoovFile.open(MovFile::OPEN_AS_INPUT);
    badMoovFile.close();

    // Corrupt the MOOV atom header
	MovFile badMoovHeaderFile(LOCAL_MEDIA_BAD_MOOV_HEADER.getCanonicalFilename().c_str());
    badMoovHeaderFile.open(MovFile::OPEN_AS_IO);
    CPPUNIT_ASSERT_MESSAGE("No MOOV atom!", badMoovHeaderFile.find(quicktime::MOOV));
    badMoovHeaderFile.seek(8, quicktime::AtomReader::SEEK_BACKWARD);
    badMoovHeaderFile.writeDW(hugeAtomLength);
    badMoovHeaderFile.open(MovFile::OPEN_AS_INPUT);
    badMdatHeaderFile.close();

    // Parse files
    // Expecting parse failure due to faulty MDAT atom
    {
        // Creating a MOV parser for the resulting MOV file
		java::MediaObject mediaObject(pmEnv,(jobject)LOCAL_MEDIA_BAD_MDAT.createMock(512));
        MovParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }
    if (false) {
        // Creating a MOV parser for the resulting MOV file
		java::MediaObject mediaObject(pmEnv, (jobject)LOCAL_MEDIA_BAD_MDAT_HEADER.createMock(512));
        MovParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }
    

    // Expecting parse failure due to faulty MOOV atom
    {
        // Creating a MOV parser for the resulting MOV file
		java::MediaObject mediaObject(pmEnv, (jobject)LOCAL_MEDIA_BAD_MOOV.createMock(512));
        MovParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }
    if (false) {
        // Creating a MOV parser for the resulting MOV file
		java::MediaObject mediaObject(pmEnv, (jobject)LOCAL_MEDIA_BAD_MOOV_HEADER.createMock(512));
        MovParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }

    // Expecting parse failure due to faulty MOOV atom
    {
        // Creating a MOV parser for the resulting MOV file
		java::MediaObject mediaObject(pmEnv, (jobject)MEDIA_CORRUPT_MOV.createMock(512));
        MovParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }
}

void
MovParserTest::testBigVideoFrames()
{
 /*  
  * This test is also disabled in MovParserTest.h, so i commeted it here also.

	unsigned rtpPacketSize(sizeof(ost::OutgoingRTPPkt) + sizeof(ost::OutgoingRTPPktLink));
    unsigned rtpFixedHeaderSize(sizeof(ost::RTPPacket::RTPFixedHeader));
    MockMediaObject mockMediaObject(".", "linton", fileExtension, contentType, BUFFER_SIZE);
    java::MediaObject javaMediaObject(pmEnv, (jobject)&mockMediaObject);
    MediaHandler mediaHandler(javaMediaObject, 40, 40, rtpFixedHeaderSize, 1500, rtpPacketSize);
	mediaHandler.parse(boost::ptr_list<MediaValidator>(), 0);
    RtpBlockHandler& blockHandler(mediaHandler.getMediaObject()->getBlockHandler());

    unsigned mediaFrameTime;
    char* payload;
    unsigned length;
    while (blockHandler.getNextVideoPayload(mediaFrameTime, payload, length)) {
        if (length > 2000) printf("Frame size: %d\n", length);
    }
*/
}


void
MovParserTest::printRtpPacket(MovRtpPacket* packet)
{
	unsigned short header(packet->getHeaderInfo());
	unsigned short sequenceNumber(packet->getSequenceNumber());
	printf("RTP Packet #%d\n", sequenceNumber);
	printf("Header:          ");
	for (int i(0); i < 16; i++) {
		if (i % 4 == 0) printf("|");
		printf("%c", (0x8000>>i)&header ? '1' : '0');
	}
	for (int i(0); i < 16; i++) {
		if (i % 4 == 0) printf("|");
		printf("%c", (0x8000>>i)&sequenceNumber ? '1' : '0');
	}
	printf("\n");
	printf("Payload header: ");
	unsigned head;
	memcpy(&head, packet->getData(), sizeof(unsigned));
	for (int i(0); i < 32; i++) {
		if (i % 4 == 0) printf("|");
		printf("%c", (0x80000000>>i)&head ? '1' : '0');
	}
	printf("\n");
	printf("Payload:\n");
	for (int row(0); row < 2; row++) {
		for (int block(0); block < 8; block++) {
			printf("%02x ", (unsigned char)packet->getData()[row*16 + block]);
		}
		printf(" ");
		for (int block(8); block < 16; block++) {
			printf("%02x ", (unsigned char)packet->getData()[row*16 + block]);
		}
		printf("\n");
	}

}


void 
MovParserTest::validateMovInfo(const MovInfo& movInfo) const
{
}

