/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <WavReaderTest.h>
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>


#include <ostream>
#if defined(WIN32)
#include <windows.h>
#include <io.h>
#else
#include <unistd.h>
#endif
#include <algorithm>
#include <fstream>
#include <time.h>
#include <sstream>

#include "TestJNIUtil.h"
#include "jniutil.h"
#include "TestUtil.h"
#include "medialibraryexception.h"

using namespace std;
using namespace CppUnit;
 
#include <TestMedia.h>
static struct MediaData LOCAL_MEDIA_TEST_PCMU = MEDIA_TEST_PCMU;
static struct MediaData LOCAL_MEDIA_BEEP_PCMU = MEDIA_BEEP_PCMU;


WavReaderTest::WavReaderTest()
    : mLogger(Logger::getLogger("medialibrary.WavReaderTest")) {
    
}

WavReaderTest::~WavReaderTest() 
{
    for (unsigned int i(0); i < mWavReaderVector.size(); i++) {
        delete mWavReaderVector[i];
        mWavReaderVector[i] = 0;
    }
}

void WavReaderTest::testConstructor() 
{
    for (int k=0; k < NR_OF_READERS; k++) {
        java::MediaObject* mediaObject = 
			TestUtil::createReadOnlyCCMediaObject(pmEnv, MEDIA_TEST_PCMU, BUFFER_SIZE_ARRAY[k]);
		WavReader* reader = createReader(pmEnv, mediaObject);

        mediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, MEDIA_ILLEGAL, BUFFER_SIZE_ARRAY[k]);
        try {
            reader = createReader(pmEnv, mediaObject);
            CPPUNIT_FAIL("Constructor should throw exception if illegal file");
        } catch (MediaLibraryException& e) {/*ok*/}
    }
}
void 
WavReaderTest::testGetRiffSize() 
{
    java::MediaObject* mediaObject = 
		TestUtil::createReadOnlyCCMediaObject(pmEnv, LOCAL_MEDIA_TEST_PCMU, 512);
	WavReader* reader = createReader(pmEnv, mediaObject);

	ostringstream oss;
    uint32_t riffSize = reader->getRiffSize();
    
	oss << "Riffsize should be FILE_SIZE-8=" << LOCAL_MEDIA_TEST_PCMU.fileSize-8 << ", it is:" << riffSize;
    CPPUNIT_ASSERT_MESSAGE(oss.str(),
		riffSize == LOCAL_MEDIA_TEST_PCMU.fileSize-8);
}

void 
WavReaderTest::testGetChunkId() 
{
    ostringstream oss;
 
	base::String chunkId;
    getReader(0)->reset();
    const char* pCur = getReader(0)->getCurrentLocation();
    getReader(0)->getChunkId(chunkId);
    CPPUNIT_ASSERT_MESSAGE("Should be at first byte", 
        getReader(0)->bytesRead() == 0);

    CPPUNIT_ASSERT_MESSAGE("The Id retreived from getChunkId should be equal to RIFF",
        chunkId == "RIFF");
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected by the getChunkId() method",
        pCur == getReader(0)->getCurrentLocation());
    
    // Only three bytes left of data should throw exception
	getReader(0)->jumpForward(LOCAL_MEDIA_TEST_PCMU.fileSize);
    getReader(0)->jumpBackward(3);
    try {
        getReader(0)->getChunkId(chunkId);
        CPPUNIT_FAIL("getChunkId method should throw std::out_of_range exception if only three bytes left of data");
    } catch (std::out_of_range &e) {/*ok*/}
    getReader(0)->jumpBackward(1);
    CPPUNIT_ASSERT_MESSAGE("Should be 4 bytes left", 
        getReader(0)->bytesLeft() == 4);
    try {
        getReader(0)->getChunkId(chunkId);
    } catch (std::out_of_range &e) {
        CPPUNIT_FAIL("getChunkId method should NOT throw std::out_of_range exception if four bytes left of data");    
    }
}
void 
WavReaderTest::testCompareChunkId() 
{
    ostringstream oss;
 
    getReader(0)->reset();
    const char* pCur = getReader(0)->getCurrentLocation();
    
    CPPUNIT_ASSERT_MESSAGE("Should be at first byte", 
        getReader(0)->bytesRead() == 0); 
    bool result = getReader(0)->compareChunkId("rIfF", true);

    CPPUNIT_ASSERT_MESSAGE(
        "compareChunkId should return true for base::String rIfF if ignore case=true",
        result);
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected by the getChunkId() method",
        pCur == getReader(0)->getCurrentLocation());
    result = getReader(0)->compareChunkId("rIfF", false);

    CPPUNIT_ASSERT_MESSAGE(
        "compareChunkId should return false for string rIfF if ignore case=false",
        !result);
    
    // exceptions
	getReader(0)->jumpForward(LOCAL_MEDIA_TEST_PCMU.fileSize);    
    getReader(0)->jumpBackward(3);
    pCur = getReader(0)->getCurrentLocation();
    try {
        getReader(0)->compareChunkId("AAAA", true);;
        CPPUNIT_FAIL("compareChunkId method should throw std::out_of_range exception if only three bytes left of data");
    } catch (std::out_of_range &e) {/*ok*/}
    // Check that location is not affected if exception is thrown
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected even if exception is thrown",
        pCur == getReader(0)->getCurrentLocation());
    
    try {
        getReader(0)->compareChunkId("AAA", false);;
    } catch (std::out_of_range &e) {
                CPPUNIT_FAIL("compareChunkId method should NOT throw std::out_of_range exception if three bytes left and passing a three bytes long string");
    }
    getReader(0)->reset();
    try {
        getReader(0)->compareChunkId("AAAAA", false);;
        CPPUNIT_FAIL("compareChunkId method should throw std::out_of_range exception passing a string with length greater than 4 bytes");
    } catch (std::out_of_range &e) {/*ok*/} 
}

