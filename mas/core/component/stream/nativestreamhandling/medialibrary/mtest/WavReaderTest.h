/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVREADERTEST_H_
#define WAVREADERTEST_H_
#include <cppunit/extensions/HelperMacros.h>
#include <wavreader.h>

#include "MediaObjectReaderTest.h"
#include "java/mediaobject.h"
#include "logger.h"

#include <vector>

/**
 * CPPUnit tests for the WavReader class. Extends the MediaObjectReaderTest
 * class which takes care of the creation of the reader, setup, tear down etc.
 * 
 * @author Mats Egland
 */ 
class WavReaderTest : public MediaObjectReaderTest  {

    CPPUNIT_TEST_SUITE( WavReaderTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testGetRiffSize );
    CPPUNIT_TEST( testGetChunkId );
    CPPUNIT_TEST( testCompareChunkId );
    CPPUNIT_TEST( testGetChunkSize );
    CPPUNIT_TEST( testNextChunk );
    CPPUNIT_TEST( testSeekChunk );
    CPPUNIT_TEST_SUITE_END();      

private:
    
    WavReaderTest( const WavReaderTest &x );
    WavReaderTest &operator=(const WavReaderTest &x );
    
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;  

    std::vector<WavReader*> mWavReaderVector;

protected:
    /**
     * Overrides super class implementation to return a WavReader.
     * Creates a WavReader. 
     * @param env The JNI Environment
     * @param mo The MediaObject to read from
     */ 
    virtual WavReader* createReader(JNIEnv* env, java::MediaObject* mo);
    /**
     * Returns the reader used in the tests. Overrides the getReader method
     * in base class to return reader of type WavReader (the cast is made in the method).
     * @return The reader used in the tests
     */ 
    virtual WavReader* getReader(int index);
public:
     
    WavReaderTest();
    virtual ~WavReaderTest();
  
    /**
     * Tests the constructor and destructor of the WavReader class
     */ 
    void testConstructor();
    
    /**
     * Test for the getRiffSize method.
     */ 
    void testGetRiffSize();
    
    /**
     * Test for the getChunkId method
     */ 
    void testGetChunkId();
    /**
     * Test for the compareChunkId method
     */ 
    void testCompareChunkId();
    /**
     * Test for the void getChunkSize(uint32_t &chunkSize);
     */ 
    void testGetChunkSize();
    /**
     * Test for the nextChunk() method
     */ 
    void testNextChunk();
    /**
     * Test for bool seekChunk(const char *chunkId, bool ignoreCase);
     */
    void testSeekChunk();
};

#endif /*WAVREADERTEST_H_*/
