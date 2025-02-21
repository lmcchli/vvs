#ifndef _MediaHandlerTest_h_
#define _MediaHandlerTest_h_

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>

class MediaHandlerTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( MediaHandlerTest  );
    CPPUNIT_TEST( testWavMediaHandler );
    CPPUNIT_TEST( testMovMediaHandler );
    CPPUNIT_TEST_SUITE_END();      

public:
     
    MediaHandlerTest();
    virtual ~MediaHandlerTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the MovBuilder class
     */ 
    void testWavMediaHandler();
    void testMovMediaHandler();

    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* m_jniEnv; 
    /** Indicates whether the thread is already attached to JVM */
    bool m_alreadyAttached;
};

#endif
