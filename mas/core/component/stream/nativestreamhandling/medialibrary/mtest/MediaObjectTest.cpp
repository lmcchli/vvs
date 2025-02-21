/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <MediaObjectTest.h>

#include <MockMediaObject.h>
#include <MockByteBuffer.h>
#include <MockJavaVM.h>

#include <java/mediaobject.h>

#include <iostream>
#include <ostream>
#include <fcntl.h>
#if defined(WIN32)
#include <windows.h>
#include <io.h>
#else
#include <unistd.h>
#endif
#include <algorithm>
#include <iostream>
#include <base_include.h>
#include <sstream>
#include <TestMedia.h>

using std::cout;
using std::endl;
using std::ostringstream;

//#include "testjniutil.h"
#include "jniutil.h"
//#include "testutil.h"

static JNIEnv* g_env = 0;

MediaObjectTest::MediaObjectTest() {

}

MediaObjectTest::~MediaObjectTest() {

}

void MediaObjectTest::setUp() {
g_env = (JNIEnv*)&(MockJavaVM::instance());
}

void MediaObjectTest::tearDown() {
}

void MediaObjectTest::testMediaObject() {
    MockMediaObject mockMediaObject;
	mockMediaObject.m_mockMediaProperties.m_fileExtension = MEDIA_GENERIC_PCMU.extension;
	mockMediaObject.m_mockMediaProperties.m_mockMimeType.m_mimeType = MEDIA_GENERIC_PCMU.contentType;
    mockMediaObject.m_size = 37241;
    mockMediaObject
        .m_mockMediaObjectNativeAccess
        .m_mockByteBuffers.push_back(new MockByteBuffer(123));
    mockMediaObject.m_isImmutable = true;
    java::MediaObject mediaObject(g_env, (jobject)&mockMediaObject);
}

void MediaObjectTest::testReadData() {
    // One buffer
    int bufferSize(100000);

	struct MediaData md = MEDIA_BEEP_PCMU;
	
	std::auto_ptr<MockMediaObject> singleBufferMOMock(md.createMock(100000));
	std::auto_ptr<MockMediaObject> multipleBufferMOMock(md.createMock(500));

    java::MediaObject singleBufferMO(g_env, (jobject)singleBufferMOMock.get());
    java::MediaObject multipleBufferMO(g_env, (jobject)multipleBufferMOMock.get());

    // -------------
    // Read should get one buffer only
    int size(singleBufferMO.getSize());
    const char* data(singleBufferMO.getData());

    // Retreieve expected values ...
    int fileSize(singleBufferMOMock->m_mockMediaObjectNativeAccess.m_fileSize);
    const char* expectedData(singleBufferMOMock->m_mockMediaObjectNativeAccess.m_mockByteBuffers[0]->m_buffer);
    int expectedSize(singleBufferMOMock->m_mockMediaObjectNativeAccess.m_mockByteBuffers[0]->m_limit);

    if (fileSize != singleBufferMO.getTotalSize()) {
        cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
        cout << "Expected: " << fileSize << endl
             << "Got:      " <<  singleBufferMO.getTotalSize() << endl;
    }

    if (expectedSize != singleBufferMO.getSize()) {
        cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
        cout << "Expected: " << expectedSize << endl
             << "Got:      " <<  singleBufferMO.getSize() << endl;
    }

    if (memcmp(data, expectedData, expectedSize) != 0) {
        cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
    }

    // There should be only one buffer (no more left)
    if (singleBufferMO.readNextBuffer()) {
        cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
    }

    // --------------
    int expectedCount(multipleBufferMOMock->m_mockMediaObjectNativeAccess.m_mockByteBuffers.size());
    for (int i(0); i < expectedCount; i++) {
        data = multipleBufferMO.getData();
        size = multipleBufferMO.getSize();
        expectedData = multipleBufferMOMock->m_mockMediaObjectNativeAccess.m_mockByteBuffers[i]->m_buffer;
        expectedSize = multipleBufferMOMock->m_mockMediaObjectNativeAccess.m_mockByteBuffers[i]->m_limit;
        if (data == 0) {
            cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
            break;
        }

        if (size != expectedSize) {
            cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
            break;
        }

        if (memcmp(data, expectedData, expectedSize) != 0) {
            cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
        }
		
        if (i < (expectedCount-1) &&  !multipleBufferMO.readNextBuffer()) {
            cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
            break;
        }
    }

    // There should be only one buffer (no more left)
    if (singleBufferMO.readNextBuffer()) {
        cout << "Error: " << __FILE__ << ":" << __LINE__ << endl;
    }
}

void MediaObjectTest::testWriteData()
{
}
