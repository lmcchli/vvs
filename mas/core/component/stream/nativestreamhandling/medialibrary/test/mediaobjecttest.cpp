/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>
#include <mediaobjecttest.h>

#include <ostream>
#include <unistd.h>
#include <algorithm>
#include <iostream>
#include <string>
#include <sstream>


#include "testjniutil.h"
#include "jniutil.h"
#include "testutil.h"

using namespace std;
using namespace CppUnit;

const char* MediaObjectTest::FILENAME = 
    "test.wav";
const int MediaObjectTest::FILE_SIZE = 38568;

const char* MediaObjectTest::MEDIAOBJECTFACTORY_CLASS = 
    "com/mobeon/masp/mediaobject/factory/MediaObjectFactory"; 

const char* MediaObjectTest::MEDIAOBJECTFACTORY_SIGNATURE = "(I)V";

const char* MediaObjectTest::MEDIAOBJECTFACTORY_CREATE = "create";
const char* MediaObjectTest::MEDIAOBJECTFACTORY_CREATE_SIGNATURE = 
    "(Ljava/io/File;Ljava/lang/String;)Lcom/mobeon/masp/mediaobject/IMediaObject;";

MediaObjectTest::MediaObjectTest() {

}

MediaObjectTest::~MediaObjectTest() {

}

void MediaObjectTest::setUp() {
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    }
    
}

void MediaObjectTest::tearDown() {
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
}

void MediaObjectTest::testMediaObject() {
    string contentType("audio/wav");
    string fileExtension("wav");
    java::MediaObject* mo = TestUtil::createReadOnlyCCMediaObject(pmEnv, 
        FILENAME, 10000, contentType, fileExtension);
    
    Asserter::failIf(mo == NULL, "Failed to create a MediaObject instance");
}

void MediaObjectTest::testReadData() {
    ostringstream oss;

    // One buffer
    int bufferSize(100000);
    string contentType("audio/wav");
    string fileExtension("wav");
    java::MediaObject* mo = TestUtil::createReadOnlyCCMediaObject(pmEnv, 
        FILENAME, bufferSize, contentType, fileExtension);
    int counter(0);
    const char* data(NULL);
    while ((data = mo->getData()) != NULL) {
        mo->readNextBuffer();
        counter++;
    }
    oss << "One buffer: Unexpected number of buffers: " << counter << endl;
    Asserter::failIf(counter != 1, oss.str());

    // Several buffers
    bufferSize = 500;
    mo = TestUtil::createReadOnlyCCMediaObject(pmEnv, FILENAME, bufferSize,
        contentType, fileExtension);
    counter = 0;
    while ((data = mo->getData()) != NULL) {
        mo->readNextBuffer();
        counter++;
    }
    int expectedCount(FILE_SIZE / bufferSize);
    if ((FILE_SIZE % bufferSize) > 0) {
        expectedCount++;
    }
    oss << "Several buffers: Unexpected number of buffers: expected=" << 
        expectedCount << ", actual=" << counter << endl;
    Asserter::failIf(counter != expectedCount, oss.str());
}


