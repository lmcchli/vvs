/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _WavBuilderTest_h_
#define _WavBuilderTest_h_

#include <wavbuilder.h>
#include <logger.h>

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>

#include <memory>
#include <vector>
#include <TestMedia.h>


class WavParser;
namespace java { class MediaObject; };
class MockMediaObject;

/**
 * CPPUnit test of the WAV builder.
 *
 * The purpose of the test cases here in is to test building WAV
 * data. Main class here is the WavBuilder. Tho WavBuilder utilizes
 * most of the WAV related classes in the medialibrary.
 *
 * The tests include testing of the classes:
 * - WavBuilder
 */ 
class WavBuilderTest : public CppUnit::TestFixture {

	CPPUNIT_TEST_SUITE( WavBuilderTest  );
    CPPUNIT_TEST( testPcmaWavBuilder );
    CPPUNIT_TEST( testPcmuWavBuilder );
    CPPUNIT_TEST( testAudioCodecName );
    CPPUNIT_TEST_SUITE_END();      

public:
     
    WavBuilderTest();
    virtual ~WavBuilderTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the WavBuilder class
     */ 
    void testPcmaWavBuilder();
    void testPcmuWavBuilder();
    void testAudioCodecName();

 private:
	 void testWavBuilder(WavParser *,struct MediaData md);
    void getAudio(WavParser& movParser, MovAudioChunkContainer& audioChunks);
    void compare(WavParser& inputParser, WavParser& outputParser);
    void compare(MovAudioChunkContainer& inputAudioChunks,
		 MovAudioChunkContainer& outputAudioChunks);
    WavParser* createParser(JNIEnv* env, 
			    struct MediaData md, 
					jlong bufferSize);


    java::MediaObject* createMediaObject();

    WavBuilderTest( const WavBuilderTest &x );
    WavBuilderTest &operator=(const WavBuilderTest &x );

 private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
    std::vector<WavParser*> mWavParserVector;
    std::vector<java::MediaObject*> mMediaObjectVector;
    std::vector<MockMediaObject*> mMockMediaObjectVector;
    
    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* mEnv; 
    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
 
};

#endif

