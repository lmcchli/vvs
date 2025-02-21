/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MOVPARSERTEST_H_
#define MOVPARSERTEST_H_

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>

#include <logger.h>
#include <memory>

class MovInfo;
class MovParser;
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

class MovParserTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( MovParserTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testParse );
    CPPUNIT_TEST( testMultipleParse );
    CPPUNIT_TEST( testGetVideoFrames );
    CPPUNIT_TEST( testGetAudio );
    CPPUNIT_TEST( testParseError );
//    CPPUNIT_TEST( testBigVideoFrames );
    CPPUNIT_TEST_SUITE_END();      

private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;   
    
    MovParserTest( const MovParserTest &x );
    MovParserTest &operator=(const MovParserTest &x );

    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* pmEnv; 
    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
 
    /**
     * Pointer to tested parser.
     */  
    MovParser* mMovParser;
    java::MediaObject* pmMediaObject; 
    MockMediaObject* pmMockMediaObject; 
    
    /**
     * Validates the MovInfo created by the MovParser::parse method
     * @param movInfo the movinfo to validate
     */ 
    void validateMovInfo(const MovInfo& movInfo) const;
	void createParser(struct MediaData md);

    void printRtpPacket(MovRtpPacket* packet);

public:
     
    MovParserTest();
    virtual ~MovParserTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the MovParser class
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
    void testBigVideoFrames();

};
#endif /*MOVPARSERTEST_H_*/
