/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <AmrParserTest.h>

#include "TestJNIUtil.h"
#include "MovFile.h"
#include "MockMediaObject.h"

#include "jniutil.h"
#include "byteutilities.h"
#include "logger.h"
#include "medialibraryexception.h"

#include "java/mediaobject.h"
#include "amrparser.h"
#include "movreader.h"
#include "movrtppacket.h"


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
 
const jlong AmrParserTest::BUFFER_SIZE = 512;


AmrParserTest::AmrParserTest():
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")) {    
}

AmrParserTest::~AmrParserTest() {

}

void 
AmrParserTest::setUp() {
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 

	struct MediaData md = MEDIA_TEST_3GP;
	pmMockMediaObject = md.createMock(BUFFER_SIZE);
    pmMediaObject = new java::MediaObject(pmEnv, (jobject)pmMockMediaObject);
    pmAmrParser = new AmrParser(pmMediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create AmrParser", pmAmrParser != NULL);
}

void 
AmrParserTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }

    delete pmAmrParser; 
    pmAmrParser = NULL;
    delete pmMediaObject;
    pmMediaObject = NULL;
    delete pmMockMediaObject;
    pmMockMediaObject = NULL;
}

void 
AmrParserTest::testConstructor() {
	struct MediaData md = MEDIA_TEST_3GP;
	MockMediaObject * mockMediaObject = md.createMock(BUFFER_SIZE);
    java::MediaObject* mediaObject = new java::MediaObject(pmEnv, (jobject)mockMediaObject);
    AmrParser* movParser = new AmrParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create AmrParser", movParser != NULL);
    
    delete movParser;
    delete mediaObject;
    delete mockMediaObject;
}

void 
AmrParserTest::testParse() {
    // Create parser object
    AmrParser* parser = pmAmrParser;
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
    base::String codecName;
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong audio codec!", codecName, base::String("AMR"));
    codecName = parser->getVideoCodec();
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong video codec!", codecName, base::String("H263"));
}

void
AmrParserTest::testMultipleParse()
{
	struct MediaData md = MEDIA_TEST_3GP;
    for (int i(0); i < 10; i++) {
        // Create parser object
		MockMediaObject * mockMediaObject = md.createMock(BUFFER_SIZE);
        java::MediaObject* mediaObject = new java::MediaObject(pmEnv, (jobject)mockMediaObject);
        AmrParser* parser = new AmrParser(mediaObject);
        CPPUNIT_ASSERT_MESSAGE("Failed to create a AmrParser instance", parser != NULL);

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
        base::String codecName;
        codecName = parser->getAudioCodec();
        CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong audio codec!", codecName, base::String("AMR"));
        codecName = parser->getVideoCodec();
        CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong video codec!", codecName, base::String("H263"));

        delete parser;
        delete mediaObject;
        delete mockMediaObject;
    }
}

void 
AmrParserTest::testGetVideoFrames()
{
    // Create parser object
    AmrParser* parser = pmAmrParser;
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
    //    printRtpPacket(&rtpPacket);
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
AmrParserTest::testGetAudio()
{
    AmrParser* parser = pmAmrParser;
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
AmrParserTest::testParseError()
{
    bool gotException(false);
	struct MediaData md = MEDIA_TEST_3GP;

	base::String reference(md.fileName);
    base::String badMoov("badMoov");
    base::String badMdat("badMdat");

    reference += "." + md.extension;
    badMoov += "." + md.extension;
	badMdat += "." + md.extension;

    // Copy reference file
    MovFile referenceFile(reference.c_str());
    referenceFile.copyTo(badMoov.c_str());
    referenceFile.copyTo(badMdat.c_str());

    // Corrupt files
    // Corrupt the MDAT atom
    MovFile badMdatFile(badMdat.c_str());
    badMdatFile.open(MovFile::OPEN_AS_IO);
    CPPUNIT_ASSERT_MESSAGE("No MDAT atom!", badMdatFile.find(quicktime::MDAT));
    badMdatFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    badMdatFile.writeDW(quicktime::BAJS);
    badMdatFile.open(MovFile::OPEN_AS_INPUT);
    CPPUNIT_ASSERT_MESSAGE("No BAJS!", badMdatFile.find(quicktime::BAJS));
    badMdatFile.close();

    // Corrupt the MOOV atom
    MovFile badMoovFile(badMoov.c_str());
    badMoovFile.open(MovFile::OPEN_AS_IO);
    CPPUNIT_ASSERT_MESSAGE("No MOOV atom!", badMoovFile.find(quicktime::MOOV));
    badMoovFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    badMoovFile.writeDW(quicktime::BAJS);
    badMoovFile.open(MovFile::OPEN_AS_INPUT);
    CPPUNIT_ASSERT_MESSAGE("No BAJS!", badMoovFile.find(quicktime::BAJS));
    badMoovFile.close();

    // Parse files
    // Expecting parse failure due to faulty MDAT atom
    {
        // Creating a MOV parser for the resulting MOV file
		MockMediaObject mockMediaObject(md.path, 
										badMdat, 
										md.extension, 
										md.contentType, 
                                        512);
        java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);
        AmrParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }
    

    // Expecting parse failure due to faulty MOOV atom
    {
        // Creating a MOV parser for the resulting MOV file
		MockMediaObject mockMediaObject(md.path, 
                                        badMoov, 
										md.extension, 
                                        md.contentType, 
                                        512);
        java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);
        AmrParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }

    // Expecting parse failure due to faulty MOOV atom
    if (false) {
        // Creating a MOV parser for the resulting MOV file
		MockMediaObject mockMediaObject(MEDIA_CORRUPT_3GP.path, 
                                        MEDIA_CORRUPT_3GP.fileName, 
                                        MEDIA_CORRUPT_3GP.extension, 
                                        MEDIA_CORRUPT_3GP.contentType, 
                                        512);
        java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);
        AmrParser movParser(&mediaObject);
        MediaParser* parser = &movParser;
        parser->init();
        CPPUNIT_ASSERT_MESSAGE("No parse failure ", parser->parse() == false);
    }
}

void
AmrParserTest::printRtpPacket(MovRtpPacket* packet)
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
AmrParserTest::validateMovInfo(const MovInfo& movInfo) const
{
}

