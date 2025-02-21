/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _MovBuilderTest_h_
#define _MovBuilderTest_h_

#include <movbuilder.h>
#include <logger.h>

#include <jni.h>

#include <cppunit/extensions/HelperMacros.h>

#include <memory>
#include <vector>
#include <TestMedia.h>

class MovParser;
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
class MovBuilderTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( MovBuilderTest  );
    CPPUNIT_TEST( testMovBuilder );
    CPPUNIT_TEST( testVideoCodecName );
    CPPUNIT_TEST( testAudioCodecName );
    CPPUNIT_TEST_SUITE_END();      

public:
     
    MovBuilderTest();
    virtual ~MovBuilderTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Tests the constructor and destructor of the MovBuilder class
     */ 
    void testMovBuilder();
    void testVideoCodecName();
    void testAudioCodecName();

 private:
    void getVideo(MovParser& movParser, MovVideoFrameContainer& videoFrames);
    void getAudio(MovParser& movParser, MovAudioChunkContainer& audioChunks);
    void compare(MovParser& inputParser, MovParser& outputParser);
    void compare(MovVideoFrameContainer& inputVideoFrames,
		 MovVideoFrameContainer& outputVideoFrames);
    void compare(MovAudioChunkContainer& inputAudioChunks,
		 MovAudioChunkContainer& outputAudioChunks);
    MovParser* createParser(JNIEnv* env, 
			    MediaData & md, 
			    jlong bufferSize);


    java::MediaObject* createMediaObject();

    MovBuilderTest( const MovBuilderTest &x );
    MovBuilderTest &operator=(const MovBuilderTest &x );

 private:
    /**
     * The logger used for this class.
     */ 
    std::auto_ptr<Logger> mLogger; 
    std::vector<MovParser*> mMovParserVector;
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
    MovParser* m_testParser;

    /**
     * Parser for the generated MOV file
     */  
    MovParser* m_resultParser;
};

#endif

