/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAOBJECTREADERTEST_H_
#define MEDIAOBJECTREADERTEST_H_

#include <jni.h>
#include <cppunit/extensions/HelperMacros.h>
#include <mediaobjectreader.h>

#include "logger.h"

#include <vector>

namespace java { class MediaObject; };
class MockMediaObject;

/**
 * CPPUnit tests for the MediaObjectReader class.
 * 
 * @author Mats Egland
 */ 
class MediaObjectReaderTest : public CppUnit::TestFixture  {

    CPPUNIT_TEST_SUITE( MediaObjectReaderTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testJumpForward );
    CPPUNIT_TEST( testJumpBackward );
    CPPUNIT_TEST( testReset );
    CPPUNIT_TEST( testReadW );
    CPPUNIT_TEST( testReadDW );
    CPPUNIT_TEST( testCompareStr );
    CPPUNIT_TEST( testMark );
    CPPUNIT_TEST( testGetData );
    
    CPPUNIT_TEST( testGetTotalSize );
    //    CPPUNIT_TEST( testPerformance );
    //    CPPUNIT_TEST( testMemoryPerformance );
    CPPUNIT_TEST_SUITE_END();      
private:
    
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;  
    
protected:
    /** The number of readers in test */
    static const int NR_OF_READERS = 4;
    /** Array of BUFFER_SIZES used in the MediaObject */
    static unsigned BUFFER_SIZE_ARRAY[];
    
    /**
     * Array of create readers used in the tests.
     */  
    MediaObjectReader* mMediaObjectReaderArray[NR_OF_READERS];
    MockMediaObject* mMockMediaObjectArray[NR_OF_READERS];
    java::MediaObject* mMediaObjectArray[NR_OF_READERS];
    std::vector<MediaObjectReader*> mMediaObjectReaderVector;

    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
        
    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* pmEnv; 
    /**
     * Creates the MediaObjectReaders used in the test. 
     * @param env The JNI Environment
     * @param mo The MediaObject to read from
     */ 
    virtual MediaObjectReader* createReader(JNIEnv* env, java::MediaObject* mo);
        
    /**
     * Returns the MediaObjectReader created with index 
     *  
     */ 
    virtual MediaObjectReader* getReader(int index);
    /**
     * Sets the reader with index that is used in the tests
     */ 
    void setReader(MediaObjectReader* reader, int index);
    /**
     * Performs some benchmarks test on performance. 
     */ 
    void benchmark();
    
    
    MediaObjectReaderTest( const MediaObjectReaderTest &x );
    MediaObjectReaderTest &operator=(const MediaObjectReaderTest &x );

public:
     
    MediaObjectReaderTest();
    virtual ~MediaObjectReaderTest();

    void setUp();
    void tearDown();
    
    /**
     * Tests the performance of standalone MediaBuffers. 
     * Prints result to stdout.
     */ 
    void testMemoryPerformance();
    /**
     * Test the performance of the reader. 
     * Benchmarks against reading a file
     * direct from disk. 
     */ 
    void testPerformance();
    
    /**
     * Tests the constructor, i.e. test creation of a reader. 
     */ 
    void testConstructor();
    /**
     * Test for the reset method.
     */ 
    void testReset();
    /**
     * test for the compareStr method.
     */ 
    void testCompareStr();
    /**
     * Tests the following methods:
     *   getCurrentLocation()
     *   jumpForward(nrOfBytes)
     *   jumpBackward(nrOfBytes)
     *   bytesLeft()
     *   bytesRead()
     * 
     *   Tests the methods by reading a file with both a MediaObjectReader
     *   object and a ifstream to compare input. 
     */ 
    void testJumpForward();
    /**
     * Tests the following methods:
     *   getCurrentLocation()
     *   jumpForward(nrOfBytes)
     *   jumpBackward(nrOfBytes)
     *   bytesLeft()
     *   bytesRead()
     * 
     *   Tests the methods by reading a file with both a MediaObjectReader
     *   object and a ifstream to compare input. 
     */ 
    void testJumpBackward();
    /**
     * Test for the readW method
     */ 
    void testReadW();
    /**
     * Test for the readDW method
     */ 
    void testReadDW();
    /**
     * Test for the mark method
     */ 
    void testMark();
    /**
     * Tests the destructor
     */ 
    void testDestructor();
    /**
     * Test for the getData method.
     */ 
    void testGetData();
    /**
     * Test for the getTotolSize method.
     */ 
    void testGetTotalSize();
    
};
#endif /*MEDIAOBJECTREADERTEST_H_*/
