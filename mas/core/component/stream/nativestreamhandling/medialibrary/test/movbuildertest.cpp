/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "movbuildertest.h"

#include <AtomName.h>

#include <movinfo.h>
#include <movbuilder.h>
#include <movparser.h>
#include <movwriter.h>

#include <movaudiochunk.h>
#include <movaudiochunkcontainer.h>

#include <logger.h>

#include "movfile.h"

#include <mediaobjectwriter.h>

#include "testjniutil.h"
#include "jniutil.h"
#include "testutil.h"

#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>

#include <string>

using namespace std;
using namespace CppUnit;
//using namespace Asserter;

const string INPUT_FILE("test.mov");
const string OUTPUT_FILE("result.mov");

MovBuilderTest::MovBuilderTest():
    mLogger(Logger::getLogger("medialibrary.MediaObjectReaderTest")),
    m_testParser(0),
    m_resultParser(0)
{
    
}

MovBuilderTest::~MovBuilderTest() 
{
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
    m_testParser = createParser(pmEnv, INPUT_FILE.c_str(), 512);
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
    delete m_testParser; 
    delete m_resultParser; 
}

void 
MovBuilderTest::testMovBuilder()
{
    // Parsing MOV file and retrieving the media info
    m_testParser->parse();

    // Retrieving video frames
    MovVideoFrameContainer inputVideoFrames;
    getVideo(*m_testParser, inputVideoFrames);

    // Retrieving audio chunks
    MovAudioChunkContainer inputAudioChunks;
    getAudio(*m_testParser, inputAudioChunks);
    
    // Compiling a MOV file
    MovBuilder builder;
    const MovInfo& info((MovInfo&)builder.getInfo());
    builder.setVideoFrames(inputVideoFrames);
    builder.setAudioChunks(inputAudioChunks);

    // Creating output media object
    jobject jMediaObject(TestUtil::createRecordableMediaObject(pmEnv));
    string contentType("audio/wav");
    string fileExtension("WAV");
    java::MediaObject* mediaObject(new java::MediaObject(pmEnv, jMediaObject));
    TestUtil::setContentTypeAndFileExtension(pmEnv, mediaObject,
					     contentType, fileExtension);

    // Injecting the media object into a created MovWriter object.
    MovWriter writer(mediaObject);
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
    TestUtil::setImmutable(pmEnv, jMediaObject);
    string fileName("result.mov");
    TestUtil::saveAs(pmEnv, jMediaObject, fileName);
    delete mediaObject;

    // Verify that 
    CPPUNIT_ASSERT_MESSAGE("MOV data error", info.check());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of frames",
				 19,
				 info.getFrameCount());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of audio chunks",
				 300,
				 info.getAudioChunkCount());

    // Creating a MOV parser for the resulting MOV file
    m_resultParser = createParser(pmEnv, OUTPUT_FILE.c_str(), 512);
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", 
			   m_resultParser != NULL);

    // Parsing the compiled MOV file
    m_resultParser->parse();

    /*
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Test Audio Start Time Offsets",
                                 (unsigned)4711,
                                 m_resultParser->getAudioStartTimeOffset());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Test Audio Start Time Offsets",
                                 (unsigned)0,
                                 m_testParser->getAudioStartTimeOffset());

    CPPUNIT_ASSERT_EQUAL_MESSAGE("Test Video Start Time Offsets",
                                 (unsigned)42,
                                 m_resultParser->getVideoStartTimeOffset());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Test Video Start Time Offsets",
                                 (unsigned)0,
                                 m_testParser->getVideoStartTimeOffset());
    */
    // Verifying both test and result parser are "equal"
    compare(*m_testParser, *m_resultParser);

    // Crashing a MOV file
    MovFile faultyFile(OUTPUT_FILE.c_str());
    faultyFile.open(MovFile::OPEN_AS_INPUT);
    faultyFile.copyTo("faulty.mov");
    faultyFile.open(MovFile::OPEN_AS_IO);

    CPPUNIT_ASSERT_MESSAGE("No TRAK!", faultyFile.find(quicktime::TRAK));
    faultyFile.seek(4, quicktime::AtomReader::SEEK_BACKWARD);
    faultyFile.writeDW(quicktime::BAJS);
    faultyFile.open(MovFile::OPEN_AS_INPUT);
    CPPUNIT_ASSERT_MESSAGE("No BAJS!", faultyFile.find(quicktime::BAJS));
    
    // Creating a MOV parser for the resulting MOV file
    MovParser* failParser(createParser(pmEnv, "faulty.mov", 512));
    CPPUNIT_ASSERT_MESSAGE("Failed to create MovParser", 
			   failParser != NULL);

    cout << "Parse should fail!" << endl;
    failParser->parse();
}

void
MovBuilderTest::testVideoCodecName()
{
    MovBuilder builder;

    // Verify that proper video codec name sets successfully.
    try {
	builder.setVideoCodec(string("H263"));
    } catch (...) {
	CPPUNIT_FAIL("Failed to set codec");
    }

    // Verify that invalid video codec name throws exception
    bool caughtException(false);
    try {
	builder.setVideoCodec(string("mpeg"));
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
	builder.setAudioCodec(string("PCMU"));
    } catch (...) {
	CPPUNIT_FAIL("Failed to set codec");
    }

    // Verify that invalid audio codec name throws exception
    bool caughtException(false);
    try {
	builder.setAudioCodec(string("AMR"));
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
			    const char* fileName, 
			    jlong bufferSize) 
{
    string contentType("video/quicktime");
    string fileExtension("mov");
    jobject mo = TestUtil::createReadOnlyMediaObject(env, fileName, 
        bufferSize, contentType, fileExtension);
    java::MediaObject* mediaObject = new java::MediaObject(env, mo);
    MovParser* pMovParser = new MovParser(mediaObject);
    CPPUNIT_ASSERT_MESSAGE("Failed to create a MovParser instance",
			   pMovParser != NULL);
    pMovParser->init();
    return pMovParser;
}

java::MediaObject*
MovBuilderTest::createMediaObject()
{
    jobject jMediaObject(TestUtil::createRecordableMediaObject(pmEnv));
    string contentType("audio/wav");
    string fileExtension("WAV");
    java::MediaObject* mediaObject(new java::MediaObject(pmEnv, jMediaObject));
    TestUtil::setContentTypeAndFileExtension(pmEnv, mediaObject,
					     contentType, fileExtension);
    return mediaObject;
}
