/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>
#include <mediaobjectreadertest.h>

#include <ostream>
#include <unistd.h>
#include <algorithm>
#include <sys/time.h>
#include <fstream>
#include <sstream>

#include "testjniutil.h"
#include "testutil.h"
#include "jniutil.h"
#include "platform.h"
#include "logger.h"

using namespace std;
using namespace CppUnit;
//using namespace Asserter;
 
 
const char* MediaObjectReaderTest::FILENAME = 
    "beep.wav";
const int MediaObjectReaderTest::FILE_SIZE = 1420;
uint32_t MediaObjectReaderTest::BUFFER_SIZE_ARRAY[] = {500, 1420, 1500, 1};

MediaObjectReaderTest::MediaObjectReaderTest(): 
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")),
    alreadyAttached(false) {
     
}

MediaObjectReaderTest::~MediaObjectReaderTest() {
    
}

void MediaObjectReaderTest::setUp() {
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
    string contentType("audio/wav");
    string fileExtension("wav");     
    for (int i = 0; i < NR_OF_READERS; i++) {
        //cout << "WavReaderTest::testConstructor" << endl;
        java::MediaObject* mediaObject = TestUtil::createReadOnlyCCMediaObject(
            pmEnv, FILENAME, BUFFER_SIZE_ARRAY[i], contentType, fileExtension);
        //LOGGER_DEBUG(mLogger.get(), "Creating MediaObject from file:" << FILENAME);
        CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mediaObject != NULL);
        MediaObjectReader* pMediaObjectReader = createReader(pmEnv, mediaObject, false);
        setReader(pMediaObjectReader, i);
        CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", 
            pMediaObjectReader != NULL);
    }
}

void MediaObjectReaderTest::tearDown() {
    
//    LOGGER_DEBUG(mLogger.get(), "tearDown");
//    for (int i = 0; i < NR_OF_READERS; i++) {
//        delete mMediaObjectReaderArray[i];
//    }
  //  LOGGER_DEBUG(mLogger.get(), "tearDown in middle");    
    
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
    //LOGGER_DEBUG(mLogger.get(), "tearDown Finished");
}

void MediaObjectReaderTest::testConstructor() {
    string contentType("audio/wav");
    string fileExtension("wav");     
    for (int k=0; k < NR_OF_READERS; k++) {
        //cout << "MediaObjectReaderTest::testConstructor" << endl;
        java::MediaObject* mediaObject = TestUtil::createReadOnlyCCMediaObject(
            pmEnv, FILENAME, BUFFER_SIZE_ARRAY[k], contentType, fileExtension);
        CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mediaObject != NULL);
        MediaObjectReader* pMediaObjectReader = createReader(pmEnv, mediaObject, false);
        CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", pMediaObjectReader != NULL);
        CPPUNIT_ASSERT_MESSAGE("Should be at first byte", 
            pMediaObjectReader->bytesRead() == 0);
        delete pMediaObjectReader;
    }
}
void MediaObjectReaderTest::testReset() {
    const char* pCurrent = mMediaObjectReaderArray[0]->getCurrentLocation();
    mMediaObjectReaderArray[0]->reset();   
    CPPUNIT_ASSERT_MESSAGE("The location should be set to the start after a reset",
        pCurrent == mMediaObjectReaderArray[0]->getCurrentLocation());
}
void MediaObjectReaderTest::testCompareStr() {
    // The content is a WAV so there should be a RIFF header
    bool result = mMediaObjectReaderArray[0]->compareStr("rIFf", 4, true); 
    CPPUNIT_ASSERT_MESSAGE("rIFf compared against RIFF should return true when ignoring case", result);    
    result = mMediaObjectReaderArray[0]->compareStr("rIFf", 4, false);
    CPPUNIT_ASSERT_MESSAGE(
        "rIFf compared against RIFF should return false when case-sensitive", 
        !result);
    result = mMediaObjectReaderArray[0]->compareStr("RIF", 3, false);
    CPPUNIT_ASSERT_MESSAGE("RIF compared against RIFF should return true length is 3", result);        
    result = mMediaObjectReaderArray[0]->compareStr("X", 0, false);    
    CPPUNIT_ASSERT_MESSAGE("0-length comparison should alwars return true", result);        
    result = mMediaObjectReaderArray[0]->compareStr("", 0, false);    
    CPPUNIT_ASSERT_MESSAGE("0-length comparison should alwars return true", result);        
    result = mMediaObjectReaderArray[0]->compareStr("", 0, false);    
    CPPUNIT_ASSERT_MESSAGE("0-length comparison should alwars return true", result);        
    
    
    const char* pCurrent = mMediaObjectReaderArray[0]->getCurrentLocation();
    mMediaObjectReaderArray[0]->reset();
    CPPUNIT_ASSERT_MESSAGE("compareStr should not affect location", 
        pCurrent == mMediaObjectReaderArray[0]->getCurrentLocation()); 
    
    try {
        result = mMediaObjectReaderArray[0]->compareStr("X", 2, false);    
        CPPUNIT_FAIL("2-length comparison of a 1-length string should throw exception");     
    } catch (std::out_of_range &e) {/*ok*/}
        
    while (mMediaObjectReaderArray[0]->jumpForward(1));
    mMediaObjectReaderArray[0]->jumpBackward(1);
    // should be at last byte
    try {
        result = mMediaObjectReaderArray[0]->compareStr("X", 1, false);    
    } catch (std::out_of_range &e) {
       CPPUNIT_FAIL("1-length comparison should not throw exception if location at last byte");     
    }       
    try {
        result = mMediaObjectReaderArray[0]->compareStr("XX", 2, false);    
        CPPUNIT_FAIL("2-length comparison should throw exception if location at last byte");     
    } catch (std::out_of_range &e) {/*ok*/}
}
void MediaObjectReaderTest::testPerformance() {
    
    benchmark();
    //cout << "MediaObjectReaderTest::testPerformance: In the following tests the MediaObject feeds itself on the Java side from a file" << endl; 
    char c;
    const char* pCurrent;
    timeval tim;
    long bytes = 0;

    string contentType("audio/wav");
    string fileExtension("wav");     
    
    // Read a MediaObject the first time
    java::MediaObject* mediaObject = 
        TestUtil::createReadOnlyCCMediaObject(pmEnv, FILENAME, 
            BUFFER_SIZE_ARRAY[0], contentType, fileExtension);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mediaObject != NULL);
    MediaObjectReader* pMediaObjectReader = createReader(pmEnv, mediaObject, false);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", pMediaObjectReader != NULL);
    gettimeofday(&tim, NULL);
    double t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    do {
        pCurrent = pMediaObjectReader->getCurrentLocation();
        CPPUNIT_ASSERT_MESSAGE("Invalid pointer before EOB",pCurrent != MediaObjectReader::EOB  || (pCurrent == MediaObjectReader::EOB && pMediaObjectReader->jumpForward(1) == 0));
	if(pCurrent != MediaObjectReader::EOB) { 
		c = *pCurrent;
		bytes++;
	}
    } while (pMediaObjectReader->jumpForward(1));
    gettimeofday(&tim, NULL);
    double t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    printf("\n%.6lf seconds to read %d bytes from a MediaObject the first time\n", t2-t1, bytes);
     
    // read a MediaObject the second time
    pMediaObjectReader->reset();
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    // todo remove forever loop
    //for (int j =0; j < 1000; j++) {
    while (pMediaObjectReader->jumpForward(1)) {
        pCurrent = pMediaObjectReader->getCurrentLocation();   
        c = *pCurrent;
    }
      //  pMediaObjectReader->reset();
    //}
    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    printf("%.6lf seconds to read %d bytes from a MediaObject the second time\n", 
        t2-t1, bytes);
    
    
    // read a MediaObject the THIRD time
    pMediaObjectReader->reset();
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    while (pMediaObjectReader->jumpForward(1)) {
        pCurrent = pMediaObjectReader->getCurrentLocation();   
        c = *pCurrent;
    }
    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    printf("%.6lf seconds to read %d bytes from a MediaObject the third time\n", 
        t2-t1, bytes);
}

