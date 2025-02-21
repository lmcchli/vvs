/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _MovAudioChunkContainerTest_h_
#define _MovAudioChunkContainerTest_h_

#include <logger.h>


#include <cppunit/extensions/HelperMacros.h>

class MovAudioChunkContainerTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( MovAudioChunkContainerTest  );
    CPPUNIT_TEST( testRechunkalize );
    CPPUNIT_TEST( testGetNextRechunked );
    CPPUNIT_TEST_SUITE_END();      

 public:     
    MovAudioChunkContainerTest();
    virtual ~MovAudioChunkContainerTest();
  
    void setUp();
    void tearDown();

    void testRechunkalize();
    void testGetNextRechunked();

 private:
    MovAudioChunkContainerTest( const MovAudioChunkContainerTest &x );
    MovAudioChunkContainerTest &operator=(const MovAudioChunkContainerTest &x );

 private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
};

#endif

    
