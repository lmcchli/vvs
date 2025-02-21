/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVPARSERTEST_H_
#define WAVPARSERTEST_H_

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>
#include <mediaobjectreader.h>

#include "wavparser.h"


/**
 * CPPUnit tests for the WavParser class. 
 * 
 * @author Mats Egland
 */ 
class WavParserTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( WavParserTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testParse );
    CPPUNIT_TEST( testGetData );
    CPPUNIT_TEST_SUITE_END();      

private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
    /** File of non-wave type */
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
    
    
    WavParserTest( const WavParserTest &x );
    WavParserTest &operator=(const WavParserTest &x );

    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* pmEnv; 
    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
 
    /**
     * Pointer to tested parser.
     */  
    WavParser *pmWavParser;
    
    /**
     *  
     * Creates a WavParser. 
     * @param env The JNI Environment
     * @param fileName The file to wrap.
     * @param bufferSize The size of the buffers the file is sliced in
     */ 
    WavParser* createParser(JNIEnv* env, const char* fileName, jlong bufferSize);
    /**
     * Validates the WavInfo created by the WavParser::parse method
     * @param wavInfo the wavinfo to validate
     */ 
    void validateWavInfo(const WavInfo& wavInfo) const;
     
public:
     
    WavParserTest();
    virtual ~WavParserTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the WavParser class
     */ 
    void testConstructor();
    /**
     * Tests for the parse method. Validates the the WavInfo is fed with values.
     */ 
    void testParse();
    /**
     * Tests the getData method.
     */ 
    void testGetData();
    
   
};
#endif /*WAVPARSERTEST_H_*/
