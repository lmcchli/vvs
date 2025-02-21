/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "MovBuilderTest.h"

#include <AtomName.h>

#include <movinfo.h>
#include <movbuilder.h>
#include <movparser.h>
#include <movwriter.h>
#include <java/mediaobject.h>

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

#include <TestMedia.h>

static struct MediaData LOCAL_MEDIA_RESULT_PCMU_MOV = {
	".",						// Path
	"mb_result_pcmu",				// File name
	"video/quicktime",				// Content type
	"mov",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ULAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
};
static struct MediaData LOCAL_MEDIA_FAULTY_PCMU_MOV = {
	".",						// Path
	"mb_faulty_pcmu",				// File name
	"video/quicktime",				// Content type
	"mov",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ULAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
};


MovBuilderTest::MovBuilderTest():
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")),
    m_testParser(0),
    m_resultParser(0)
{
    
}

MovBuilderTest::~MovBuilderTest() 
{
    for (unsigned int i(0); i < mMockMediaObjectVector.size(); i++) {
        delete mMockMediaObjectVector[i];
        mMockMediaObjectVector[i] = 0;
    }
    for (unsigned int i(0); i < mMediaObjectVector.size(); i++) {
        delete mMediaObjectVector[i];
        mMediaObjectVector[i] = 0;
    }
    for (unsigned int i(0); i < mMovParserVector.size(); i++) {
        delete mMovParserVector[i];
        mMovParserVector[i] = 0;
    }
}

void 
MovBuilderTest::setUp() 
{
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
	m_testParser = createParser(pmEnv, MEDIA_TEST_PCMU_MOV, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", 
			   m_testParser != NULL);
}

void 
MovBuilderTest::tearDown() 
{
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
}

void 
MovBuilderTest::testMovBuilder()
{
    // Parsing MOV file and retrieving the media info
    try {
        m_testParser->parse();
    } catch (...) {
        CPPUNIT_FAIL("Unknown exception during parse.");
    }
    
    // Retrieving video frames
    MovVideoFrameContainer inputVideoFrames;
    getVideo(*m_testParser, inputVideoFrames);

    // Retrieving audio chunks
    MovAudioChunkContainer inputAudioChunks;
    getAudio(*m_testParser, inputAudioChunks);
    
    // Compiling a MOV file
    MovBuilder builder;
    const MovInfo& info((MovInfo&)builder.getInfo());
	builder.setAudioCodec(MEDIA_TEST_PCMU_MOV.getAudioCodec());
    builder.setVideoFrames(inputVideoFrames);
    builder.setAudioChunks(inputAudioChunks);

    // Creating output media object
    MockMediaObject mockMediaObject;
    java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);

    // Injecting the media object into a created MovWriter object.
    MovWriter writer(&mediaObject);
    // Opening the objects for write
    writer.open();
    // Storing MOV information though the writer
	builder.store(writer);
    // Closing the writer
    writer.close();
    // Performing some magic stuff ...
    mockMediaObject.m_isImmutable = true;
	TestUtil::saveAs(pmEnv, (jobject)&mockMediaObject, LOCAL_MEDIA_RESULT_PCMU_MOV.getCanonicalFilename());
	
    // Verify that 
    CPPUNIT_ASSERT_MESSAGE("MOV data error", info.check());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of frames",
				 19,
				 info.getFrameCount());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of audio chunks",
				 300,
				 info.getAudioChunkCount());

    // Creating a MOV parser for the resulting MOV file
	m_resultParser = createParser(pmEnv, LOCAL_MEDIA_RESULT_PCMU_MOV, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", 
			   m_resultParser != NULL);

    // Parsing the compiled MOV file
    m_resultParser->parse();


    // Verifying both test and result parser are "equal"
    compare(*m_testParser, *m_resultParser);

    // Crashing a MOV file
	MovFile faultyFile(LOCAL_MEDIA_RESULT_PCMU_MOV.getCanonicalFilename().c_str());
    faultyFile.open(MovFile::OPEN_AS_INPUT);
    faultyFile.copyTo(LOCAL_MEDIA_FAULTY_PCMU_MOV.getCanonicalFilename().c_str());
    faultyFile.open(MovFile::OPEN_AS_IO);

    CPPUNIT_ASSERT_MESSAGE("No TRAK!", faultyFile.find(quicktime::TRAK));
    faultyFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    faultyFile.writeDW(quicktime::BAJS);
    faultyFile.open(MovFile::OPEN_AS_INPUT);
    CPPUNIT_ASSERT_MESSAGE("No BAJS!", faultyFile.find(quicktime::BAJS));
    faultyFile.close();
    
    // Creating a MOV parser for the resulting MOV file
    MovParser* failParser(createParser(pmEnv,LOCAL_MEDIA_FAULTY_PCMU_MOV, 512));
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", 
			   failParser != NULL);

    failParser->parse();
	// TODO: verify that parse failed ...
}