void MediaObjectReaderTest::testMemoryPerformance() {
    
    //cout << "MediaObjectReaderTest::testMemoryPerformance: In the following tests the buffers are created in C++ and appended to a MediaObject" << endl; 
    char c;
    const char* pCurrent;
    timeval tim;
    long bytesRead = 0;
    double t1, t2;
    int i;
    
     
    // create an empty MediaObject
    jobject mediaObject = TestUtil::createRecordableMediaObject(pmEnv);
    CPPUNIT_ASSERT_MESSAGE("Failed to create Java MediaObject", mediaObject != NULL);
    // create buffers that match the filesize
    // append each byte buffer 
    ifstream examplefile1 (FILENAME);
    vector<MediaBuffer*> mediaBufferVector;
    
    while(!examplefile1.eof()) {
        // append buffer to MediaObject
        char *buffer = TestUtil::appendDataToMediaObject(pmEnv, mediaObject, 
            BUFFER_SIZE_ARRAY[0]);
        examplefile1.read(buffer, BUFFER_SIZE_ARRAY[0]);
        
        // Also create MediaBuffer and add that to a vector
        mediaBufferVector.push_back(new MediaBuffer(buffer, BUFFER_SIZE_ARRAY[0], false));
    }
    string contentType("audio/wav");
    string fileExtension("wav");
    TestUtil::setContentTypeAndFileExtension(pmEnv, mediaObject,
        contentType, fileExtension);
    TestUtil::setImmutable(pmEnv, mediaObject);
    
    // also read the file into one MemoryBuffer 
    ifstream examplefile2 (FILENAME);
    char *buffer2 = new char[FILE_SIZE];
    examplefile2.read(buffer2, FILE_SIZE);
    MediaBuffer* mediaBuffer2 = new MediaBuffer(buffer2, FILE_SIZE, false);
        
    // Read from a vector of mediabuffers...
    vector<MediaBuffer*>::iterator iter = mediaBufferVector.begin();
    bytesRead = 0;
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    while( iter != mediaBufferVector.end()) {
        MediaBuffer* pMB = (MediaBuffer*)*iter;
        do {
            const char* cur = pMB->getCurrentLocation();
	    CPPUNIT_ASSERT_MESSAGE("EOB before end of file !",cur != MediaBuffer::EOB);
	    c = *cur;
	    bytesRead++;
        } while (pMB->jumpForward(1) && pMB->getCurrentLocation() != MediaBuffer::EOB && bytesRead < FILE_SIZE);
        iter++;
    }

    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    printf("%.6lf seconds to read %d bytes from %d nr of MediaBuffers\n", 
        t2-t1, bytesRead, mediaBufferVector.size());
    
    // Read from one mediabuffer
    bytesRead = 0;
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    do {
       pCurrent = mediaBuffer2->getCurrentLocation();
       CPPUNIT_ASSERT(pCurrent != 0);
       c = *pCurrent;
       bytesRead++; 
    } while (mediaBuffer2->jumpForward(1));
    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    printf("%.6lf seconds to read %d bytes injected into one MediaBuffer\n", 
        t2-t1, bytesRead);
    
    
    // Now create a CCMediaObject
    java::MediaObject* ccmo = new java::MediaObject(pmEnv, mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", ccmo != NULL);
    // and a MediaObjectReader
    MediaObjectReader* moR = new MediaObjectReader(ccmo, false);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", moR != NULL);
    moR->init();
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    //for (i=0; i < 1000; i++) {
        bytesRead = 0;
        do {
            pCurrent = moR->getCurrentLocation();   
            CPPUNIT_ASSERT(pCurrent != 0);
            c = *pCurrent;
            bytesRead++;
            moR->jumpForward(1);    
        }while (moR->getCurrentLocation() != MediaObjectReader::EOB);
        moR->reset();
    //}  
    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
//    t2 = (t2-t1)/i;
    t2 = (t2-t1);
    printf("%.6lf seconds to read %d bytes from a MediaObjectReader appended with a MediaObject that contain same data as test above\n", 
            t2, bytesRead);  
     
}

void MediaObjectReaderTest::testJumpForward() {
    //cout << "MediaObjectReaderTest::testJump" << endl;
    char c;
    const char* pBeg;
    const char* pLastByte;
    const char* pCurrent;
    long bytes = 0;
    size_t jumpedBytes;
   
    // Test each reader with associated BufferSize
    for (int k=0; k < NR_OF_READERS; k++) {
//        LOGGER_DEBUG(mLogger.get(), 
//            "Testing MediaObjectReader::jumpForward with buffer size:" << 
//                BUFFER_SIZE_ARRAY[k]);
        pBeg = mMediaObjectReaderArray[k]->getCurrentLocation();
        CPPUNIT_ASSERT_MESSAGE("Should be at first byte", 
            mMediaObjectReaderArray[k]->bytesRead() == 0);
        // Read the file into a buffer to have something to compare with
        ifstream examplefile (FILENAME);
        char *buffer = new char[FILE_SIZE];
        examplefile.read(buffer, FILE_SIZE);
        // Compare content, and check bytesRead() and bytesLeft methods
        mMediaObjectReaderArray[k]->reset();
        for (int i = 0; i < FILE_SIZE ; i++) {
           c = buffer[i];   
           pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation();
           CPPUNIT_ASSERT_MESSAGE(
                "Character mismatch between file and MediaObjectReader", 
                c == *pCurrent);
//            LOGGER_DEBUG(mLogger.get(),
//                "jumpForward: BUFFER_SIZE:" << BUFFER_SIZE_ARRAY[k] << 
//                "bytes: " << i << 
//                ", bytesLeft():" << mMediaObjectReaderArray[k]->bytesLeft() <<
//                ", bytesRead():" << mMediaObjectReaderArray[k]->bytesRead() << 
//                ", c:" << c);
           CPPUNIT_ASSERT_MESSAGE("bytesRead() should be equal to i", 
                i == mMediaObjectReaderArray[k]->bytesRead());
           CPPUNIT_ASSERT_MESSAGE("bytesLeft() should be getTotalSize-i", 
                mMediaObjectReaderArray[k]->getTotalSize()-i == mMediaObjectReaderArray[k]->bytesLeft());
           CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (jumping forward)", 
                mMediaObjectReaderArray[k]->bytesLeft() + mMediaObjectReaderArray[k]->bytesRead() == mMediaObjectReaderArray[k]->getTotalSize());
           jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(1);
           CPPUNIT_ASSERT_MESSAGE("Should return 1", 
                jumpedBytes == 1);
        }
        CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
                mMediaObjectReaderArray[k]->getCurrentLocation() == MediaObjectReader::EOB);
        CPPUNIT_ASSERT_MESSAGE("At EOB bytesRead should be FILE_SIZE", 
                mMediaObjectReaderArray[k]->bytesRead() == FILE_SIZE);
        CPPUNIT_ASSERT_MESSAGE("At EOB bytesLeft should be 0", 
                mMediaObjectReaderArray[k]->bytesLeft() == 0);               
        jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(1);
        CPPUNIT_ASSERT_MESSAGE("At EOB jumpForward should return 0", 
                jumpedBytes == 0);
                
        // jump half file length
        mMediaObjectReaderArray[k]->reset();
        size_t toJump = FILE_SIZE/2;
        jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(toJump);
        //cout << "Jumped " << toJump << endl;
        CPPUNIT_ASSERT_MESSAGE("Number of bytes jumped mismatch", 
                jumpedBytes == toJump);
        CPPUNIT_ASSERT_MESSAGE("A jumpForward(x) should result in bytesRead=x", 
                mMediaObjectReaderArray[k]->bytesRead() == toJump);
        CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (jumping forward)", 
                mMediaObjectReaderArray[k]->bytesLeft() + 
                mMediaObjectReaderArray[k]->bytesRead() == 
                mMediaObjectReaderArray[k]->getTotalSize());        
    }
}
void MediaObjectReaderTest::testJumpBackward() {
    //cout << "MediaObjectReaderTest::testJump" << endl;
    char c;
    const char* pBeg;
    const char* pLastByte;
    const char* pCurrent;
    long bytes = 0;
    size_t jumpedBytes;
   
    // Read the file into a buffer to have something to compare with
    ifstream examplefile (FILENAME);
    char *buffer = new char[FILE_SIZE];
    examplefile.read(buffer, FILE_SIZE);
    // Test each reader with associated BufferSize
    for (int k=0; k < NR_OF_READERS; k++) {
//        LOGGER_DEBUG(mLogger.get(), 
//            "Testing MediaObjectReader::jumpBackward with buffer size:" << 
//                BUFFER_SIZE_ARRAY[k]);
        pBeg = mMediaObjectReaderArray[k]->getCurrentLocation();
        
        // Jump to EOB
        mMediaObjectReaderArray[k]->jumpForward(FILE_SIZE);
        // Jump backward to 4 bytes left.
        jumpedBytes = mMediaObjectReaderArray[k]->jumpBackward(4);  
        CPPUNIT_ASSERT_MESSAGE("Should be 4 bytes left", 
                mMediaObjectReaderArray[k]->bytesLeft() == 4);
        pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation();
        mMediaObjectReaderArray[k]->jumpForward(4);
        mMediaObjectReaderArray[k]->jumpBackward(4);  
        CPPUNIT_ASSERT_MESSAGE("Location mismatch after a jumpForward/jumpBackward near EOB", 
                pCurrent == mMediaObjectReaderArray[k]->getCurrentLocation());
    //printf("last character from file:%c, from reader(%X):%c, pLastByte:%X\n", 
    //        c, pCurrent, *pCurrent, pLastByte);
     
        // Jump backward and check content and bytesLeft, bytesRead
        mMediaObjectReaderArray[k]->reset();
        mMediaObjectReaderArray[k]->jumpForward(FILE_SIZE);
        jumpedBytes = mMediaObjectReaderArray[k]->jumpBackward(1);
        pLastByte =  mMediaObjectReaderArray[k]->getCurrentLocation();
        CPPUNIT_ASSERT_MESSAGE("bytesLeft() should be 1 after a jumpBackward(1) from EOB", 
                1 == mMediaObjectReaderArray[k]->bytesLeft());
        CPPUNIT_ASSERT_MESSAGE("bytesRead() should be FILE_SIZE-1 after a jumpBackward(1) from EOB", 
                FILE_SIZE-1 == mMediaObjectReaderArray[k]->bytesRead());        
        CPPUNIT_ASSERT_MESSAGE("pLastByte is null", 
                pLastByte != NULL);    
        for (int i = FILE_SIZE -1; i >= 0; i--) {
           c = buffer[i];   
    //       printf("bytes: %i, bytesLeft():%i, bytesRead():%i, c[%i]:%c\n",
    //             i, mMediaObjectReaderArray[k]->bytesLeft(), mMediaObjectReaderArray[k]->bytesRead(),
    //             i, c);
           pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation();
//           LOGGER_DEBUG(mLogger.get(), 
//                "jumpBackward Reader[" << k << "]" <<
//                            "BUFFER_SIZE:" << BUFFER_SIZE_ARRAY[k] <<
//                            ", i:" << i << ", bytesLeft():" << mMediaObjectReaderArray[k]->bytesLeft() <<
//                            ", bytesRead():" <<  mMediaObjectReaderArray[k]->bytesRead() <<
//                            ", c:" << c <<endl);
           ostringstream oss;
           CPPUNIT_ASSERT_MESSAGE(
                "Character mismatch when jumping backward", 
                c == *pCurrent); 
           CPPUNIT_ASSERT_MESSAGE("bytesRead() should be equal to i (jumping backward)", 
                i == mMediaObjectReaderArray[k]->bytesRead());
           CPPUNIT_ASSERT_MESSAGE("bytesLeft() should be getTotalSize-i (jumping backward)", 
                mMediaObjectReaderArray[k]->getTotalSize()-i == mMediaObjectReaderArray[k]->bytesLeft());
           CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (jumping backward)", 
                mMediaObjectReaderArray[k]->bytesLeft() + mMediaObjectReaderArray[k]->bytesRead() == mMediaObjectReaderArray[k]->getTotalSize());     
           jumpedBytes = mMediaObjectReaderArray[k]->jumpBackward(1);
           CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) should return 1", 
                jumpedBytes == 1 || i == 0);
           
        } 
        CPPUNIT_ASSERT_MESSAGE("Should be at first bytes", 
                mMediaObjectReaderArray[k]->getCurrentLocation() == mMediaObjectReaderArray[k]->firstByte());
        CPPUNIT_ASSERT_MESSAGE("At first byte bytesRead should be 0", 
                mMediaObjectReaderArray[k]->bytesRead() == 0);
        CPPUNIT_ASSERT_MESSAGE("At first byte bytesLeft should be FILE_SIZE", 
                mMediaObjectReaderArray[k]->bytesLeft() == FILE_SIZE); 
        
        // Jump forward til end and assert we are at end 
        while (mMediaObjectReaderArray[k]->getCurrentLocation() != MediaObjectReader::EOB) {
            jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(1);
            CPPUNIT_ASSERT_MESSAGE("jumpForward(1) should return 1", 
                jumpedBytes == 1); 
        }
        CPPUNIT_ASSERT_MESSAGE(
            "The location should be at EOB after a while(jumpForward(1))",
            MediaObjectReader::EOB ==  mMediaObjectReaderArray[k]->getCurrentLocation());
        
        // Jump backward til start and check location and bytesLeft/bytesRead
        while (mMediaObjectReaderArray[k]->getCurrentLocation() != mMediaObjectReaderArray[k]->firstByte()) {
            jumpedBytes = mMediaObjectReaderArray[k]->jumpBackward(1);
            CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) should return 1", 
                jumpedBytes == 1); 
        }
        
        CPPUNIT_ASSERT_MESSAGE(
            "The location should be at start after a while(jumpBackward(1))",
            mMediaObjectReaderArray[k]->firstByte() ==  mMediaObjectReaderArray[k]->getCurrentLocation());
        CPPUNIT_ASSERT_MESSAGE("bytesLeft() should be equal to total size after a while(jumpBackward(1))", 
             mMediaObjectReaderArray[k]->bytesLeft() ==  mMediaObjectReaderArray[k]->getTotalSize());
        CPPUNIT_ASSERT_MESSAGE("bytesRead() should be 0 after a while(jumpBackward(1))", 
             mMediaObjectReaderArray[k]->bytesRead() ==  0);
        
        // Jump til EOB and assert we are at EOB
        jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(FILE_SIZE+999);
        CPPUNIT_ASSERT_MESSAGE("jumpForward(FILE_SIZE) should return FILE_SIZE", 
                jumpedBytes == FILE_SIZE);
        //printf("After a jumpForward(%i), location:%X\n", FILE_SIZE-1, mMediaObjectReaderArray[k]->getCurrentLocation());
        CPPUNIT_ASSERT_MESSAGE("The location should be at end after a jumpForward(size-1)",
            MediaObjectReader::EOB ==  mMediaObjectReaderArray[k]->getCurrentLocation());   
        
        mMediaObjectReaderArray[k]->reset();           
        // Jump til last byte and assert we are at end
        jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(FILE_SIZE-1);
        //printf("After a jumpForward(%i), location:%X\n", FILE_SIZE-1, mMediaObjectReaderArray[k]->getCurrentLocation());
        CPPUNIT_ASSERT_MESSAGE("The location should be at end after a jumpForward(size-1)",
            pLastByte ==  mMediaObjectReaderArray[k]->getCurrentLocation());   
        CPPUNIT_ASSERT_MESSAGE("jumpForward(FILE_SIZE-1) should return FILE_SIZE-1", 
                jumpedBytes == FILE_SIZE-1);
        // Jump til start and assert we are at the start
        jumpedBytes = mMediaObjectReaderArray[k]->jumpBackward(FILE_SIZE-1);
        CPPUNIT_ASSERT_MESSAGE("The location should be at start after a jumpBackward(size-1)",
            mMediaObjectReaderArray[k]->firstByte() ==  mMediaObjectReaderArray[k]->getCurrentLocation());    
        CPPUNIT_ASSERT_MESSAGE("jumpBackward(FILE_SIZE-1) should return FILE_SIZE-1", 
                jumpedBytes == FILE_SIZE-1);
        // Jump til half of data
        size_t halfWays = FILE_SIZE/2;
        jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(halfWays);
        CPPUNIT_ASSERT_MESSAGE("Return from jumpForward not correct",
            halfWays == jumpedBytes);
        CPPUNIT_ASSERT_MESSAGE("bytesRead mismatch after a jump half data length",
            halfWays == mMediaObjectReaderArray[k]->bytesLeft());
        CPPUNIT_ASSERT_MESSAGE("bytesLeft/bytesRead mismatch after a jump half data length",
            mMediaObjectReaderArray[k]->getTotalSize() == mMediaObjectReaderArray[k]->bytesLeft()+mMediaObjectReaderArray[k]->bytesRead());
        
        // Jump to first buffer/buffer boundary, The following tests are only
        // meaningful if BUFFER_SIZE_ARRAY[k] < FILE_SIZE
        if (BUFFER_SIZE_ARRAY[k] < FILE_SIZE) { 
            mMediaObjectReaderArray[k]->reset();
            jumpedBytes = jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(BUFFER_SIZE_ARRAY[k]-1);
            pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation();
            CPPUNIT_ASSERT_MESSAGE("bytesRead mismatch after a jump BUFFER_SIZE_ARRAY[k]-1",
                jumpedBytes == mMediaObjectReaderArray[k]->bytesRead());
            CPPUNIT_ASSERT_MESSAGE("bytesLeft/bytesRead",
                mMediaObjectReaderArray[k]->getTotalSize() == mMediaObjectReaderArray[k]->bytesLeft()+mMediaObjectReaderArray[k]->bytesRead());
            // should be at the last byte of the current buffer
            CPPUNIT_ASSERT_MESSAGE("jumpForward should have returned BUFFER_SIZE_ARRAY[k]-1", 
                jumpedBytes == BUFFER_SIZE_ARRAY[k] -1);
            // jump over to next buffer
            jumpedBytes = mMediaObjectReaderArray[k]->jumpForward(4);
            CPPUNIT_ASSERT_MESSAGE("bytesLeft/bytesRead",
                mMediaObjectReaderArray[k]->getTotalSize() == mMediaObjectReaderArray[k]->bytesLeft()+mMediaObjectReaderArray[k]->bytesRead());
            CPPUNIT_ASSERT_MESSAGE("jumpForward should have returned 4", jumpedBytes == 4);
            jumpedBytes = mMediaObjectReaderArray[k]->jumpBackward(4);
            CPPUNIT_ASSERT_MESSAGE("bytesRead mismatch",
                BUFFER_SIZE_ARRAY[k]-1 == mMediaObjectReaderArray[k]->bytesRead());
            CPPUNIT_ASSERT_MESSAGE("bytesLeft/bytesRead mismatch",
                mMediaObjectReaderArray[k]->getTotalSize() == mMediaObjectReaderArray[k]->bytesLeft()+mMediaObjectReaderArray[k]->bytesRead());
            CPPUNIT_ASSERT_MESSAGE("jumpBackward should have returned 4", jumpedBytes == 4);
            CPPUNIT_ASSERT_MESSAGE("should be back after a jumpForward/jumpBackwrad", 
                pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation());
            // go to EOB and jump back passed a buffer boundary
            mMediaObjectReaderArray[k]->reset();
            mMediaObjectReaderArray[k]->jumpForward(FILE_SIZE);
            mMediaObjectReaderArray[k]->jumpBackward(BUFFER_SIZE_ARRAY[k]+1);
            CPPUNIT_ASSERT_MESSAGE("Should be BUFFER_SIZE_ARRAY[k]+1 bytes left", 
                mMediaObjectReaderArray[k]->bytesLeft() == BUFFER_SIZE_ARRAY[k]+1);
            CPPUNIT_ASSERT_MESSAGE("bytesLeft/bytesRead mismatch",
                mMediaObjectReaderArray[k]->getTotalSize() == mMediaObjectReaderArray[k]->bytesLeft()+mMediaObjectReaderArray[k]->bytesRead());
            pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation();
            mMediaObjectReaderArray[k]->jumpForward(BUFFER_SIZE_ARRAY[k]+1);
            mMediaObjectReaderArray[k]->jumpBackward(BUFFER_SIZE_ARRAY[k]+1);
            CPPUNIT_ASSERT_MESSAGE("should be back after a jumpForward/jumpBackwrad", 
                pCurrent = mMediaObjectReaderArray[k]->getCurrentLocation());  
        }
    }
} 
     
