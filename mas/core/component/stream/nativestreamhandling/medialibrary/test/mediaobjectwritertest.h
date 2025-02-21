/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAOBJECTWRITERTEST_H_
#define MEDIAOBJECTWRITERTEST_H_

#include <jni.h>
#include <cppunit/extensions/HelperMacros.h>
#include <mediaobjectreader.h>

#include "logger.h"
#include "mediaobject.h"

/**
 * CPPUnit tests for the MediaObjectWriter class.
 * 
 * @author Jörgen Terner
 */ 
class MediaObjectWriterTest : public CppUnit::TestFixture  {

    CPPUNIT_TEST_SUITE( MediaObjectWriterTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testWrite );
    CPPUNIT_TEST_SUITE_END();
    
private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;  
    
protected:
    /** Test audio file. */
    static const char* FILENAME;
    /** Size of test audio file. */
    static const int FILE_SIZE;

    /** Indicates whether the thread is already attached to JVM */
    bool mAlreadyAttached;
        
    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* mEnv; 

    MediaObjectWriterTest( const MediaObjectWriterTest &x );
    MediaObjectWriterTest &operator=(const MediaObjectWriterTest &x );

public:
     
    MediaObjectWriterTest();
    virtual ~MediaObjectWriterTest();

    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor, i.e. test creation of a writer. 
     */ 
    void testConstructor();
    
    /**
     * Writes a data to a media object using a writer instance.
     */
    void testWrite();
};

#endif /*MEDIAOBJECTWRITERTEST_H_*/
