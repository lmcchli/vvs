/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <wavreadertest.h>
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>


#include <ostream>
#include <unistd.h>
#include <algorithm>
#include <fstream>
#include <time.h>
#include <sstream>

#include "testjniutil.h"
#include "jniutil.h"
#include "testutil.h"
#include "medialibraryexception.h"

using namespace std;
using namespace CppUnit;
//using namespace Asserter;
 
 
const char* WavReaderTest::ILLEGAL_FILENAME = 
    "illegalfile.wav";    
const char* WavReaderTest::FILENAME2 = 
    "gillty.wav";   
const char* WavReaderTest::FILENAME3 = 
    "beep.wav";  
    
WavReaderTest::WavReaderTest()
    : mLogger(Logger::getLogger("medialibrary.WavReaderTest")) {
    
}

WavReaderTest::~WavReaderTest() 
{

}

void WavReaderTest::testConstructor() 
{
    string contentType("audio/wav");
    string fileExtension("wav");
    for (int k=0; k < NR_OF_READERS; k++) {
        //cout << "WavReaderTest::testConstructor" << endl;
        java::MediaObject* mediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, FILENAME, 
                BUFFER_SIZE_ARRAY[k], contentType, fileExtension);
        WavReader* reader = createReader(pmEnv, mediaObject, false);
        delete reader;
        
        mediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, 
                ILLEGAL_FILENAME, BUFFER_SIZE_ARRAY[k],
                contentType, fileExtension);
        try {
            reader = createReader(pmEnv, mediaObject, false);
            CPPUNIT_FAIL("Constructor should throw exception if illegal file");
        } catch (MediaLibraryException& e) {/*ok*/}
    }
}
void 
WavReaderTest::testGetRiffSize() 
{
    ostringstream oss;
    //cout << "WavReaderTest::testConstructor" << endl;
    
    uint32_t riffSize = getReader(0)->getRiffSize();
    
    oss << "Riffsize should be FILE_SIZE-8=" << FILE_SIZE-8 << ", it is:" << riffSize;
    CPPUNIT_ASSERT_MESSAGE(oss.str(),
        riffSize == FILE_SIZE-8);
}
void 
WavReaderTest::testGetChunkId() 
{
    ostringstream oss;
 
    string chunkId;
    getReader(0)->reset();
    const char* pCur = getReader(0)->getCurrentLocation();
    getReader(0)->getChunkId(chunkId);
    CPPUNIT_ASSERT_MESSAGE("Should be at first byte", 
        getReader(0)->bytesRead() == 0);
//    cout << "found id:" << chunkId;
    CPPUNIT_ASSERT_MESSAGE("The Id retreived from getChunkId should be equal to RIFF",
        chunkId.compare("RIFF") == 0);
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected by the getChunkId() method",
        pCur == getReader(0)->getCurrentLocation());
    
    // Only three bytes left of data should throw exception
    getReader(0)->jumpForward(FILE_SIZE);
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
 
    //cout << "WavReaderTest::testConstructor" << endl;
    getReader(0)->reset();
    const char* pCur = getReader(0)->getCurrentLocation();
    
    CPPUNIT_ASSERT_MESSAGE("Should be at first byte", 
        getReader(0)->bytesRead() == 0); 
    bool result = getReader(0)->compareChunkId("rIfF", true);
    //cout << "found id:" << chunkId;
    CPPUNIT_ASSERT_MESSAGE(
        "compareChunkId should return true for string rIfF if ignore case=true",
        result);
    CPPUNIT_ASSERT_MESSAGE("The location should not be affected by the getChunkId() method",
        pCur == getReader(0)->getCurrentLocation());
    result = getReader(0)->compareChunkId("rIfF", false);
    //cout << "found id:" << chunkId;
    CPPUNIT_ASSERT_MESSAGE(
        "compareChunkId should return false for string rIfF if ignore case=false",
        !result);
    
    // exceptions
    getReader(0)->jumpForward(FILE_SIZE);    
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
    oss << "ChunkSize of the Riff chunk should be FILE_SIZE-8=" << FILE_SIZE-8 << ", it is:" << chunkSize;
    CPPUNIT_ASSERT_MESSAGE(oss.str(),
        chunkSize == FILE_SIZE-8);
    CPPUNIT_ASSERT_MESSAGE(
        "The location should not be affected by the getChunkSize() method",
        pCur == getReader(0)->getCurrentLocation());
    // exceptions
    
    getReader(0)->jumpForward(FILE_SIZE-1);   
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
    string contentType("audio/wav");
    string fileExtension("wav");
    const char* files[3] = {FILENAME, FILENAME2, FILENAME3};
    for (int i = 0; i < 3; i++) {
        java::MediaObject* pMediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, files[i], 
            BUFFER_SIZE_ARRAY[0], contentType, fileExtension);
        WavReader* pReader = createReader(pmEnv, pMediaObject, false);
        pReader->nextChunk();
        
        // We should now be at the fmt chunk!
        CPPUNIT_ASSERT_MESSAGE("Should be at the fmt chunk", 
            pReader->compareChunkId("fmt", true));  
        // Should take us to the last chunk
        string id;
         
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
    string contentType("audio/wav");
    string fileExtension("wav");
    const char* files[3] = {FILENAME, FILENAME2, FILENAME3};
    for (int i = 0; i < 3; i++) {
        java::MediaObject* pMediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, files[i], 
                BUFFER_SIZE_ARRAY[0], contentType, fileExtension);
        WavReader* pReader = createReader(pmEnv, pMediaObject, false);
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
WavReaderTest::createReader(JNIEnv* env, java::MediaObject* mo, bool swap) 
{
     
    WavReader* pWavReader = new WavReader(mo);
    //setReader(pWavReader, 0);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a WavReader instance",
        pWavReader != NULL);
    pWavReader->init();
    return pWavReader;
}
