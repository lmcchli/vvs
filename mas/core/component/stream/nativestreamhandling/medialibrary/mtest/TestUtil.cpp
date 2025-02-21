/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
 
#include "TestUtil.h"

#include <base_include.h>
#include <stdexcept>
#include <iostream>
#include <sstream>

#include <fcntl.h>
#if defined(WIN32)
#include "io.h"
#else
#include <unistd.h>
#define O_BINARY 0
#endif

#include "java/mediaobject.h"

#include "MockMediaObject.h"
#include "MockMediaObjectNativeAccess.h"
#include "MockByteBuffer.h"

#include <TestMedia.h>
using namespace std;

vector<java::MediaObject*> TestUtil::mMediaObjectVector;
vector<MockMediaObject*> TestUtil::mMockMediaObjectVector;

void
TestUtil::cleanUp()
{
    for (unsigned int i(0); i < TestUtil::mMediaObjectVector.size(); i++) {
        delete TestUtil::mMediaObjectVector[i];
        TestUtil::mMediaObjectVector[i] = 0;
    }
    for (unsigned int i(0); i < TestUtil::mMockMediaObjectVector.size(); i++) {
        delete TestUtil::mMockMediaObjectVector[i];
        TestUtil::mMockMediaObjectVector[i] = 0;
    }
}


java::MediaObject* 
TestUtil::createReadOnlyCCMediaObject(JNIEnv* env, 
                                      struct MediaData md,jint bufSz) 
{
	return createReadOnlyCCMediaObject(env,md.fileName.c_str(),bufSz,md.contentType,md.extension);
}

java::MediaObject* 
TestUtil::createReadOnlyCCMediaObject(JNIEnv* env, 
                                      const char* fileName, 
                                      jint bufferSize, 
                                      base::String& contentTypeStr, 
                                      base::String& fileExtension) 
{

    try {
        MockMediaObject* mockMo = 
            new MockMediaObject(".", fileName, fileExtension, contentTypeStr, bufferSize);
        TestUtil::mMockMediaObjectVector.push_back(mockMo);
        java::MediaObject* mo = new java::MediaObject(env, (jobject)mockMo);
        TestUtil::mMediaObjectVector.push_back(mo);
        return mo;
    }
    catch (exception& e) {
        base::String msg("Unexpected exception when creating CCMediaObject");
        msg += ": ";
        msg += e.what();
        return NULL;
    }
}

jobject
TestUtil::createReadOnlyMediaObject(JNIEnv* env, const char* filename, 
                                    jint bufferSize, base::String& contentTypeStr, base::String& fileExtension) 
{
    MockMediaObject* mockMediaObject = 
        new MockMediaObject(".", filename, fileExtension, contentTypeStr, bufferSize);
    TestUtil::mMockMediaObjectVector.push_back(mockMediaObject);
    return (jobject)mockMediaObject;
}

jobject 
TestUtil::createRecordableMediaObject(JNIEnv* env) 
{
    MockMediaObject* mockMediaObject = 
        new MockMediaObject();
    TestUtil::mMockMediaObjectVector.push_back(mockMediaObject);
    return (jobject)mockMediaObject;
}

char* 
TestUtil::appendDataToMediaObject(JNIEnv* pEnv, jobject mo, size_t bufferSize)
{
    MockMediaObject* mockMediaObject((MockMediaObject*)mo);
    char* buffer = new char[bufferSize];
    MockByteBuffer* byteBuffer = new MockByteBuffer(bufferSize, buffer);
    mockMediaObject->m_size += bufferSize;
    mockMediaObject->m_mockMediaObjectNativeAccess.m_mockByteBuffers.push_back(byteBuffer);

    return (char*)buffer;    
}

void 
TestUtil::setImmutable(JNIEnv* pEnv, jobject mo) {
    MockMediaObject* mockMediaObject((MockMediaObject*)mo);

    mockMediaObject->m_isImmutable = true;
}

void
TestUtil::setContentTypeAndFileExtension(JNIEnv* env, jobject mo,
                                         base::String& contentTypeStr, base::String& fileExtension) 
{
    MockMediaObject* mockMediaObject = (MockMediaObject*)mo;
}

void
TestUtil::setContentTypeAndFileExtension(JNIEnv* env, java::MediaObject* mo,
										 base::String& contentTypeStr, base::String& fileExtension) 
{
    //    mo->setContentType(contentType);
    mo->setFileExtension(fileExtension);
}

void
TestUtil::saveAs(JNIEnv* env, jobject mo, base::String& fileName) 
{
    int fd = open(fileName.c_str(), O_RDWR | O_CREAT | O_BINARY, 00644);

    if (fd > 0) {
        MockMediaObject& mockMo(*((MockMediaObject*)mo));
        MockMediaObjectNativeAccess& data(mockMo.m_mockMediaObjectNativeAccess);
        for (unsigned int i = 0; i < data.m_mockByteBuffers.size(); i++) {
            void* buffer = (void*)data.m_mockByteBuffers[i]->m_buffer;
            int size = data.m_mockByteBuffers[i]->m_limit;
            write(fd, buffer, size);
        }
        close(fd);
    }
}