void
MovBuilderTest::testVideoCodecName()
{
    MovBuilder builder;

    // Verify that proper video codec name sets successfully.
    try {
	builder.setVideoCodec(base::String("H263"));
    } catch (...) {
	CPPUNIT_FAIL("Failed to set codec");
    }

    // Verify that invalid video codec name throws exception
    bool caughtException(false);
    try {
	builder.setVideoCodec(base::String("mpeg"));
    } catch (...) {
	caughtException = true;
    }
    CPPUNIT_ASSERT_MESSAGE("No exception!", caughtException);
}

void
MovBuilderTest::testAudioCodecName()
{
    MovBuilder builder;

    // Verify that proper audio codec name sets successfully.
    try {
	builder.setAudioCodec(base::String("PCMU"));
    } catch (...) {
	CPPUNIT_FAIL("Failed to set codec");
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
MovBuilderTest::getVideo(MovParser& movParser, 
			 MovVideoFrameContainer& videoFrames)
{
    MovInfo& inputMOV((MovInfo&)movParser.getMediaInfo());

    for (int i(0); i < inputMOV.getFrameCount(); i++) {
        boost::ptr_list<MovRtpPacket>* rtpPackets(new boost::ptr_list<MovRtpPacket>);
	movParser.getFrame(*rtpPackets, i);
	videoFrames.push_back(rtpPackets);
    }

}

void
MovBuilderTest::getAudio(MovParser& movParser, 
			 MovAudioChunkContainer& audioChunks)
{
    MovInfo& inputMOV((MovInfo&)movParser.getMediaInfo());

    for (int i(0); i < inputMOV.getAudioChunkCount(); i++) {
	unsigned length;
	const unsigned char* buf(movParser.getAudioChunk(length, i));
	char* data(new char[length]);
	memcpy(data, buf, length);
	audioChunks.push_back(new MovAudioChunk(data, length));
    }
    
}

void 
MovBuilderTest::compare(MovParser& inputParser, MovParser& outputParser)
{
    // Retrieving video frames
    MovVideoFrameContainer inputVideoFrames;
    getVideo(inputParser, inputVideoFrames);
    MovVideoFrameContainer outputVideoFrames;
    getVideo(outputParser, outputVideoFrames);

    // Comparing video
    compare(inputVideoFrames, outputVideoFrames);

    // Retrieving audio chunks
    MovAudioChunkContainer inputAudioChunks;
    getAudio(inputParser, inputAudioChunks);
    MovAudioChunkContainer outputAudioChunks;
    getAudio(outputParser, outputAudioChunks);

    // Comparing audio
    compare(inputAudioChunks, outputAudioChunks);
}

void
MovBuilderTest::compare(MovVideoFrameContainer& inputVideoFrames,
			MovVideoFrameContainer& outputVideoFrames)
{
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong size!", 
				 inputVideoFrames.size(),
				 outputVideoFrames.size());

    MovVideoFrameContainer::iterator inputVideoIter(inputVideoFrames.begin());
    MovVideoFrameContainer::iterator outputVideoIter(outputVideoFrames.begin());
    for (; inputVideoIter != inputVideoFrames.end(); ++inputVideoIter, ++outputVideoIter) {
        boost::ptr_list<MovRtpPacket>& inputPackets(*inputVideoIter);
        boost::ptr_list<MovRtpPacket>& outputPackets(*outputVideoIter);

        boost::ptr_list<MovRtpPacket>::iterator inputPktIter = inputPackets.begin();
        boost::ptr_list<MovRtpPacket>::iterator outputPktIter = outputPackets.begin();
        CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong size!", 
                                     inputPackets.size(),
                                     outputPackets.size());
        for (; inputPktIter != inputPackets.end(); ++inputPktIter, ++outputPktIter) {
	    MovRtpPacket& inputPacket(*inputPktIter);
	    MovRtpPacket& outputPacket(*outputPktIter);
	    CPPUNIT_ASSERT_MESSAGE("Bad package!", 
				   inputPacket == outputPacket);
	}
    }
}

void
MovBuilderTest::compare(MovAudioChunkContainer& inputAudioChunks,
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

MovParser* 
MovBuilderTest::createParser(JNIEnv* env, 
			    MediaData & md, 
			    jlong bufferSize) 
{
	MockMediaObject* mockMediaObject =	md.createMock((int)bufferSize);
    java::MediaObject* mediaObject = new java::MediaObject(env, (jobject)mockMediaObject);
    MovParser* pMovParser = new MovParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a MovParser instance",
			   pMovParser != NULL);

    mMockMediaObjectVector.push_back(mockMediaObject);
    mMediaObjectVector.push_back(mediaObject);
    mMovParserVector.push_back(pMovParser);
    pMovParser->init();
    return pMovParser;
}

java::MediaObject*
MovBuilderTest::createMediaObject()
{
    MockMediaObject* mockMediaObject = 
		new MockMediaObject("", "", MEDIA_TEST_PCMU.extension, MEDIA_TEST_PCMU.contentType, 512); 
    mMockMediaObjectVector.push_back(mockMediaObject);

    java::MediaObject* mediaObject = new java::MediaObject(pmEnv, (jobject)mockMediaObject);
    
    mMediaObjectVector.push_back(mediaObject);
    
    return mediaObject;
}