void MediaObjectReaderTest::testReadW() { 
     
    uint16_t temp;
    //cout << "MediaObjectReaderTest::testReadW" << endl;
   
    const char* pBeg = mMediaObjectReaderArray[0]->getCurrentLocation();
    const char* pEnd;
    const char* curLoc;
    char c;
    size_t bytesRead = 0;
    ostringstream oss;
    
    while ((curLoc = mMediaObjectReaderArray[0]->readW(temp)) != MediaObjectReader::EOB) {
        bytesRead += 2;
        //cout << "BytesRead:" << bytesRead << ", bytesLeft:" << mMediaObjectReaderArray[0]->bytesLeft() << endl;
        //cout << "BytesRead: " << bytesRead << endl;
        CPPUNIT_ASSERT_MESSAGE("A readW should skip to next word",
            mMediaObjectReaderArray[0]->bytesRead() == bytesRead);
        CPPUNIT_ASSERT_MESSAGE("Mismatch for return value of readW and getCurrentLocation()",
            curLoc == mMediaObjectReaderArray[0]->getCurrentLocation());
        CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (readW)", 
            mMediaObjectReaderArray[0]->bytesLeft() + mMediaObjectReaderArray[0]->bytesRead() == mMediaObjectReaderArray[0]->getTotalSize());        
    } 
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
            mMediaObjectReaderArray[0]->getCurrentLocation() == MediaObjectReader::EOB);
    CPPUNIT_ASSERT_MESSAGE("At EOB bytesRead should be FILE_SIZE", 
            mMediaObjectReaderArray[0]->bytesRead() == FILE_SIZE);
    CPPUNIT_ASSERT_MESSAGE("At EOB bytesLeft should be 0", 
            mMediaObjectReaderArray[0]->bytesLeft() == 0);    
    
    // TWO BYTES LEFT
    temp = 1;
    mMediaObjectReaderArray[0]->jumpBackward(2);
    oss << "Should be 2 bytes left, it is left:" << mMediaObjectReaderArray[0]->bytesLeft() << endl;
    CPPUNIT_ASSERT_MESSAGE(oss.str(), mMediaObjectReaderArray[0]->bytesLeft() == 2);
    curLoc = mMediaObjectReaderArray[0]->readW(temp);
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
        MediaObjectReader::EOB == mMediaObjectReaderArray[0]->getCurrentLocation());  
    CPPUNIT_ASSERT_MESSAGE("Did not read value", 
        temp != 1);
        
    // ONE BYTE LEFT
    temp = 1;
    mMediaObjectReaderArray[0]->jumpForward(FILE_SIZE);
    mMediaObjectReaderArray[0]->jumpBackward(1);
    CPPUNIT_ASSERT_MESSAGE("Should be 1 bytes left", mMediaObjectReaderArray[0]->bytesLeft() == 1);
    curLoc = mMediaObjectReaderArray[0]->readW(temp);
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
        MediaObjectReader::EOB == mMediaObjectReaderArray[0]->getCurrentLocation());  
    CPPUNIT_ASSERT_MESSAGE("Should not read value", 
        temp == 1);
    // Test to write values to file and read them back with readW 
    // ... first with a buffersize of 1  
    const char* filename =  "./readW.txt";  
    ofstream outfile (filename, ios::trunc | ios::binary);
    uint16_t value = 0;
    char buf[2];
    for (uint16_t i = 900; i < 1000; i++) {
        memcpy(buf, &i, sizeof(uint16_t)); 
        outfile << buf[0];
        //printf("Wrote:%X\n",buf[0]);
        outfile << buf[1];
        //printf("Wrote:%X\n",buf[1]);   
    }
    outfile.close();
    jlong bs = 1;
    string contentType("audio/wav");
    string fileExtension("wav");
    java::MediaObject* mo = 
        TestUtil::createReadOnlyCCMediaObject(pmEnv, filename, bs,
            contentType, fileExtension);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mo != NULL);
    MediaObjectReader* mor = createReader(pmEnv, mo, false);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", mor != NULL);
    uint16_t readBackValue;
    for (uint16_t i = 900; i < 1000; i++) {
        curLoc = mor->readW(readBackValue); 
        //cout << "Read back with MediaObjectReader.readW:" << readBackValue << endl;  
        CPPUNIT_ASSERT_MESSAGE("The value read from file with read does not match value written", 
            readBackValue == i);
    }
    // Do the same thing but with a while
    mor->reset();
    uint16_t i = 900;
    while (mor->readW(readBackValue) != MediaObjectReader::EOB) {
        CPPUNIT_ASSERT_MESSAGE("The value read from file with read does not match value written", 
            readBackValue == i);
        i++;
    }
    CPPUNIT_ASSERT_MESSAGE("readBackValue should be 999", readBackValue == 999);
    // ... and then with a bigger buffersize    
    bs = 1024;
    mo = TestUtil::createReadOnlyCCMediaObject(pmEnv, filename, bs,
        contentType, fileExtension);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mo != NULL);
    mor = createReader(pmEnv, mo, false);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", mor != NULL);
    for (uint16_t i = 900; i < 1000; i++) {
          
        mor->readW(readBackValue); 
        //cout << "Read back with MediaObjectReader.readW:" << readBackValue << endl;  
        CPPUNIT_ASSERT_MESSAGE("The value read from file with read does not match value written", 
            readBackValue == i);
        
    }
    
}
void MediaObjectReaderTest::testReadDW() { 
     
    uint32_t temp;
    //cout << "MediaObjectReaderTest::testReadW" << endl;
   
    const char* pBeg = mMediaObjectReaderArray[0]->getCurrentLocation();
    const char* pEnd;
    const char* curLoc;
    char c;
    size_t bytesRead = 0;
    
    while ((curLoc = mMediaObjectReaderArray[0]->readDW(temp)) != MediaObjectReader::EOB) {
        bytesRead += 4;
        //cout << "BytesRead: " << bytesRead << endl;
        CPPUNIT_ASSERT_MESSAGE("A readDW should skip to next double word",
            mMediaObjectReaderArray[0]->bytesRead() == bytesRead);
        CPPUNIT_ASSERT_MESSAGE("Mismatch for return value of readW and getCurrentLocation()",
            curLoc == mMediaObjectReaderArray[0]->getCurrentLocation());
        CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (readW)", 
            mMediaObjectReaderArray[0]->bytesLeft() + mMediaObjectReaderArray[0]->bytesRead() == mMediaObjectReaderArray[0]->getTotalSize());        
    } 
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
            mMediaObjectReaderArray[0]->getCurrentLocation() == MediaObjectReader::EOB);
    CPPUNIT_ASSERT_MESSAGE("At EOB bytesRead should be FILE_SIZE", 
            mMediaObjectReaderArray[0]->bytesRead() == FILE_SIZE);
    CPPUNIT_ASSERT_MESSAGE("At EOB bytesLeft should be 0", 
            mMediaObjectReaderArray[0]->bytesLeft() == 0);    
     
        
    // four BYTES LEFT
    temp = 1;
    mMediaObjectReaderArray[0]->jumpBackward(4);
    CPPUNIT_ASSERT_MESSAGE("Should be 4 bytes left", mMediaObjectReaderArray[0]->bytesLeft() == 4);
    curLoc = mMediaObjectReaderArray[0]->readDW(temp);
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
        MediaObjectReader::EOB == mMediaObjectReaderArray[0]->getCurrentLocation());  
    CPPUNIT_ASSERT_MESSAGE("Did not read value", 
        temp != 1);
        
    // three BYTE LEFT
    temp = 1;
    mMediaObjectReaderArray[0]->jumpForward(FILE_SIZE);
    mMediaObjectReaderArray[0]->jumpBackward(3);
    CPPUNIT_ASSERT_MESSAGE("Should be 3 bytes left", mMediaObjectReaderArray[0]->bytesLeft() == 3);
    curLoc = mMediaObjectReaderArray[0]->readDW(temp);
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
        MediaObjectReader::EOB == mMediaObjectReaderArray[0]->getCurrentLocation());  
    CPPUNIT_ASSERT_MESSAGE("Should not read value", 
        temp == 1);
        
          
    // Test to write values to file and read them back with readW 
    // ... first with a buffersize of 1  
    const char* filename =  "./readDW.txt";  
    ofstream outfile (filename, ios::trunc | ios::binary);
    uint32_t value = 0;
    char buf[4];
    for (uint32_t i = 900; i < 1000; i++) {
        memcpy(buf, &i, sizeof(uint32_t)); 
        outfile << buf[0];
        //printf("Wrote:%X\n",buf[0]);
        outfile << buf[1];
        outfile << buf[2];
        outfile << buf[3];
        //printf("Wrote:%X\n",buf[1]);   
    }
    outfile.close();
    size_t bs = 1;
    string contentType("audio/wav");
    string fileExtension("wav");
    java::MediaObject* mo = TestUtil::createReadOnlyCCMediaObject(pmEnv, 
        filename, bs, contentType, fileExtension);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mo != NULL);
    MediaObjectReader* mor = createReader(pmEnv, mo, false);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", mor != NULL);
    uint32_t readBackValue;
    for (uint32_t i = 900; i < 1000; i++) {
        curLoc = mor->readDW(readBackValue); 
        //cout << "Read back with MediaObjectReader.readW:" << readBackValue << endl;  
        CPPUNIT_ASSERT_MESSAGE("The value read from file with read does not match value written", 
            readBackValue == i);
    }
    
    // Do the same thing but with a while
    mor->reset();
    uint32_t i = 900;
    while (mor->readDW(readBackValue) != MediaObjectReader::EOB) {
        CPPUNIT_ASSERT_MESSAGE("The value read from file with read does not match value written", 
            readBackValue == i);
        i++;
    }
    CPPUNIT_ASSERT_MESSAGE("readBackValue should be 999", readBackValue == 999);
    // ... and then with a bigger buffersize    
    bs = 1024;
    mo = TestUtil::createReadOnlyCCMediaObject(pmEnv, filename, bs,
        contentType, fileExtension);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObject", mo != NULL);
    mor = createReader(pmEnv, mo, false);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MediaObjectReader", mor != NULL);
    for (uint32_t i = 900; i < 1000; i++) {
        mor->readDW(readBackValue); 
        //cout << "Read back with MediaObjectReader.readW:" << readBackValue << endl;  
        CPPUNIT_ASSERT_MESSAGE("The value read from file with read does not match value written", 
            readBackValue == i);
    }
}
void 
MediaObjectReaderTest::testMark() {
   
    //cout << "MediaObjectReaderTest::testMark" << endl; 
    uint32_t temp32;
    uint16_t temp16; 
     
    const char* pEnd;
    const char* pBeg;
    const char* curLoc;
    bool result;
    
    for (int i = 0; i < NR_OF_READERS; i++) {
        mMediaObjectReaderArray[i]->reset();
        size_t toJump = FILE_SIZE/2;
        size_t jumpedBytes = mMediaObjectReaderArray[i]->jumpForward(toJump);
        //cout << "Jumped " << toJump << endl;
        CPPUNIT_ASSERT_MESSAGE("Number of bytes jumped mismatch", 
                jumpedBytes == toJump);
        CPPUNIT_ASSERT_MESSAGE("A jumpForward(x) should result in bytesRead=x", 
                mMediaObjectReaderArray[i]->bytesRead() == toJump);
        CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (jumping forward)", 
                mMediaObjectReaderArray[i]->bytesLeft() + 
                mMediaObjectReaderArray[i]->bytesRead() == 
                mMediaObjectReaderArray[i]->getTotalSize()); 
        mMediaObjectReaderArray[i]->setMark();
        curLoc =  mMediaObjectReaderArray[i]->getCurrentLocation();
        
        // Do some operations on the reader 
        while(mMediaObjectReaderArray[i]->jumpForward(1)) {}
        while(mMediaObjectReaderArray[i]->jumpBackward(1)) {}
        mMediaObjectReaderArray[i]->jumpForward(BUFFER_SIZE_ARRAY[0]);
        mMediaObjectReaderArray[i]->compareStr("HEJJLO", 1, false);
        mMediaObjectReaderArray[i]->readW(temp16);
        mMediaObjectReaderArray[i]->readDW(temp32);
        mMediaObjectReaderArray[i]->reset();
        mMediaObjectReaderArray[i]->gotoMark();
        
        ostringstream oss;
        oss << "The setMark/gotoMark test failed, current buffersize:" << 
                BUFFER_SIZE_ARRAY[i];
        CPPUNIT_ASSERT_MESSAGE(oss.str(),
            curLoc == mMediaObjectReaderArray[i]->getCurrentLocation());    
    }
}

