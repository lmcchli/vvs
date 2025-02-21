/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <movparsertest.h>
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>


#include <ostream>
#include <unistd.h>
#include <algorithm>
#include <fstream>
#include <time.h>
#include <sstream>
#include <string>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "testjniutil.h"
#include "jniutil.h"
#include "testutil.h"
#include "byteutilities.h"
#include "logger.h"
#include "medialibraryexception.h"
#include "movreader.h"
#include "movrtppacket.h"

using namespace std;
using namespace CppUnit;
//using namespace Asserter;
 
const char* MovParserTest::FILENAME =         "test.mov";
const int   MovParserTest::FILE_SIZE =        62330;
const char* MovParserTest::ILLEGAL_FILENAME = "illegalfile.mov";    
const char* MovParserTest::FILENAME2 = "gillty.mov";   
const char* MovParserTest::FILENAME3 = "beep.mov";  
const jlong MovParserTest::BUFFER_SIZE = 512;

MovParserTest::MovParserTest():
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
    pmMovParser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", pmMovParser != NULL);
}

void 
MovParserTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
    delete pmMovParser; 
}

void 
MovParserTest::testConstructor() {
    //cout << "MovParserTest::testConstructor" << endl;
    MovParser* parser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", parser != NULL);
    
    delete parser;
    /*     
    try {
        parser = createParser(pmEnv, ILLEGAL_FILENAME, BUFFER_SIZE);
        CPPUNIT_FAIL("Constructor should throw exception if illegal file");
    } catch (MediaLibraryException& e) {}
    */
}
void 
MovParserTest::testParse() {
    // Create parser object
    //    MovParser* parser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    MovParser* parser = pmMovParser;
    // Check validity
    CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check() == false);
    MovInfo& info((MovInfo&)parser->getMediaInfo());
    CPPUNIT_ASSERT_MESSAGE("Hint track", info.getHintTrack() == NULL);
    CPPUNIT_ASSERT_MESSAGE("Video track", info.getVideoTrack() == NULL);
    // Parse the MOV file/object
    parser->init();
    parser->parse();
    // Check validity
    CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check());
    parser->getMediaInfo();
    CPPUNIT_ASSERT_MESSAGE("No hint track", info.getHintTrack() != NULL);
    CPPUNIT_ASSERT_MESSAGE("No video track", info.getVideoTrack() != NULL);
    string codecName(parser->getAudioCodec());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("No pcmu!", codecName, string("PCMU"));
    codecName = parser->getVideoCodec();
    CPPUNIT_ASSERT_EQUAL_MESSAGE("No pcmu!", codecName, string("H263"));
}

void
MovParserTest::testMultipleParse()
{
    for (int i(0); i < 100; i++) {
        // Create parser object
        MovParser* parser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
        // Check validity
        CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check() == false);
        MovInfo& info((MovInfo&)parser->getMediaInfo());
        CPPUNIT_ASSERT_MESSAGE("Hint track", info.getHintTrack() == NULL);
        CPPUNIT_ASSERT_MESSAGE("Video track", info.getVideoTrack() == NULL);
        // Parse the MOV file/object
        parser->init();
        parser->parse();
        // Check validity
        CPPUNIT_ASSERT_MESSAGE("Parse error ...", parser->check());
        parser->getMediaInfo();
        CPPUNIT_ASSERT_MESSAGE("No hint track", info.getHintTrack() != NULL);
        CPPUNIT_ASSERT_MESSAGE("No video track", info.getVideoTrack() != NULL);
        string codecName(parser->getAudioCodec());
        CPPUNIT_ASSERT_EQUAL_MESSAGE("No pcmu!", codecName, string("PCMU"));
        codecName = parser->getVideoCodec();
        CPPUNIT_ASSERT_EQUAL_MESSAGE("No pcmu!", codecName, string("H263"));
        delete parser;
    }
}

void 
MovParserTest::testGetVideoFrames()
{
    // Create parser object
    //    MovParser* parser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    MovParser* parser = pmMovParser;
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
				 (unsigned)rtpPackets.size());
    MovRtpPacket& rtpPacket(*rtpPackets.begin());
    CPPUNIT_ASSERT_MESSAGE("No length", rtpPacket.getLength() > 0);
    printRtpPacket(&rtpPacket);
    //    delete rtpPacket;
    rtpPackets.clear();
    for (int j(0); j < info.getFrameCount(); j++) {
	parser->getFrame(rtpPackets, j);
	//	printf("Frame #%d\n", j);
	for (boost::ptr_list<MovRtpPacket>::iterator iter = rtpPackets.begin();
              iter != rtpPackets.end(); ++iter) {
	    MovRtpPacket& packet(*iter);
	    //	    printf("Frame time: %d\n", packet->getFrameTime());
	    //	    printRtpPacket(packet);
            //	    delete packet;
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
    // Create parser object
    //    MovParser* parser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    MovParser* parser = pmMovParser;
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
    int fd = open("test.au", O_RDWR | O_CREAT, 00644);
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

MovParser* 
MovParserTest::createParser(JNIEnv* env, const char* fileName, 
			    jlong bufferSize) {
    string contentType("video/mov");
    string fileExtension("mov");
    java::MediaObject* mediaObject = 
	TestUtil::createReadOnlyCCMediaObject(env, fileName, bufferSize,
        contentType, fileExtension); 
    MovParser* pMovParser = new MovParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a MovParser instance",
        pMovParser != NULL);
    //    pMovParser->init();
    return pMovParser;
}

