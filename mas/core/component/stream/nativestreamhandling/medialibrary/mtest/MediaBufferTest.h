/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIABUFFERTEST_H_
#define MEDIABUFFERTEST_H_

#include <cppunit/extensions/HelperMacros.h>
#include <mediabuffer.h>

/**
 * CPPUnit tests for the MediaBuffer class.
 * 
 * @author Mats Egland
 */ 
class MediaBufferTest : public CppUnit::TestFixture  {

    CPPUNIT_TEST_SUITE( MediaBufferTest  );
    CPPUNIT_TEST( testJump );
    CPPUNIT_TEST( testReadW );
    CPPUNIT_TEST( testReadDW );
    CPPUNIT_TEST_SUITE_END();      

private:
    /**
     * The raw buffer that the MediaBuffer wraps.
     */ 
    static const char* BUFFER_CONTENT;
    /**
     * The size of the buffer
     */ 
    static const size_t BUFFER_SIZE;
    
    /**
     * Pointer to the tested MediaBuffer
     */ 
    MediaBuffer *pMediaBuffer;
    
    MediaBufferTest( const MediaBufferTest &x );
    MediaBufferTest &operator=(const MediaBufferTest &x );

public:
     
    MediaBufferTest();
    virtual ~MediaBufferTest();

    void setUp();
    void tearDown();
    
    /**
     * Tests readW 
     */ 
    void testReadW();
    /**
     * Tests readDW
     */ 
    void testReadDW();
    /**
     * Tests jumpForward, jumpBackWard, gotoStart, getCurrentIndex, gotoIndex
     */ 
    void testJump();
    
};

#endif //MEDIABUFFERTEST_H_