void 
MediaObjectReaderTest::testGetData() {
    //cout << "MediaObjectReaderTest::testGetData" << endl; 
    uint32_t temp32;
    uint16_t temp16; 
     
    const char* pEnd;
    const char* pBeg;
    const char* curLoc;
    const char* curLoc1;
    bool result;
    
    size_t requestedSize = 1024;
    size_t readSize = 0;
    size_t totalRead = 0;
    const char* pData;
    vector<MediaBuffer*> v;
    mMediaObjectReaderArray[0]->reset();
    // Fetch all data with the getData method, and validate the returned content
    while ((pData = mMediaObjectReaderArray[0]->getData(requestedSize, (unsigned int&)readSize)) != MediaObjectReader::EOB) {
        totalRead += readSize;    
        //cout << "getData: read bytes:" << readSize << ", totalRead:" << totalRead << endl;
        MediaBuffer* mediaBuffer = new MediaBuffer(pData, readSize, false);
        CPPUNIT_ASSERT_MESSAGE("Size of MediaBuffer is not coherent with read size",
            mediaBuffer->getBufferSize() == readSize);
        mMediaObjectReaderArray[0]->reset();
        mMediaObjectReaderArray[0]->jumpForward(totalRead-readSize);
        // DO a character check of the data against reader
        while ((curLoc = mediaBuffer->getCurrentLocation()) != MediaBuffer::EOB) {
            curLoc1 = mMediaObjectReaderArray[0]->getCurrentLocation();
            //printf("Characters from reader:%c from getData:%c\n", *curLoc1, *curLoc);
            CPPUNIT_ASSERT_MESSAGE("Character mismatch", *curLoc == *curLoc1); 
            mMediaObjectReaderArray[0]->jumpForward(1); 
            mediaBuffer->jumpForward(1);  
        }
        // Copy content to a new allocated buffer as, the data is only valid til next call
        char* dataBuffer = new char[mediaBuffer->getBufferSize()];
        memcpy(dataBuffer, pData, readSize);
        mediaBuffer = new MediaBuffer(dataBuffer, readSize, false);
        v.push_back(mediaBuffer);
        CPPUNIT_ASSERT_MESSAGE("A getData should skip read bytes forward",
            mMediaObjectReaderArray[0]->bytesRead() == totalRead);
        CPPUNIT_ASSERT_MESSAGE("bytesLeft()+bytesRead() should be equal to getTotalSize() (readW)", 
            mMediaObjectReaderArray[0]->bytesLeft() + mMediaObjectReaderArray[0]->bytesRead() == mMediaObjectReaderArray[0]->getTotalSize());        
    } 
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
            mMediaObjectReaderArray[0]->getCurrentLocation() == MediaObjectReader::EOB);
    CPPUNIT_ASSERT_MESSAGE("At EOB bytesRead should be FILE_SIZE", 
            mMediaObjectReaderArray[0]->bytesRead() == FILE_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Totalread should be FILE_SIZE", 
            totalRead == FILE_SIZE);
    CPPUNIT_ASSERT_MESSAGE("At EOB bytesLeft should be 0", 
            mMediaObjectReaderArray[0]->bytesLeft() == 0);    

    mMediaObjectReaderArray[0]->reset();
    
    // Iterate over all data and assert it is equal to data in reader
    vector<MediaBuffer*>::iterator iter = v.begin();
    while (iter != v.end()) {
        MediaBuffer* currentBuffer = *iter;
        currentBuffer->gotoFirstByte();
        CPPUNIT_ASSERT_MESSAGE("MediaBuffer is not at first byte after gotoFirstByte", 
            currentBuffer->firstByte() == currentBuffer->getCurrentLocation());
        while ((curLoc = currentBuffer->getCurrentLocation()) != MediaBuffer::EOB) {
            ostringstream oss;
            oss << "Character mismatch, Character from reader:" << *curLoc1 << ", from getData:" << *curLoc << endl;
            curLoc1 = mMediaObjectReaderArray[0]->getCurrentLocation();
            //printf("Characters from reader:%c from getData:%c\n", *curLoc1, *curLoc);
            CPPUNIT_ASSERT_MESSAGE(oss.str(), *curLoc == *curLoc1);    
            currentBuffer->jumpForward(1);
            mMediaObjectReaderArray[0]->jumpForward(1);
        }
        iter++;
    }
    
    // go to EOB and test getData
    mMediaObjectReaderArray[0]->jumpForward(FILE_SIZE);
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
            mMediaObjectReaderArray[0]->getCurrentLocation() == MediaObjectReader::EOB);
    pData = mMediaObjectReaderArray[0]->getData(requestedSize, (unsigned int&)readSize);
    CPPUNIT_ASSERT_MESSAGE("getData at EOB should read 0 bytes", readSize == 0);
}  
void 
MediaObjectReaderTest::testGetTotalSize() {
   
    //cout << "MediaObjectReaderTest::testGetData" << endl; 
    uint32_t temp32;
    uint16_t temp16; 
     
    const char* pEnd;
    const char* pBeg;
    const char* curLoc;
    bool result;
    
    CPPUNIT_ASSERT_MESSAGE("getTotalSize() mismatch with size of file",
        mMediaObjectReaderArray[0]->getTotalSize() == FILE_SIZE);
}

