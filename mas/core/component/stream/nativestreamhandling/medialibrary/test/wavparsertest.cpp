/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <wavparsertest.h>
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>


#include <ostream>
#include <unistd.h>
#include <algorithm>
//#include <fstream.h>
#include <fstream>
#include <time.h>
#include <sstream>

#include "testjniutil.h"
#include "jniutil.h"
#include "testutil.h"
#include "byteutilities.h"
#include "logger.h"
#include "medialibraryexception.h"
#include "wavreader.h"

using namespace std;
using namespace CppUnit;
//using namespace Asserter;
 
const char* WavParserTest::FILENAME = 
    "test.wav";
const int WavParserTest::FILE_SIZE = 38568;
const char* WavParserTest::ILLEGAL_FILENAME = 
    "illegalfile.wav";    
const char* WavParserTest::FILENAME2 = 
    "gillty.wav";   
const char* WavParserTest::FILENAME3 = 
    "beep.wav";  
const jlong WavParserTest::BUFFER_SIZE = 9999;
    
WavParserTest::WavParserTest():
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")) {
    
}

WavParserTest::~WavParserTest() {

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
    //cout << "WavReaderTest::testConstructor" << endl;
    pmWavParser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Failed to create WavParser", pmWavParser != NULL);
}

void 
WavParserTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
    delete pmWavParser; 
}

