/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAOBJECTTEST_H_
#define MEDIAOBJECTTEST_H_

#include <cppunit/extensions/HelperMacros.h>
#include <jni.h>

namespace java { class MediaObject; };

/**
 * CPPUnit tests for the MediaObject class
 * @author Jörgen Terner
 */ 
class MediaObjectTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( MediaObjectTest  );
    CPPUNIT_TEST( testReadData );
    CPPUNIT_TEST( testMediaObject );
    CPPUNIT_TEST_SUITE_END();

private:

    /** Name of Java-class MediaObjectFactory. */
    static const char* MEDIAOBJECTFACTORY_CLASS;
    /** Signature of MediaObjectFactory contructor. */
    static const char* MEDIAOBJECTFACTORY_SIGNATURE;
    /** Name of MediaObjectFactory create-method. */
    static const char* MEDIAOBJECTFACTORY_CREATE;
    /** Signature of MediaObjectFactory create-method. */
    static const char* MEDIAOBJECTFACTORY_CREATE_SIGNATURE;

    MediaObjectTest( const MediaObjectTest &x );
    MediaObjectTest &operator=(const MediaObjectTest &x );
    
    
    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* pmEnv;
    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
    
public:
    MediaObjectTest();
    virtual ~MediaObjectTest();

    virtual void setUp();
    virtual void tearDown();
    
    /**
     * Tests to create a MediaObject instance from an audiofile.
     */
    void testMediaObject();
    
    /**
     * Tests to read data from a MediaObject instance containing an audiofile.
     * <p>
     * <b>One buffer</><br>
     * 1. Creates a MediaObject where the buffer is large enough to hold all data.<br>
     * 2. Read all data from the buffer.<br>
     * Result: <br>
     * Exactly one buffer should be read.<br>
     * <p>
     * <b>Several buffers</><br>
     * 1. Creates a MediaObject where the buffer is not large enough to hold all data.<br>
     * 2. Iterate over the buffers and read all data.<br>
     * Result:<br>
     * The correct number of buffers should be read.<br>
     */
    void testReadData();
	void testWriteData();
};

#endif //MEDIAOBJECTTEST_H_