MediaObjectReader* 
MediaObjectReaderTest::getReader(int index) {
    return mMediaObjectReaderArray[index];
}
void 
MediaObjectReaderTest::setReader(MediaObjectReader* reader, int index) {
    mMediaObjectReaderArray[index] = reader;
}
MediaObjectReader* 
MediaObjectReaderTest::createReader(JNIEnv* env, java::MediaObject* mo, bool swap) {
     
    MediaObjectReader* pMediaObjectReader = new MediaObjectReader(mo, swap);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a MediaObjectReader instance",
        pMediaObjectReader != NULL);
    pMediaObjectReader->init();
    return pMediaObjectReader;
}


void
MediaObjectReaderTest::benchmark() {
    cout << "*** Benchmarks for performance:" << endl; 
    char c;
    const char* pCurrent;
    timeval tim;
    int bytesRead = 0;
    double t1,t2;
    
    // read the file into memory and read from that
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0); 
    int j; 
    for (j=0; j < 1000; j++) {
        char *buffer = new char[FILE_SIZE];
        ifstream examplefile1 (FILENAME);
        examplefile1.read(buffer, FILE_SIZE);
        for (int i = 0; i < FILE_SIZE; i++) {
            c = buffer[i];    
        }
    }
    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    t2 = (t2-t1)/j;
    
    printf("%.6lf seconds to read %d bytes into memory and then read them\n", 
        t2, FILE_SIZE); 
        
    // read direct from file with ifstream
    gettimeofday(&tim, NULL);
    t1=tim.tv_sec+(tim.tv_usec/1000000.0);
    ifstream examplefile (FILENAME);
    
    while (! examplefile.eof() ) {
        examplefile.get(c);   
        bytesRead++;
    }
    gettimeofday(&tim, NULL);
    t2=tim.tv_sec+(tim.tv_usec/1000000.0);
    printf("%.6lf seconds to read %d bytes direct from disk with an ifstream\n", 
        t2-t1, bytesRead);    
}
 