void 
WavReaderTest::testGetChunkSize() 
{
    ostringstream oss;
    uint32_t chunkSize;
    
    getReader(0)->reset();
    const char* pCur = getReader(0)->getCurrentLocation();
    getReader(0)->getChunkSize(chunkSize);
	oss << "ChunkSize of the Riff chunk should be FILE_SIZE-8=" << LOCAL_MEDIA_BEEP_PCMU.fileSize-8 << ", it is:" << chunkSize;
    CPPUNIT_ASSERT_MESSAGE(oss.str(),
		chunkSize == LOCAL_MEDIA_BEEP_PCMU.fileSize-8);
    CPPUNIT_ASSERT_MESSAGE(
        "The location should not be affected by the getChunkSize() method",
        pCur == getReader(0)->getCurrentLocation());
    // exceptions
    
	getReader(0)->jumpForward(LOCAL_MEDIA_BEEP_PCMU.fileSize-1);   
    CPPUNIT_ASSERT_MESSAGE(
        "Should be one byte left",
        1 == getReader(0)->bytesLeft()); 
    // Exception should be thrown if only one byte left    
    pCur = getReader(0)->getCurrentLocation();
    try {
        getReader(0)->getChunkSize(chunkSize);
        CPPUNIT_FAIL("getChunkSize method should throw std::out_of_range exception if only seven bytes left of data");
    } catch (std::out_of_range &e) {/*ok*/}
    // assert that the location is not affected if exception is thrown
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected by the getChunkId() method",
        pCur == getReader(0)->getCurrentLocation());
    
    // Exception should be thrown if only seven bytes left    
    getReader(0)->jumpBackward(6);
    pCur = getReader(0)->getCurrentLocation();
    try {
        getReader(0)->getChunkSize(chunkSize);
        CPPUNIT_FAIL("getChunkSize method should throw std::out_of_range exception if only seven bytes left of data");
    } catch (std::out_of_range &e) {/*ok*/}

    // assert that the location is not affected if exception is thrown
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected by the getChunkId() method",
        pCur == getReader(0)->getCurrentLocation());
    getReader(0)->jumpBackward(1);
    try {
        getReader(0)->getChunkSize(chunkSize);
    } catch (std::out_of_range &e) {
        CPPUNIT_FAIL("getChunkSize method should not throw std::out_of_range exception if 8 bytes left of data");
    }
}

