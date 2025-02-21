/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef AMRPARSERTEST_H_
#define AMRPARSERTEST_H_

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>

#include <logger.h>
#include <memory>

class MovInfo;
class AmrParser;
namespace java { class MediaObject; };
class MockMediaObject;

/**
 * CPPUnit test of the MOV parser.
 *
 * The MOV parser consist of MovParser, MovInfo and MovTrackInfo.
 * Since the MOV parser is responsible for the extraction of RTP
 * packages (from MOV data) that feature is also tested here.
 *
 * The tests include testing of the classes:
 * - MovParser
 * - MovInfo
 * - MovTrackInfo
 * - MovRtpPaket
 * - MovAtomId
 * 
 */ 

class MovRtpPacket;

class AmrParserTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( AmrParserTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testParse );
    CPPUNIT_TEST( testMultipleParse );
    CPPUNIT_TEST( testGetVideoFrames );
    CPPUNIT_TEST( testGetAudio );
    CPPUNIT_TEST( testParseError );
    CPPUNIT_TEST_SUITE_END();      

private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
    /** The buffer size of each ByteBuffer in the MediaObject */
    static const jlong BUFFER_SIZE; 
    
    
    AmrParserTest( const AmrParserTest &x );
    AmrParserTest &operator=(const AmrParserTest &x );

    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* pmEnv; 
    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
 
    /**
     * Pointer to tested parser.
     */  
    AmrParser* pmAmrParser;
    java::MediaObject* pmMediaObject; 
    MockMediaObject* pmMockMediaObject; 
    
    /**
     * Validates the MovInfo created by the AmrParser::parse method
     * @param movInfo the movinfo to validate
     */ 
    void validateMovInfo(const MovInfo& movInfo) const;
     

    void printRtpPacket(MovRtpPacket* packet);

public:
     
    AmrParserTest();
    virtual ~AmrParserTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the AmrParser class
     */ 
    void testConstructor();

    /**
     * Testing the methods and procedures involved in MOV parsing. 
     */ 
    void testParse();
    void testMultipleParse();

    void testGetVideoFrames();
    void testGetAudio();

    void testParseError();

};
#endif
