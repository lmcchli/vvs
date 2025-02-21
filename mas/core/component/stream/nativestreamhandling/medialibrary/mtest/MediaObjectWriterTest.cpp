/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "MediaObjectWriterTest.h"
#include "mediaobjectwriter.h"

#include "java/mediaobject.h"

#include <cppunit/TestResult.h>
#include <cppunit/Asserter.h>

#include <ostream>
#include <fstream>

#include "TestJNIUtil.h"
#include "TestUtil.h"
#include "jniutil.h"
#include "platform.h"
#include "logger.h"

#include <TestMedia.h>
using namespace std;
using namespace CppUnit;
 

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
    java::MediaObject mediaObject(mEnv, jMediaObject);
    MediaObjectWriter writer(&mediaObject, 1);
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
        java::MediaObject mediaObject(mEnv, jMediaObject);
		base::String fileName("./"+MEDIA_BEEP_PCMU.fileName+"."+MEDIA_BEEP_PCMU.extension);
        TestUtil::setContentTypeAndFileExtension(mEnv, &mediaObject,
			MEDIA_BEEP_PCMU.contentType, MEDIA_BEEP_PCMU.extension);
        MediaObjectWriter writer(&mediaObject, bufferSizes[i]);
        writer.open();
        ifstream examplefile(fileName.c_str());
        uint8 *buffer = new uint8[chunkSizes[i]];    
        int nrOfChunks(0);
		CPPUNIT_ASSERT_MESSAGE("File error", examplefile.is_open());
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
        delete [] buffer;
        
        TestUtil::setImmutable(mEnv, jMediaObject);
        int numberOfBuffers(0);
        const char* data(NULL);
        while ((data = mediaObject.getData()) != NULL) {
             mediaObject.readNextBuffer();
             numberOfBuffers++;
        }
        ostringstream os;
		os << "TestCase #" << i << ": Unexpected number of buffers";
        CPPUNIT_ASSERT_EQUAL_MESSAGE(os.str(), expectedNrOfBuffers[i], numberOfBuffers);
		os << "TestCase #" << i << ": Unexpected number of chunks";
        CPPUNIT_ASSERT_EQUAL_MESSAGE(os.str(), expectedNrOfChunks[i], nrOfChunks);
        
    }
}
