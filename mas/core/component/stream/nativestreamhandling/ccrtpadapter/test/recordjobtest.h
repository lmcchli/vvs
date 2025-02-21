/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef RECORDJOBTEST_H_
#define RECORDJOBTEST_H_

#include <jni.h>
#include <cppunit/extensions/HelperMacros.h>

#include "recordjob.h"
#include "logger.h"

/**
 * CPPUnit tests for the RecordJob class.
 * 
 * @author Jörgen Terner
 */ 
class RecordJobTest : public CppUnit::TestFixture  {

    CPPUNIT_TEST_SUITE( RecordJobTest  );
    CPPUNIT_TEST( testRecord );
    CPPUNIT_TEST_SUITE_END();
    
private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;  
    
    jobject createConfig();
    jobject getInstance(const char* method, const char* signature);
    
protected:
    /** Indicates whether the thread is already attached to JVM */
    bool mAlreadyAttached;
        
    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* mEnv; 

    RecordJobTest( const RecordJobTest &x );
    RecordJobTest &operator=(const RecordJobTest &x );

public:
     
    RecordJobTest();
    virtual ~RecordJobTest();

    void setUp();
    void tearDown();
    
    /**
     * 
     */ 
    void testRecord();
};

#endif /*RECORDJOBTEST_H_*/