void 
WavReaderTest::testNextChunk() 
{
	const struct MediaData  mdList[] = {MEDIA_TEST_PCMU, MEDIA_GILLTY_ADPCM, MEDIA_BEEP_PCMA};
    for (int i = 0; i < 3; i++) {
		const struct MediaData md = mdList[i];
        java::MediaObject* pMediaObject = 
			TestUtil::createReadOnlyCCMediaObject(pmEnv, md,BUFFER_SIZE_ARRAY[0]);
        WavReader* pReader = createReader(pmEnv, pMediaObject);
        pReader->nextChunk();
        
        // We should now be at the fmt chunk!
        CPPUNIT_ASSERT_MESSAGE("Should be at the fmt chunk", 
            pReader->compareChunkId("fmt", true));  
        // Should take us to the last chunk
        base::String id;
         
        while(pReader->nextChunk()) {
            id.clear();
            pReader->getChunkId(id);
           // std::cout << "File:" << files[i] << ", Chunk:" << id << std::endl;
        }
        const char* pCur = pReader->getCurrentLocation();
        // Test that location is not affected if last chunk
        CPPUNIT_ASSERT_MESSAGE("nextChunk should return false if last chunk",
            !(pReader->nextChunk()));
        CPPUNIT_ASSERT_MESSAGE("The location should not be affected by nextChunk call if last chunk",
            pCur == pReader->getCurrentLocation());
        
    }
}

void 
WavReaderTest::testSeekChunk() 
{
	const struct MediaData mdList[] = {MEDIA_TEST_PCMA, MEDIA_GILLTY_ADPCM, MEDIA_BEEP_PCMU};
    for (int i = 0; i < 3; i++) {
		struct MediaData md = mdList[i];
        java::MediaObject* pMediaObject = 
			TestUtil::createReadOnlyCCMediaObject(pmEnv, md,BUFFER_SIZE_ARRAY[0]);
        WavReader* pReader = createReader(pmEnv, pMediaObject);
        const char* pCur = pReader->getCurrentLocation();
        bool result = pReader->seekChunk("rIfF", true);
        CPPUNIT_ASSERT_MESSAGE("The chunk rIfF should be found with ignore-case=true",
            result);
        CPPUNIT_ASSERT_MESSAGE("The location should not be affected by seekChunk as we're already at the RIFF chunk",
            pCur == pReader->getCurrentLocation());    
        
        result = pReader->seekChunk("rIfF", false);
        CPPUNIT_ASSERT_MESSAGE("The chunk rIfF should not be found with ignore-case=false",
            !result);
        CPPUNIT_ASSERT_MESSAGE(
            "The location should not be affected by seekChunk if chunk not found",
            pCur == pReader->getCurrentLocation());   
        
        // Test seek chunk not to be found
        result = pReader->seekChunk("apa", true);
        CPPUNIT_ASSERT_MESSAGE("The chunk apa should not be found in a WAVE",
            !result);
        CPPUNIT_ASSERT_MESSAGE("The location should not be affected by seekChunk if chunk not found",
            pCur == pReader->getCurrentLocation()); 
       
        // Test seek chunk with illegal length id
        try {
            result = pReader->seekChunk("apaaa", false);
            CPPUNIT_FAIL("seekChunk should throw exception if illegal length of searched id");
        } catch (out_of_range& e) {/*ok*/}   
        CPPUNIT_ASSERT_MESSAGE("The location should not be affected by seekChunk when it throws exception",
            pCur == pReader->getCurrentLocation()); 
        // Test seek of fmt chunk
        result = pReader->seekChunk("fmt", true);
        CPPUNIT_ASSERT_MESSAGE("The chunk fmt should be found in a WAVE",
            result);
        // Test seek of data chunk
        result = pReader->seekChunk("data", true);
        CPPUNIT_ASSERT_MESSAGE("The chunk data should be found in a WAVE",
            result);
         // Test seek of fmt chunk again
        pCur = pReader->getCurrentLocation();
        result = pReader->seekChunk("fmt", true);
        CPPUNIT_ASSERT_MESSAGE("The chunk fmt should not be found in a if at data chunk",
            !result);   
        CPPUNIT_ASSERT_MESSAGE("The location should not be affected by seekChunk if chunk not found",
            pCur == pReader->getCurrentLocation());  
        
        // goto start and seek data chunk again
        pReader->reset();
        // Test seek of data chunk
        result = pReader->seekChunk("data", true);
        CPPUNIT_ASSERT_MESSAGE("The chunk data should be found in a WAVE",
            result);
    }   
}

WavReader* 
WavReaderTest::getReader(int index) 
{
    return (WavReader*)MediaObjectReaderTest::getReader(index);
}

WavReader* 
WavReaderTest::createReader(JNIEnv* env, java::MediaObject* mo) 
{     
    WavReader* pWavReader = new WavReader(mo);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a WavReader instance",
        pWavReader != NULL);
    mWavReaderVector.push_back(pWavReader);
    pWavReader->init();
    return pWavReader;
}
