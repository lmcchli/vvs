/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "WavBuilderTest.h"

#include "java/mediaobject.h"

#include <AtomName.h>

#include <wavinfo.h>
#include <wavbuilder.h>
#include <wavparser.h>
//#include <wavwriter.h>

#include <movaudiochunk.h>
#include <movaudiochunkcontainer.h>

#include <logger.h>

#include "MovFile.h"

#include <mediaobjectwriter.h>

#include "TestJNIUtil.h"
#include "TestUtil.h"
#include "jniutil.h"
#include "MockMediaObject.h"

#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>

#include <base_include.h>

using namespace std;
using namespace CppUnit;
//using namespace Asserter;

static struct MediaData LOCAL_MEDIA_RESULT_PCMU = MEDIA_GENERIC_PCMU.as("result_pcmu");
static struct MediaData LOCAL_MEDIA_RESULT_PCMA = MEDIA_GENERIC_PCMA.as("result_pcma");

WavBuilderTest::WavBuilderTest():
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest"))
 {
    
}


WavBuilderTest::~WavBuilderTest() 
{
    for (unsigned i(0); i < mMockMediaObjectVector.size(); i++) {
        delete mMockMediaObjectVector[i];
        mMockMediaObjectVector[i] = 0;
    }
    for (unsigned i(0); i < mMediaObjectVector.size(); i++) {
        delete mMediaObjectVector[i];
        mMediaObjectVector[i] = 0;
    }
    for (unsigned i(0); i < mWavParserVector.size(); i++) {
        delete mWavParserVector[i];
        mWavParserVector[i] = 0;
    }
}

void 
WavBuilderTest::setUp() 
{
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&mEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
}

void 
WavBuilderTest::tearDown() 
{
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
}

void 
WavBuilderTest::testPcmaWavBuilder()
{
	WavParser* pcma = createParser(mEnv, MEDIA_TEST_PCMA, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create WavParser", 
			   pcma != NULL);
	testWavBuilder(pcma,LOCAL_MEDIA_RESULT_PCMA);
}
void 
WavBuilderTest::testPcmuWavBuilder()
{

	WavParser* pcmu = createParser(mEnv, MEDIA_TEST_PCMU, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create WavParser", 
			   pcmu != NULL);
	testWavBuilder(pcmu,LOCAL_MEDIA_RESULT_PCMU);
}

void 
WavBuilderTest::testWavBuilder(WavParser * parser,struct MediaData md)
{
    // Parsing WAV file and retrieving the media info
    try {
		parser->parse();
    } catch (...) {
        CPPUNIT_FAIL("Unknown exception during parse.");
    }
    
    // Retrieving audio chunks
    MovAudioChunkContainer inputAudioChunks;
    getAudio(*parser, inputAudioChunks);
    
    // Compiling a WAV file
    WavBuilder builder;
    const WavInfo& info((WavInfo&)builder.getInfo());
    builder.setAudioChunks(inputAudioChunks);
	builder.setAudioCodec(parser->getAudioCodec());


    // Creating output media object
    MockMediaObject mockMediaObject;
    java::MediaObject mediaObject(mEnv, (jobject)&mockMediaObject);

    // Injecting the media object into a created WavWriter object.
    MediaObjectWriter writer(&mediaObject);
    // Opening the objects for write
    writer.open();
    // Storing WAV information though the writer
    WavInfo wavInfo;
    //    builder.setAudioStartTimeOffset(4711);
    //    builder.setVideoStartTimeOffset(42);
    builder.store(writer);

    // Closing the writer
    writer.close();
    // Performing some magic stuff ...
    mockMediaObject.m_isImmutable = true;

	TestUtil::saveAs(
		mEnv, 
		(jobject)&mockMediaObject, 
		MockMediaObjectNativeAccess::mediaFilePath(md.path,md.fileName,md.extension)
		);
	

	// Creating a WAV parser for the resulting WAV file
    WavParser * resultParser = createParser(mEnv, md, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create result WavParser", 
			   resultParser != NULL);

    // Parsing the compiled WAV file
    resultParser->parse();

    // Verifying both test and result parser are "equal"
    compare(*parser, *resultParser);

    // TODO: add negative test ... illegal file etc.
}

