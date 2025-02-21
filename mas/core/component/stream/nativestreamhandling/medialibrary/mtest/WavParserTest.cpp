/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <WavParserTest.h>
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>


#include <ostream>
#if defined(WIN32)
//#include <windows.h>
#include <io.h>
#else
#include <unistd.h>
#endif
#include <algorithm>
//#include <fstream.h>
#include <fstream>
#include <time.h>
#include <sstream>

#include "TestJNIUtil.h"
#include "jniutil.h"

#include "byteutilities.h"
#include "logger.h"
#include "medialibraryexception.h"
#include "wavreader.h"
#include "java/mediaobject.h"

#include "MockMediaObject.h"


using namespace std;
using namespace CppUnit;
 
#include "TestMedia.h"

/** The buffer size of each ByteBuffer in the MediaObject */
const jlong BUFFER_SIZE = 9999;


WavParserTest::WavParserTest():
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")) 
{    
}

WavParserTest::~WavParserTest() {
    for (unsigned i(0); i < mMediaObjectVector.size(); i++) {
        delete mMediaObjectVector[i];
        mMediaObjectVector[i] = 0;
    }
    for (unsigned i(0); i < mMockMediaObjectVector.size(); i++) {
        delete mMockMediaObjectVector[i];
        mMockMediaObjectVector[i] = 0;
    }
    for (unsigned i(0); i < mWavParserVector.size(); i++) {
        delete mWavParserVector[i];
        mWavParserVector[i] = 0;
    }
}

void 
WavParserTest::setUp() {
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
}

void 
WavParserTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
}

void 
WavParserTest::testConstructor() {
	WavParser* parser = createParser(pmEnv, MEDIA_TEST_PCMU, BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Failed to create WavParser", parser != NULL);
    
    try {
        parser = createParser(pmEnv, MEDIA_ILLEGAL, BUFFER_SIZE);
        CPPUNIT_FAIL("Constructor should throw exception if illegal file");
    } catch (MediaLibraryException&) {/*ok*/}
}


void 
WavParserTest::testPcmuParse() {
	const MediaData testMedia[] = {MEDIA_BEEP_PCMU,MEDIA_TEST_PCMU};
	testParse(testMedia,2);
}

void 
WavParserTest::testPcmaParse() {
	const MediaData testMedia[] = {MEDIA_BEEP_PCMA,MEDIA_TEST_PCMA};
	testParse(testMedia,2);
}


void WavParserTest::testParse(const MediaData testMedia[],int testMediaCount) {
    // Parse files, with a parallell WavReader to check position and such
    for (int i = 0; i < testMediaCount; i++) {

		struct MediaData md = testMedia[i];
        WavParser* parser = createParser(pmEnv, md, BUFFER_SIZE);     


        // Test to parse the WAVE file
        parser->parse();
        const WavInfo& wavInfo = parser->getMediaInfo();
		md.validate(wavInfo);

        // After the parse method the current location would be at the data chunk
        const WavReader* pWavReader = parser->getWavReader();
        CPPUNIT_ASSERT_MESSAGE(
            "The location should be at the first byte of raw data, i.e. after the id and size fields of the data chunk",
            pWavReader->bytesRead() == offsetToFirstDataByte(md));
    }
}


void WavParserTest::testPcmaGetData() {
	const MediaData md[] = {MEDIA_TEST_PCMA, MEDIA_BEEP_PCMA};
	testGetData(md,2);
}

void WavParserTest::testPcmuGetData() {
	const MediaData md[] = {MEDIA_TEST_PCMU, MEDIA_BEEP_PCMU};
	testGetData(md,2);
}

void WavParserTest::testGetData(const MediaData testMedia[],int testMediaCount) 
{
    
    for (int i = 0; i < testMediaCount; i++) { 
		MediaData md = testMedia[i];

		WavParser* pWavParser = createParser(pmEnv, md, BUFFER_SIZE);     
        
		// Parse files, with a WavReader only to retrieve data chunk position and size
		int expectedDataChunkSize;
		int expectedBytesRead = offsetToFirstDataByte(md,expectedDataChunkSize);

		// Parse the WAVE file
        pWavParser->parse();
        
        // After the parse method the current location would be at the first raw data chunk
        const WavReader* pWavReader = pWavParser->getWavReader();
        CPPUNIT_ASSERT_MESSAGE(
            "The location should be at the first byte of raw data, i.e. after the id and size fields of the data chunk",
			pWavReader->bytesRead() == expectedBytesRead);
        
        uint32_t dataSize = pWavParser->getMediaInfo().getDataChunkSize();


		CPPUNIT_ASSERT_MESSAGE(
            "The size of the data chunk read from the parser and the reader are not same",
            expectedDataChunkSize == dataSize);

		const unsigned char* pData = 0;
        size_t requestedSize = 789;		// Arbitrary chunk size
        size_t readBytes = 0;
		int chunkCount  = pWavParser->getAudioChunkCount();

        for (int i = 0; i < chunkCount; i++) {
            pData = pWavParser->getAudioChunk(requestedSize, i);
            if (requestedSize > 0) {
                readBytes += requestedSize;
            }
        }
        CPPUNIT_ASSERT_MESSAGE(
            "The size of the bytes read does not match the size of the data chunk", 
            expectedDataChunkSize == readBytes);
    }
}

int WavParserTest::offsetToFirstDataByte(MediaData & md) {

		int chunkSize;
		return offsetToFirstDataByte(md,chunkSize);

}

int WavParserTest::offsetToFirstDataByte(MediaData & md,int & chunkSize) {

		// create a WavReader and set it to the first byte of raw data
		MockMediaObject mockMediaObject(md.path, md.fileName, md.extension, md.contentType, 7);
        java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);
        WavReader wavReader(&mediaObject);
        wavReader.init();

		bool foundChunk = wavReader.seekChunk("data", true);
        CPPUNIT_ASSERT_MESSAGE("Did not find data chunk", foundChunk);
            
        wavReader.jumpForward(4);
        wavReader.readDW((uint32_t &)chunkSize);
      
        return wavReader.bytesRead();
}

WavParser* 
WavParserTest::createParser(JNIEnv* env, struct MediaData & mediaData, jlong bufferSize) 
{
	MockMediaObject* mockMediaObject = new MockMediaObject(mediaData.path, mediaData.fileName, mediaData.extension, mediaData.contentType, (int)bufferSize);
    java::MediaObject* pMediaObject = new java::MediaObject(env, (jobject)mockMediaObject);
    WavParser* pWavParser = new WavParser(pMediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a WavParser instance", pWavParser != NULL);
    pWavParser->init();
    pWavParser->setPTime(20);
    mMockMediaObjectVector.push_back(mockMediaObject);
    mMediaObjectVector.push_back(pMediaObject);
    mWavParserVector.push_back(pWavParser);
    return pWavParser;
}

