/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <cppunit/TestResult.h>
#include <cppunit/Asserter.h>

#include "mediaobjectwritertest.h"
#include "mediaobjectwriter.h"

#include <ostream>
#include <fstream>

#include "testjniutil.h"
#include "testutil.h"
#include "jniutil.h"
#include "platform.h"
#include "logger.h"

using namespace std;
using namespace CppUnit;
//using namespace Asserter;
 
 
const char* MediaObjectWriterTest::FILENAME = "beep.wav";
const int MediaObjectWriterTest::FILE_SIZE = 1420;

MediaObjectWriterTest::MediaObjectWriterTest(): 
    mLogger(Logger::getLogger("medialibrary.MediaObjectWriterTest")),
    mAlreadyAttached(false), mEnv(NULL) {
}

MediaObjectWriterTest::~MediaObjectWriterTest() {
}

void MediaObjectWriterTest::setUp() {
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    mAlreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&mEnv, mAlreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
}

void MediaObjectWriterTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!mAlreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
}

void MediaObjectWriterTest::testConstructor() {
    jobject jMediaObject = TestUtil::createRecordableMediaObject(mEnv);
    java::MediaObject* mediaObject = new java::MediaObject(mEnv, jMediaObject);
    MediaObjectWriter writer(mediaObject, 1);
}

void MediaObjectWriterTest::testWrite() {
    
    int bufferSizes[] = {32, 33, 65};
    int chunkSizes[] = {32, 32, 32};
    int expectedNrOfChunks[] = {45, 45, 45};
    // If the chunksize is equal to the buffersize, one buffer is created
    // for each buffer. If the chunksize is smaller but not less than 
    // buffersize/2, two chunks can fit in each buffer but the last chunk
    // will reside in its own buffer (45/2+1=23).
    int expectedNrOfBuffers[] = {45, 
                                 23,  //(45/2+1=23)
                                 15}; //(45/3)
    
    for (int i = 0; i < 3; i++) {
        jobject jMediaObject = TestUtil::createRecordableMediaObject(mEnv);
        java::MediaObject* mediaObject = new java::MediaObject(mEnv, jMediaObject);
        string contentType("audio/wav");
        string fileExtension("WAV");
        TestUtil::setContentTypeAndFileExtension(mEnv, mediaObject,
            contentType, fileExtension);
        MediaObjectWriter writer(mediaObject, bufferSizes[i]);
        writer.open();
        ifstream examplefile(FILENAME);
        uint8 *buffer = new uint8[chunkSizes[i]];    
        int nrOfChunks(0);
        while(!examplefile.eof()) {
            examplefile.read((char*)buffer, chunkSizes[i]);
            writer.write((const char*)buffer, chunkSizes[i]);
            nrOfChunks++;
        }
        try {
            writer.close();
        }
        catch (runtime_error& e) {
            Asserter::fail(e.what());
        }
        delete buffer;
        
        TestUtil::setImmutable(mEnv, jMediaObject);
        int numberOfBuffers(0);
        const char* data(NULL);
        while ((data = mediaObject->getData()) != NULL) {
             mediaObject->readNextBuffer();
             numberOfBuffers++;
        }
        ostringstream os;
        os << "TestCase " << i << ": Unexpected number of buffers: " << 
            numberOfBuffers << " (expected=" << expectedNrOfBuffers[i] << ")";
        CPPUNIT_ASSERT_MESSAGE(os.str(), 
            numberOfBuffers == expectedNrOfBuffers[i]);
        os << "TestCase " << i << ": Unexpected number of chunks: " << 
            nrOfChunks << " (expected=" << expectedNrOfChunks[i] << ")";
        CPPUNIT_ASSERT_MESSAGE(os.str(), nrOfChunks == expectedNrOfChunks[i]);
        
        //string fileName("mittLillaTest.wav");
        //TestUtil::saveAs(mEnv, jMediaObject, fileName);
        
        delete mediaObject;
    }
}