void
WavBuilderTest::testAudioCodecName()
{
    WavBuilder builder;

    // Verify that proper audio codec name sets successfully.
    try {
	builder.setAudioCodec(base::String("PCMU"));
    } catch (...) {
	CPPUNIT_FAIL("Failed to set codec to PCMU");
    }

	// Verify that proper audio codec name sets successfully.
    try {
	builder.setAudioCodec(base::String("PCMA"));
    } catch (...) {
	CPPUNIT_FAIL("Failed to set codec to PCMA");
    }

    // Verify that invalid audio codec name throws exception
    bool caughtException(false);
    try {
	builder.setAudioCodec(base::String("AMR"));
    } catch (...) { 
	caughtException = true;
    }
    CPPUNIT_ASSERT_MESSAGE("No exception!", caughtException);

}

void
WavBuilderTest::getAudio(WavParser& wavParser, 
			 MovAudioChunkContainer& audioChunks)
{
    WavInfo& inputWAV((WavInfo&)wavParser.getMediaInfo());
	// The data chunk size is the size of the entire chunk of audio in the wav file
    int chunkCount = 
        inputWAV.getDataChunkSize()/160;

	for (int i(0); i < chunkCount; i++) {
	unsigned length;
	const unsigned char* buf(wavParser.getAudioChunk(length, i));
	char* data(new char[length]);
	memcpy(data, buf, length);
	audioChunks.push_back(new MovAudioChunk(data, length));
    }
    
}

void 
WavBuilderTest::compare(WavParser& inputParser, WavParser& outputParser)
{
    // Retrieving audio chunks
    MovAudioChunkContainer inputAudioChunks;
    getAudio(inputParser, inputAudioChunks);
    MovAudioChunkContainer outputAudioChunks;
    getAudio(outputParser, outputAudioChunks);

    // Comparing audio
    compare(inputAudioChunks, outputAudioChunks);
}

void
WavBuilderTest::compare(MovAudioChunkContainer& inputAudioChunks,
			MovAudioChunkContainer& outputAudioChunks)
{
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong size!", 
				 inputAudioChunks.size(),
				 outputAudioChunks.size());
    
    int i(0);
    MovAudioChunkContainer::iterator inAudioIter(inputAudioChunks.begin());
    MovAudioChunkContainer::iterator outAudioIter(outputAudioChunks.begin());
    for (; inAudioIter != inputAudioChunks.end(); ++inAudioIter, ++outAudioIter) {
	MovAudioChunk& inputAudioChunk(*inAudioIter);
	MovAudioChunk& outputAudioChunk(*outAudioIter);
        char message[80];
        std::sprintf(message, "Chunk #%d (of %d) (size %d : %d)", 
                     ++i, inputAudioChunks.size(),
                     inputAudioChunk.getLength(),
                     outputAudioChunk.getLength());
	CPPUNIT_ASSERT_MESSAGE(message,
			       inputAudioChunk == outputAudioChunk);
    }
}

WavParser* 
WavBuilderTest::createParser(JNIEnv* env, 
			    struct MediaData md, 
			    jlong bufferSize) 
{
	MockMediaObject* mockMediaObject = md.createMock((int)bufferSize);
    java::MediaObject* mediaObject = new java::MediaObject(env, (jobject)mockMediaObject);
    WavParser* pWavParser = new WavParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a WavParser instance",
			   pWavParser != NULL);

    mMockMediaObjectVector.push_back(mockMediaObject);
    mMediaObjectVector.push_back(mediaObject);
    mWavParserVector.push_back(pWavParser);
    pWavParser->init();
    pWavParser->setPTime(20);
    return pWavParser;
}

java::MediaObject*
WavBuilderTest::createMediaObject()
{
    MockMediaObject* mockMediaObject = 
		new MockMediaObject("", "", LOCAL_MEDIA_RESULT_PCMU.extension, LOCAL_MEDIA_RESULT_PCMU.contentType, 512); 
    mMockMediaObjectVector.push_back(mockMediaObject);

    java::MediaObject* mediaObject = new java::MediaObject(mEnv, (jobject)mockMediaObject);
    
    mMediaObjectVector.push_back(mediaObject);
    
    return mediaObject;
}
