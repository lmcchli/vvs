/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */


#include <MovReaderTest.h>

#include "java/mediaobject.h"
#include "movatomid.h"

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
#include <fstream>
#include <time.h>
#include <sstream>

#include "TestJNIUtil.h"
#include "jniutil.h"
//#include "testutil.h"
#include "medialibraryexception.h"

#include "MockMediaObject.h"
#include <TestMedia.h>
using namespace std;
using namespace CppUnit;
//using namespace Asserter;
 
MovReaderTest::MovReaderTest()
    : mLogger(Logger::getLogger("medialibrary.MovReaderTest")) {
    
}

MovReaderTest::~MovReaderTest() 
{
    for (unsigned int i(0); i < mMovReaderVector.size(); i++) {
        delete mMovReaderVector[i];
        mMovReaderVector[i] = 0;
    }
}

void MovReaderTest::testConstructor() 
{
	struct MediaData md = MEDIA_TEST_PCMU_MOV;
    for (int k=0; k < NR_OF_READERS; k++) {
		MockMediaObject mockMediaObject(md.path, md.fileName, md.extension, md.contentType, BUFFER_SIZE_ARRAY[k]);
        java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);
        MovReader* reader = createReader(pmEnv, &mediaObject, false);
    }
}

void
MovReaderTest::testMovReaderPcmu() {
	testMovReader(MEDIA_TEST_PCMU_MOV);
}

void
MovReaderTest::testMovReaderPcma() {
	testMovReader(MEDIA_TEST_PCMA_MOV);
}

void
MovReaderTest::testMovReader(struct MediaData md)
{
    
	MockMediaObject mockMediaObject(md.path, md.fileName, md.extension, md.contentType, 512);
    java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);
    MovReader* reader = createReader(pmEnv, &mediaObject, false);
    CPPUNIT_ASSERT_MESSAGE("NULL reader", reader != NULL);
    // The current position should be 0 (tell)
    int position(reader->tell());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong position", 0, position);
    // A MOV file should start with an atom header (MDAT or MOOV)
    unsigned dataLength;
    unsigned length;
    unsigned id;
    reader->getAtomInformation(dataLength, id);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Bad atom", (int)MovAtomId::MDAT, (int)id);
    reader->jumpBackward(4);
    reader->readDW(id);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Bad atom", (int)MovAtomId::MDAT, (int)id);
    // The current position should be 8 (tell)
    position = reader->tell();
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong position", 8, position);
    // The the atom length and go there (by seek)
    position = reader->seek(dataLength);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong position", (int)dataLength, position);
    // Verify that we have an atom header here (MDAT or MOOV)
    reader->getAtomInformation(length, id);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Bad atom", (int)MovAtomId::MOOV, (int)id);
    // Go back and get the previous atom id
    reader->seek(4);
    reader->readDW(id);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Bad atom", (int)MovAtomId::MDAT, (int)id);
    // Go forth and get the other atom id
    length = dataLength + 4;
    position = reader->seek(length);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong position", (int)length, position);
    reader->readDW(id);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Bad atom", (int)MovAtomId::MOOV, (int)id);
}

MovReader* 
MovReaderTest::getReader(int index) 
{
    return (MovReader*)MediaObjectReaderTest::getReader(index);
}

MovReader* 
MovReaderTest::createReader(JNIEnv* env, java::MediaObject* mo, bool swap) 
{
    MovReader* pMovReader = new MovReader(mo);
    //setReader(pWavReader, 0);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a WavReader instance",
			   pMovReader != NULL);
    mMovReaderVector.push_back(pMovReader);
    pMovReader->init();
    return pMovReader;
}
