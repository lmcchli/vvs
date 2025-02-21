/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "AmrBuilderTest.h"

#include <AtomName.h>

#include <movinfo.h>
#include <amrbuilder.h>
#include <amrparser.h>
#include <movwriter.h>

#include "java/mediaobject.h"

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

#include <TestMedia.h>
using namespace std;
using namespace CppUnit;

static const struct MediaData MEDIA_INPUT_3GP = {
 	".",						// Path
	"input",					// File name
	"video/3gp",				// Content type
	"3gp",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,					// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
};
static const struct MediaData MEDIA_OUTPUT_3GP = {
 	".",						// Path
	"result",					// File name
	"video/3gp",				// Content type
	"3gp",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,					// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
};
static const struct MediaData MEDIA_FAULTY_3GP = {
 	".",						// Path
	"faulty",					// File name
	"video/3gp",				// Content type
	"3gp",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,					// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
};
const base::String OUTPUT_FILE("result");

	AmrBuilderTest::AmrBuilderTest():
    mLogger(Logger::getLogger("medialibrary.AmrBuilderTest")),
    m_testParser(0),
    m_resultParser(0)
{
    
}

AmrBuilderTest::~AmrBuilderTest() 
{
    for (unsigned i(0); i < mMockMediaObjectVector.size(); i++) {
        delete mMockMediaObjectVector[i];
        mMockMediaObjectVector[i] = 0;
    }
    for (unsigned i(0); i < mMediaObjectVector.size(); i++) {
        delete mMediaObjectVector[i];
        mMediaObjectVector[i] = 0;
    }
    for (unsigned i(0); i < mAmrParserVector.size(); i++) {
        delete mAmrParserVector[i];
        mAmrParserVector[i] = 0;
    }
}

void 
AmrBuilderTest::setUp() 
{
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&pmEnv, alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
	m_testParser = createParser(pmEnv, MEDIA_INPUT_3GP, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create AmrParser", 
			   m_testParser != NULL);
}

void 
AmrBuilderTest::tearDown() 
{
    TestJNIUtil::tearDown(); 
    if (!alreadyAttached) {
        JNIUtil::DetachCurrentThread();
    }
}

void 
AmrBuilderTest::testAmrBuilder()
{
	unsigned frameCount(19);
	unsigned chunkCount(256);
    // Parsing MOV file and retrieving the media info
	/*
    try {
        m_testParser->parse();
    } catch (...) {
        CPPUNIT_FAIL("Unknown exception during parse.");
    }
    */
    // Retrieving video frames
    MovVideoFrameContainer outputVideoFrames;
    MovVideoFrameContainer inputVideoFrames;
	generateVideo(frameCount, outputVideoFrames);

    // Retrieving audio chunks
    MovAudioChunkContainer outputAudioChunks;
    MovAudioChunkContainer inputAudioChunks;
    generateAudio(chunkCount, outputAudioChunks);
    
    // Compiling a MOV file
    AmrBuilder builder;
    const MovInfo& info((MovInfo&)builder.getInfo());
    builder.setVideoFrames(outputVideoFrames);
    builder.setAudioChunks(outputAudioChunks);

    // Creating output media object
    MockMediaObject mockMediaObject;
    java::MediaObject mediaObject(pmEnv, (jobject)&mockMediaObject);

    // Injecting the media object into a created MovWriter object.
    MovWriter writer(&mediaObject);
    // Opening the objects for write
    writer.open();
    // Storing MOV information though the writer
    MovInfo movInfo;
    //    builder.setAudioStartTimeOffset(4711);
    //    builder.setVideoStartTimeOffset(42);
    builder.store(writer);

    // Closing the writer
    writer.close();
    // Performing some magic stuff ...
    mockMediaObject.m_isImmutable = true;
    base::String fileName("result.3gp");
    TestUtil::saveAs(pmEnv, (jobject)&mockMediaObject, fileName);
	
    // Verify that 
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong size!", 
				 chunkCount,
				 outputAudioChunks.size());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong size!", 
				 frameCount,
				 outputVideoFrames.size());
