/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _AmrBuilderTest_h_
#define _AmrBuilderTest_h_

#include <amrbuilder.h>
#include <logger.h>

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>

#include <memory>
#include <vector>

class AmrParser;
namespace java { class MediaObject; };
class MockMediaObject;

/**
 * CPPUnit test of the MOV builder.
 *
 * The purpose of the test cases here in is to test building MOV
 * data. Main class here is the MovBuilder. Tho MovBuilder utilizes
 * most of the MOV related classes in the medialibrary.
 *
 * The tests include testing of the classes:
 * - MovBuilder
 * - MovAudioChunk
 * - MovRtpPacket
 * - MovInfo
 * - MovTrackInfo
 * - MovRtpPacketInfo
 * 
 */ 
class AmrBuilderTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( AmrBuilderTest  );
    CPPUNIT_TEST( testAmrBuilder );
    CPPUNIT_TEST( testVideoCodecName );
    CPPUNIT_TEST( testAudioCodecName );
    CPPUNIT_TEST_SUITE_END();      

public:
     
    AmrBuilderTest();
    virtual ~AmrBuilderTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the MovBuilder class
     */ 
    void testAmrBuilder();
    void testVideoCodecName();
    void testAudioCodecName();

 private:
    void getVideo(AmrParser& movParser, MovVideoFrameContainer& videoFrames);
    void getAudio(AmrParser& movParser, MovAudioChunkContainer& audioChunks);
	void generateVideo(int frameCount, MovVideoFrameContainer& videoFrames);
	void generateAudio(int chunkCount, MovAudioChunkContainer& audioChunks);
    void compare(AmrParser& inputParser, AmrParser& outputParser);
    void compare(MovVideoFrameContainer& inputVideoFrames,
		 MovVideoFrameContainer& outputVideoFrames);
    void compare(MovAudioChunkContainer& inputAudioChunks,
		 MovAudioChunkContainer& outputAudioChunks);
    AmrParser* createParser(JNIEnv* env, 
			    struct MediaData md, 
				jlong bufferSize);
    java::MediaObject* createMediaObject();

    AmrBuilderTest( const AmrBuilderTest &x );
    AmrBuilderTest &operator=(const AmrBuilderTest &x );

 private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
    std::vector<AmrParser*> mAmrParserVector;
    std::vector<java::MediaObject*> mMediaObjectVector;
    std::vector<MockMediaObject*> mMockMediaObjectVector;
    
    /**
     * Pointer to the JNI environment.
     */ 
    JNIEnv* pmEnv; 
    /** Indicates whether the thread is already attached to JVM */
    bool alreadyAttached;
 
    /**
     * Parser for the reference MOV file
     */  
    AmrParser* m_testParser;

    /**
     * Parser for the generated MOV file
     */  
    AmrParser* m_resultParser;
};

#endif

