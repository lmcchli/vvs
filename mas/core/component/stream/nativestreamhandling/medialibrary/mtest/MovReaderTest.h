/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MOVREADERTEST_H_
#define MOVREADERTEST_H_
#include <cppunit/extensions/HelperMacros.h>
#include <movreader.h>

#include "MediaObjectReaderTest.h"
#include "logger.h"

namespace java { class MediaObject; };
/**
 * CPPUnit tests for the MovReader class. Extends the MediaObjectReaderTest
 * class which takes care of the creation of the reader, setup, tear down etc.
 *
 * The objective is to test the features which are provided by the 
 * MovReader class:
 * - getAtomInformation();
 * And the MediaObjectReader features seek() and tell().
 * All other features are assumed to be tested elsewhere.
 * 
 */ 
class MovReaderTest : public MediaObjectReaderTest  {

    CPPUNIT_TEST_SUITE( MovReaderTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testMovReaderPcmu );
    CPPUNIT_TEST( testMovReaderPcma );
    CPPUNIT_TEST_SUITE_END();      

private:
    
    MovReaderTest( const MovReaderTest &x );
    MovReaderTest &operator=(const MovReaderTest &x );
    void testMovReader(struct MediaData);
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;
    std::vector<MovReader*> mMovReaderVector;

protected:
    /**
     * Overrides super class implementation to return a MovReader.
     * Creates a MovReader. 
     *
     * @param env The JNI Environment
     * @param mo The MediaObject to read from
     * @param swap Whether bytes should be swapped due to little/big endian 
     *        differences.
     */ 
    virtual MovReader* createReader(JNIEnv* env, java::MediaObject* mo, bool swap);

    /**
     * Returns the reader used in the tests. Overrides the getReader method
     * in base class to return reader of type MovReader (the cast is made 
     * in the method).
     *
     * @return The reader used in the tests
     */ 
    virtual MovReader* getReader(int index);

public:
    MovReaderTest();
    virtual ~MovReaderTest();
  
    /**
     * Tests the constructor and destructor of the MovReader class
     */ 
    void testConstructor();
    /**
     * Combined test for getAtomInformation(), tell() and seek()
     */
    void testMovReaderPcmu();
    void testMovReaderPcma();
};

#endif /*MOVREADERTEST_H_*/
