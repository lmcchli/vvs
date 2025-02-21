/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include "movatomid.h"

#include <movreadertest.h>
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
 
 
const char* MovReaderTest::ILLEGAL_FILENAME = 
    "illegalfile.mov";    
const char* MovReaderTest::FILENAME2 = 
    "test.mov";   
const char* MovReaderTest::FILENAME3 = 
    "beep.mov";  
    
MovReaderTest::MovReaderTest()
    : mLogger(Logger::getLogger("medialibrary.MovReaderTest")) {
    
}

MovReaderTest::~MovReaderTest() 
{

}

void MovReaderTest::testConstructor() 
{
    string contentType("video/quicktime");
    string fileExtension("mov");
    
    for (int k=0; k < NR_OF_READERS; k++) {
        java::MediaObject* mediaObject = 
            TestUtil::createReadOnlyCCMediaObject(pmEnv, "test.mov", 
					  BUFFER_SIZE_ARRAY[k], contentType, fileExtension);
        MovReader* reader = createReader(pmEnv, mediaObject, false);
        delete reader;
	/*        
        mediaObject = 
            TestUtil::createCCMediaObject(pmEnv, ILLEGAL_FILENAME, 
					  BUFFER_SIZE_ARRAY[k]);
        try {
            reader = createReader(pmEnv, mediaObject, false);
            CPPUNIT_FAIL("Constructor should throw exception "
			 "if illegal file");
        } catch (MediaLibraryException& e) {} 	*/

    }
}

void
MovReaderTest::testMovReader()
{
    string contentType("audio/wav");
    string fileExtension("wav");
    
    java::MediaObject* mediaObject = 
	TestUtil::createReadOnlyCCMediaObject(pmEnv, "test.mov", 
				      512, contentType, fileExtension);
    MovReader* reader = createReader(pmEnv, mediaObject, false);
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
    pMovReader->init();
    return pMovReader;
}
