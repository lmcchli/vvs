/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef WAVPARSERTEST_H_
#define WAVPARSERTEST_H_

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>
#include <mediaobjectreader.h>

#include "wavparser.h"

#include <vector>

class MockMediaObject;
namespace java { class MediaObject; };
class WavParser;


/**
 * CPPUnit tests for the WavParser class. 
 * 
 * @author Mats Egland
 */ 
class WavParserTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( WavParserTest  );
    CPPUNIT_TEST( testConstructor );
    CPPUNIT_TEST( testPcmuParse );
    CPPUNIT_TEST( testPcmaParse );
    CPPUNIT_TEST( testPcmuGetData );
    CPPUNIT_TEST( testPcmaGetData );
    CPPUNIT_TEST_SUITE_END();      

private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger;     
    
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
    std::vector<WavParser*> mWavParserVector;
    std::vector<java::MediaObject*> mMediaObjectVector;
    std::vector<MockMediaObject*> mMockMediaObjectVector;
    
    /**
     *  
     * Creates a WavParser. 
     * @param env The JNI Environment
     * @param mediaData The media data descriptor for the file
     * @param bufferSize The size of the buffers the file is sliced in
     */ 
    WavParser* createParser(JNIEnv* env, struct MediaData & mediaData, jlong bufferSize);
    
	void testParse(const MediaData testMedia[],int testMediaCount);     
	void testGetData(const MediaData testMedia[],int testMediaCount);     
	int offsetToFirstDataByte(MediaData & md,int & chunkSize);
	int offsetToFirstDataByte(MediaData & md);

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
    void testPcmuParse();
    void testPcmaParse();
    /**
     * Tests the getData method.
     */ 
    void testPcmuGetData();
    void testPcmaGetData();
    
   
};
#endif /*WAVPARSERTEST_H_*/
