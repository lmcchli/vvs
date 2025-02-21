/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MOVPARSERTEST_H_
#define MOVPARSERTEST_H_

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>
#include <mediaobjectreader.h>

#include "movparser.h"


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
    CPPUNIT_TEST_SUITE_END();      

private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
    /** File of non-move type */
    static const char* ILLEGAL_FILENAME;
    /** Test audio files . */
    static const char* FILENAME;
    static const char* FILENAME2;
    static const char* FILENAME3;
    /** Test audio file. */
    /** Size of test audio file. */
    static const int FILE_SIZE;
    /** The buffer size of each ByteBuffer in the MediaObject */
    static const jlong BUFFER_SIZE; 
    
    
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
    MovParser *pmMovParser;
    
    /**
     *  
     * Creates a MovParser. 
     * @param env The JNI Environment
     * @param fileName The file to wrap.
     * @param bufferSize The size of the buffers the file is sliced in
     */ 
    MovParser* createParser(JNIEnv* env, const char* fileName, jlong bufferSize);
    /**
     * Validates the MovInfo created by the MovParser::parse method
     * @param movInfo the movinfo to validate
     */ 
    void validateMovInfo(const MovInfo& movInfo) const;
     

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

};
#endif /*MOVPARSERTEST_H_*/
