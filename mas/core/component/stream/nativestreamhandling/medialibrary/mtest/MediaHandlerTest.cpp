#include "MediaHandlerTest.h"

#include "mediahandler.h"
#include "mediaobject.h"
#include "java/mediaobject.h"
#include "rtpblockhandler.h"
#include "MediaValidator.h"
#include "jniutil.h"

#include "MockMediaObject.h"
#include "TestJNIUtil.h"

#include <string>

#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>
#include <TestMedia.h>

using namespace std;
using namespace CppUnit;
 
MediaHandlerTest::MediaHandlerTest()
{
}

MediaHandlerTest::~MediaHandlerTest()
{
}

void
MediaHandlerTest::setUp()
{
    if (TestJNIUtil::setUp() < 0) {
        CPPUNIT_FAIL("Failed to create JavaVM");
    }
    m_alreadyAttached = false;
    if (!JNIUtil::getJavaEnvironment((void**)&m_jniEnv, m_alreadyAttached)) {
        Asserter::fail("Failed to get a reference to Java environment.");
    } 
}

void MediaHandlerTest::tearDown()
{
}

void MediaHandlerTest::testWavMediaHandler()
{
	struct MediaData md = MEDIA_BEEP_PCMU;
 
    // Creating a mocked Java domain MediaObject
	MockMediaObject jMediaObject(md.path, md.fileName, md.extension, md.contentType, 512);
    // Creating a C++ domain MediaObject
    java::MediaObject javaMediaObject(m_jniEnv, (jobject)&jMediaObject);
    // Creating a MediaHandler for the MediaObject
    MediaHandler mediaHandler(javaMediaObject, 20, 20, 0, 1500, 32);
    // Retrieving an RtpBlockHandler
    CPPUNIT_ASSERT(!mediaHandler.isOk());
	mediaHandler.parse(boost::ptr_list<MediaValidator>());
    CPPUNIT_ASSERT(mediaHandler.isOk());
	md.validate(mediaHandler);
    MediaObject* mediaObject(mediaHandler.getMediaObject());
    CPPUNIT_ASSERT(mediaObject != 0);
}

void MediaHandlerTest::testMovMediaHandler()
{
	struct MediaData md = MEDIA_UM_0604_PCMU_MOV;

    // Creating a mocked Java domain MediaObject
	MockMediaObject jMediaObject(md.path, md.fileName, md.extension, md.contentType, 512);
    // Creating a C++ domain MediaObject
    java::MediaObject javaMediaObject(m_jniEnv, (jobject)&jMediaObject);
    // Creating a MediaHandler for the MediaObject
    MediaHandler mediaHandler(javaMediaObject, 20, 20, 0, 1500, 32);
    // Retrieving an RtpBlockHandler
    CPPUNIT_ASSERT(!mediaHandler.isOk());
    mediaHandler.parse(boost::ptr_list<MediaValidator>());
    CPPUNIT_ASSERT(mediaHandler.isOk());

	md.validate(mediaHandler);

	MediaObject* mediaObject(mediaHandler.getMediaObject());
    CPPUNIT_ASSERT(mediaObject != 0);
}