void 
WavParserTest::testConstructor() {
    //cout << "WavParserTest::testConstructor" << endl;
    WavParser* parser = createParser(pmEnv, FILENAME, BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Failed to create WavParser", parser != NULL);
    
    delete parser;
     
    try {
        parser = createParser(pmEnv, ILLEGAL_FILENAME, BUFFER_SIZE);
        CPPUNIT_FAIL("Constructor should throw exception if illegal file");
    } catch (MediaLibraryException& e) {/*ok*/}
    
}
void 
WavParserTest::testParse() {
    const char* files[3] = {FILENAME, FILENAME2, FILENAME3};
    const char* pCur;
    string contentType("audio/wav");
    string fileExtension("wav");
    // Parse files, with a parallell WavReader to check position and such
    for (int i = 0; i < 3; i++) { 
        WavParser* pWavParser = createParser(pmEnv, files[i], BUFFER_SIZE);     
        java::MediaObject* pMediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, files[i], 7,
                contentType, fileExtension);
        WavReader* pWavReader = new WavReader(pMediaObject);
        pWavReader->init();
        pWavReader->seekChunk("data", true);
        pWavReader->jumpForward(8);
        size_t bytesRead = pWavReader->bytesRead();
        // Test to parse the WAVE file
        pWavParser->parse();
        const WavInfo& wavInfo = pWavParser->getMediaInfo();
        if (i == 0) {
            validateWavInfo(wavInfo);
        }
        // After the parse method the current location would be at the data chunk
        const WavReader* wavReader = pWavParser->getWavReader();
        CPPUNIT_ASSERT_MESSAGE(
            "The location should be at the first byte of raw data, i.e. after the id and size fields of the data chunk",
            wavReader->bytesRead() == bytesRead);
    }
}
void 
WavParserTest::validateWavInfo(const WavInfo& wavInfo) const{
    uint32_t temp32;
    uint16_t temp16;
    
    temp32 = wavInfo.getRiffLength();
    cout << "RiffLength: " << temp32 << endl;
    CPPUNIT_ASSERT_MESSAGE("RiffLength should be 38560", temp32 == 38560);
    temp16 = wavInfo.getCompressionCode();
    cout << "Compression code: " << temp16 << endl;
    CPPUNIT_ASSERT_MESSAGE("Compression code should be 7", temp16 == 7);   
    temp16 = wavInfo.getNumChannels();
    cout << "Num Channels: " << temp16 << endl;
    CPPUNIT_ASSERT_MESSAGE("Number of channels should 1", temp16 == 1);   
    temp32 = wavInfo.getAudioSampleRate();
    cout << "Audio Sample Rate: " << temp32<< endl;
    CPPUNIT_ASSERT_MESSAGE("SampleRate should be 8000", temp32 == 8000);
    temp32 = wavInfo.getByteRate();
    cout << "Byte Rate: " << temp32 << endl;
    CPPUNIT_ASSERT_MESSAGE("ByteRate should be 8000", temp32 == 8000);
    temp16 = wavInfo.getBlockAlign();
    cout << "Block Align: " << temp16 << endl;
    CPPUNIT_ASSERT_MESSAGE("Block Align should be 1", temp16 == 1);   
    temp16 = wavInfo.getBitsPerSample();
    cout << "Bits per sample: " << temp16 << endl;
    CPPUNIT_ASSERT_MESSAGE("Bits per sample should be 8", temp16 == 8);
    temp32 = wavInfo.getDataChunkSize();
    cout << "Chunk Size: " << temp32 << endl;
    CPPUNIT_ASSERT_MESSAGE("Data chunk size should be 38512", temp32 == 38512);   
}
void 
WavParserTest::testGetData() {
    const char* files[3] = {FILENAME, FILENAME2, FILENAME3};
    const char* pCur;
    uint32_t temp32;
    string contentType("audio/wav");
    string fileExtension("wav");
    
    // Parse files, with a parallell WavReader to check position and such
    for (int i = 0; i < 3; i++) { 
        WavParser* pWavParser = createParser(pmEnv, files[i], BUFFER_SIZE);     
        
        // create a WavReader and set it to the first byte of raw data
        java::MediaObject* pMediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, files[i], 7,
                contentType, fileExtension);
        WavReader* pWavReader = new WavReader(pMediaObject);
        pWavReader->init();
        bool foundChunk = pWavReader->seekChunk("data", true);
        CPPUNIT_ASSERT_MESSAGE("Did not found data chunk",
            foundChunk);
            
        pWavReader->jumpForward(4);
        pWavReader->readDW(temp32);
        ByteUtilities::alignDW(temp32);
        //cout << "Read data size from reader " << temp32 << endl;
      
        size_t bytesRead = pWavReader->bytesRead();
        // Test to parse the WAVE file
        pWavParser->parse();
        
        // After the parse method the current location would be at the first raw data chunk
        const WavReader* wavReader = pWavParser->getWavReader();
        CPPUNIT_ASSERT_MESSAGE(
            "The location should be at the first byte of raw data, i.e. after the id and size fields of the data chunk",
            wavReader->bytesRead() == bytesRead);
        
        uint32_t dataSize = pWavParser->getMediaInfo().getDataChunkSize();
        cout << "Read data size from parser " << dataSize << " and the reader " << temp32 << endl;
        CPPUNIT_ASSERT_MESSAGE(
            "The size of the data chunk read from the parser and the reader is not same",
            dataSize == temp32);
        const unsigned char* pData(NULL);
        size_t requestedSize = 789;
        size_t actualSize;
        size_t readBytes = 0;
        int chunkCount(pWavParser->getAudioChunkCount());
        for (int i = 0; i < chunkCount; i++) {
            //cout << "Requested size: " << requestedSize;
            pData = pWavParser->getAudioChunk((unsigned int&)requestedSize, i);
            //cout << "Retrieved " << requestedSize << " number of bytes" <<endl;
            if (requestedSize > 0) {
                readBytes += requestedSize;
            }
        }
        //cout << "Total number of bytes read " << readBytes << endl;
        CPPUNIT_ASSERT_MESSAGE(
            "The size of the bytes read does not match the size of the data chunk", 
            readBytes == dataSize);
    }
}
WavParser* 
WavParserTest::createParser(JNIEnv* env, const char* fileName, jlong bufferSize) {
    string contentType("audio/wav");
    string fileExtension("wav");
    
    java::MediaObject* mediaObject = 
        TestUtil::createReadOnlyCCMediaObject(env, fileName, bufferSize,
            contentType, fileExtension); 
    WavParser* pWavParser = new WavParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a WavParser instance",
        pWavParser != NULL);
    pWavParser->init();
    pWavParser->setPTime(20);
    return pWavParser;
}