//    CPPUNIT_ASSERT_MESSAGE("MOV data error", info.check());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of frames",
				 frameCount,
				 (unsigned)info.getFrameCount());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of audio chunks",
				 chunkCount,
				 (unsigned)info.getAudioChunkCount());

    // Creating a MOV parser for the resulting MOV file
	m_resultParser = createParser(pmEnv, MEDIA_OUTPUT_3GP, 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create AmrParser", 
			   m_resultParser != NULL);

    // Parsing the compiled MOV file
    m_resultParser->parse();
    getVideo(*m_resultParser, inputVideoFrames);
    getAudio(*m_resultParser, inputAudioChunks);

	compare(outputVideoFrames, inputVideoFrames);
	compare(outputAudioChunks, inputAudioChunks);

    // Verifying both test and result parser are "equal"
    //compare(*m_testParser, *m_resultParser);

    // Crashing a MOV file
	base::String filename(MEDIA_OUTPUT_3GP.fileName);
    filename += ".3gp";
    MovFile faultyFile(filename.c_str());
    faultyFile.open(MovFile::OPEN_AS_INPUT);
    faultyFile.copyTo("faulty.3gp");
    faultyFile.open(MovFile::OPEN_AS_IO);

    CPPUNIT_ASSERT_MESSAGE("No TRAK!", faultyFile.find(quicktime::TRAK));
    faultyFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    faultyFile.writeDW(quicktime::BAJS);
    faultyFile.open(MovFile::OPEN_AS_INPUT);
    CPPUNIT_ASSERT_MESSAGE("No BAJS!", faultyFile.find(quicktime::BAJS));
    faultyFile.close();
    
    // Creating a MOV parser for the resulting MOV file
	AmrParser* failParser(createParser(pmEnv, MEDIA_FAULTY_3GP, 512));
    CPPUNIT_ASSERT_MESSAGE("Failed to create AmrParser", 
			   failParser != NULL);

    failParser->parse();
	// TODO: verify that parse failed ...
}

void
AmrBuilderTest::testVideoCodecName()
{
    AmrBuilder builder;

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
AmrBuilderTest::testAudioCodecName()
{
    AmrBuilder builder;

    // Verify that proper audio codec name sets successfully.
    try {
		builder.setAudioCodec(base::String("AMR"));
    } catch (...) {
		CPPUNIT_FAIL("Failed to set codec");
    }

    // Verify that invalid audio codec name throws exception
    bool caughtException(false);
    try {
		builder.setAudioCodec(base::String("PCMU"));
    } catch (...) { 
		caughtException = true;
    }
    CPPUNIT_ASSERT_MESSAGE("No exception!", caughtException);

}

void
AmrBuilderTest::getVideo(AmrParser& movParser, 
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
AmrBuilderTest::getAudio(AmrParser& movParser, 
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
AmrBuilderTest::generateVideo(int frameCount, 
							  MovVideoFrameContainer& videoFrames)
{
	const char CONTENT[] = "headVideo #";
	const int LENGTH = strlen(CONTENT)+5+1;

	for (int i(0); i < frameCount; i++) {
		boost::ptr_list<MovRtpPacket>* frame = new boost::ptr_list<MovRtpPacket>;
		MovRtpPacket* packet(new MovRtpPacket());
		// TODO: fix this leak
		char* data(new char[LENGTH]);
		sprintf(data, "%s%05d", CONTENT, i);
		packet->setData(data);
		packet->setLength(LENGTH);
		packet->setFrameTime(i);
		frame->push_back(packet);
		videoFrames.push_back(frame);
	}
}

void 
AmrBuilderTest::generateAudio(int chunkCount, 
							  MovAudioChunkContainer& audioChunks)
{
	const char CONTENT[] = "Audio #";
	const int LENGTH = strlen(CONTENT)+5+1;

	for (int i(0); i < chunkCount; i++) {
		char* data(new char[LENGTH]);
		sprintf(data, "%s%05d", CONTENT, i);
		MovAudioChunk* chunk(new MovAudioChunk(data, LENGTH));
		audioChunks.push_back(chunk);
	}
}

void 
AmrBuilderTest::compare(AmrParser& inputParser, AmrParser& outputParser)
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
AmrBuilderTest::compare(MovVideoFrameContainer& inputVideoFrames,
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
AmrBuilderTest::compare(MovAudioChunkContainer& inputAudioChunks,
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

AmrParser* 
AmrBuilderTest::createParser(JNIEnv* env, 
			    struct MediaData md, 
			    jlong bufferSize) 
{
	MockMediaObject* mockMediaObject = md.createMock(bufferSize);
    java::MediaObject* mediaObject = new java::MediaObject(env, (jobject)mockMediaObject);
    AmrParser* pAmrParser = new AmrParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a AmrParser instance",
			   pAmrParser != NULL);

    mMockMediaObjectVector.push_back(mockMediaObject);
    mMediaObjectVector.push_back(mediaObject);
    mAmrParserVector.push_back(pAmrParser);
    pAmrParser->init();
    return pAmrParser;
}

java::MediaObject*
AmrBuilderTest::createMediaObject()
{
    MockMediaObject* mockMediaObject = 
		new MockMediaObject("", "", MEDIA_TEST_3GP.extension, MEDIA_TEST_3GP.contentType, 512); 
    mMockMediaObjectVector.push_back(mockMediaObject);

    java::MediaObject* mediaObject = new java::MediaObject(pmEnv, (jobject)mockMediaObject);
    
    mMediaObjectVector.push_back(mediaObject);
    
    return mediaObject;
}
