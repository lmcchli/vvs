/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
 
#include "testutil.h"

#include <string>
#include <stdexcept>
#include <iostream>
#include <sstream>

#include "jniutil.h"

using namespace std;

const char* MEDIAOBJECTFACTORY_CLASS = 
    "com/mobeon/masp/mediaobject/factory/MediaObjectFactory";
static const char* MEDIAOBJECT_NATIVE_ACCESS_CLASSNAME = "MediaObjectNativeAccess";
static const char* GET_NATIVE_ACCESS_METHOD = "getNativeAccess";
static const char* GET_NATIVE_ACCESS_METHOD_SIGNATURE = 
    "()Lcom/mobeon/masp/mediaobject/MediaObjectNativeAccess;";
const char* MEDIAOBJECTFACTORY_SIGNATURE = "()V";
const char* MEDIAOBJECTFACTORY_SIGNATURE_BUFFERSIZE = "(I)V";
const char* MEDIAOBJECT_CLASS = 
    "com/mobeon/masp/mediaobject/IMediaObject";
const char* APPEND_METHOD = "append";
const char* APPEND_METHOD_SIGNATURE = "(I)Ljava/nio/ByteBuffer;";

const char* MEDIAOBJECTFACTORY_CREATE_FILE = "create";
const char* MEDIAOBJECTFACTORY_CREATE_FILE_SIGNATURE = 
    "(Ljava/io/File;)Lcom/mobeon/masp/mediaobject/IMediaObject;";
    
const char* MEDIAOBJECTFACTORY_CREATE = "create";
const char* MEDIAOBJECTFACTORY_CREATE_SIGNATURE = 
    "()Lcom/mobeon/masp/mediaobject/IMediaObject;";

const char* MEDIAOBJECTFACTORY_SET_BUFFER_SIZE = "setBufferSize";
const char* MEDIAOBJECTFACTORY_SET_BUFFER_SIZE_SIGNATURE = "(I)V";

static const char* SET_IMMUTABLE_METHOD = "setImmutable";
static const char* SET_IMMUTABLE_METHOD_SIGNATURE = "()V";

static const char* GET_MEDIA_PROPERTIES_METHOD = "getMediaProperties";
static const char* GET_MEDIA_PROPERTIES_METHOD_SIGNATURE = 
    "()Lcom/mobeon/masp/mediaobject/MediaProperties;";

static const char* MEDIA_PROPERTIES_CLASSNAME = "MediaProperties";
static const char* SET_CONTENT_TYPE_METHOD = "setContentType";
static const char* SET_CONTENT_TYPE_METHOD_SIGNATURE = 
    "(Ljakarta/activation/MimeType;)V";
static const char* SET_FILE_EXTENSION_METHOD = "setFileExtension";
static const char* SET_FILE_EXTENSION_METHOD_SIGNATURE = 
    "(Ljava/lang/String;)V";

java::MediaObject* 
TestUtil::createReadOnlyCCMediaObject(JNIEnv* env, 
        const char* filename, jint bufferSize, 
        string& contentTypeStr, string& fileExtension) {

    try {
        jobject mo = 
            TestUtil::createReadOnlyMediaObject(env, filename, bufferSize,
                contentTypeStr, fileExtension);
        return new java::MediaObject(env, mo);
    }
    catch (exception& e) {
        string msg("Unexpected exception when creating CCMediaObject");
        msg += ": ";
        msg += e.what();
        return NULL;
    }
}
jobject
TestUtil::createReadOnlyMediaObject(JNIEnv* env, const char* filename, 
        jint bufferSize, string& contentTypeStr, string& fileExtension) {

    try {
        // Create MediaObjectFactory
        jclass moFactoryCls = env->FindClass(MEDIAOBJECTFACTORY_CLASS);
        string msg("Could not find class ");
        msg += MEDIAOBJECTFACTORY_CLASS;
        if (moFactoryCls == NULL) {
            cerr << msg;
            return NULL;
        }
        jmethodID cId = JNIUtil::getJavaMethodID(env, moFactoryCls, 
                "<init>", MEDIAOBJECTFACTORY_SIGNATURE, true); 
        jobject mediaObjectFactory = env->NewObject(moFactoryCls, cId);
        
        //File f = new File(file);
        jclass cls = env->FindClass("java/io/File");
        if (cls == NULL) {
            cerr << "Could not find class java/io/File";
            return NULL;
        }
        cId = JNIUtil::getJavaMethodID(env, cls, 
            "<init>", "(Ljava/lang/String;)V", true);
        jstring fileNameString = env->NewStringUTF(filename);
        jobject file = env->NewObject(cls, cId, fileNameString);
        
        //IMediaObject mObj = factory.create(f, "wav");
        jstring wavString = env->NewStringUTF("wav");
        jmethodID setbuffersizeId = JNIUtil::getJavaMethodID(env, moFactoryCls, 
            MEDIAOBJECTFACTORY_SET_BUFFER_SIZE, 
            MEDIAOBJECTFACTORY_SET_BUFFER_SIZE_SIGNATURE, true);
        jmethodID createId = JNIUtil::getJavaMethodID(env, moFactoryCls, 
            MEDIAOBJECTFACTORY_CREATE, MEDIAOBJECTFACTORY_CREATE_FILE_SIGNATURE, true);
        if (setbuffersizeId == NULL) {
            cerr << "Could not find method id for setBufferSize";
            return NULL;
        }
         
        env->CallLongMethod(mediaObjectFactory, setbuffersizeId, bufferSize);
        jobject mediaObject = env->CallObjectMethod(mediaObjectFactory, createId, file, wavString);        
        JNIUtil::checkException(env, MEDIAOBJECTFACTORY_CREATE_FILE, true);
        
        TestUtil::setContentTypeAndFileExtension(env, mediaObject, 
            contentTypeStr, fileExtension);
        return mediaObject;
    } catch (exception& e) {
        string msg("Unexpected exception when creating MediaObject: ");
        msg += MEDIAOBJECTFACTORY_CLASS;
        msg += ": ";
        msg += e.what();
        cerr << msg << endl;
        return NULL;
    }
}

jobject 
TestUtil::createRecordableMediaObject(JNIEnv* env) {
    try {
        // Create MediaObjectFactory
        jclass moFactoryCls = env->FindClass(MEDIAOBJECTFACTORY_CLASS);
        string msg("Could not find class ");
        msg += MEDIAOBJECTFACTORY_CLASS;
        if (moFactoryCls == NULL) {
            cerr << msg;
            return NULL;
        }
        
        jmethodID cId = JNIUtil::getJavaMethodID(env, moFactoryCls, 
                "<init>", MEDIAOBJECTFACTORY_SIGNATURE, true);
        jobject mediaObjectFactory = env->NewObject(moFactoryCls, cId);

        jmethodID createId = JNIUtil::getJavaMethodID(env, moFactoryCls, 
            MEDIAOBJECTFACTORY_CREATE, MEDIAOBJECTFACTORY_CREATE_SIGNATURE, true);
         
        jobject mediaObject = env->CallObjectMethod(mediaObjectFactory, createId);        
        JNIUtil::checkException(env, MEDIAOBJECTFACTORY_CREATE, true);
        return mediaObject;
    }
    catch (exception& e) {
        string msg("Unexpected exception when creating empty MediaObject: ");
        msg += MEDIAOBJECTFACTORY_CLASS;
        msg += ": ";
        msg += e.what();
        cerr << msg << endl;
        return NULL;
    }    
}

char* 
TestUtil::appendDataToMediaObject(JNIEnv* pEnv, jobject mo, size_t bufferSize) throw (std::logic_error) {
    // Get the method for append
    jclass moCls = pEnv->FindClass(MEDIAOBJECT_CLASS);
    if (moCls == NULL) {
        throw logic_error("Could not find class for mediaobject");
    }
    // Perform getNativeAccess
    jmethodID mid = JNIUtil::getJavaMethodID(pEnv, moCls,
                GET_NATIVE_ACCESS_METHOD, GET_NATIVE_ACCESS_METHOD_SIGNATURE,
                true);
    jobject nativeAccess = pEnv->CallObjectMethod(mo, mid);    
    JNIUtil::checkException(pEnv, GET_NATIVE_ACCESS_METHOD,
        true);
    // MediaObjectNativeAccess class
    jclass nativeAccessCls = 
        JNIUtil::getJavaClass(pEnv, nativeAccess, MEDIAOBJECT_NATIVE_ACCESS_CLASSNAME, 
            true); 
    
    jmethodID appendId = JNIUtil::getJavaMethodID(pEnv, nativeAccessCls, 
        APPEND_METHOD, APPEND_METHOD_SIGNATURE, true);
    if (appendId == NULL) {
        throw logic_error("Could not find id for method append ");
    }
    // Call append method
    jobject byteBuffer(pEnv->CallObjectMethod(
        nativeAccess, appendId, bufferSize));
    
    return (char*)pEnv->GetDirectBufferAddress(byteBuffer);    
}

void 
TestUtil::setImmutable(JNIEnv* pEnv, jobject mo) {
    jclass moCls = pEnv->FindClass(MEDIAOBJECT_CLASS);
    if (moCls == NULL) {
        throw logic_error("Could not find class for mediaobject");
    }

    // Call setImmutable
    jmethodID mid = JNIUtil::getJavaMethodID(pEnv, moCls,
        SET_IMMUTABLE_METHOD, SET_IMMUTABLE_METHOD_SIGNATURE, true);
    pEnv->CallVoidMethod(mo, mid);    
    
    JNIUtil::checkException(pEnv, SET_IMMUTABLE_METHOD, true);
}

void
TestUtil::setContentTypeAndFileExtension(JNIEnv* env, jobject mo,
    string& contentTypeStr, string& fileExtension) {
    jclass cls = env->FindClass("jakarta/activation/MimeType");
    if (cls == NULL) {
        throw logic_error("Could not find class jakarta/activation/MimeType");
    }
    jmethodID cId = JNIUtil::getJavaMethodID(env, cls, 
        "<init>", "(Ljava/lang/String;)V", false);
    jstring jContentTypeStr = env->NewStringUTF(contentTypeStr.c_str());
    jobject contentType = env->NewObject(cls, cId, jContentTypeStr);
    JNIUtil::checkException(env, "jakarta/activation/MimeType::<init>", false);
    
    jclass mediaObjectCls = 
        JNIUtil::getJavaClass(env, mo, MEDIAOBJECT_CLASS, false);
    
    // getMediaProperties
    jmethodID mid = JNIUtil::getJavaMethodID(env, mediaObjectCls,
        GET_MEDIA_PROPERTIES_METHOD, GET_MEDIA_PROPERTIES_METHOD_SIGNATURE,
        false);
    jobject mediaProp = env->CallObjectMethod(mo, mid);    
    JNIUtil::checkException(env, GET_MEDIA_PROPERTIES_METHOD, false);

    // Call methods on the MediaProperties instance
    jclass mediaPropCls = JNIUtil::getJavaClass(env, mediaProp, 
        MEDIA_PROPERTIES_CLASSNAME, false);

    // Call setContentType
    jmethodID mediaPropMid = JNIUtil::getJavaMethodID(env, mediaPropCls,
        SET_CONTENT_TYPE_METHOD, SET_CONTENT_TYPE_METHOD_SIGNATURE, false);
    env->CallVoidMethod(mediaProp, mediaPropMid, contentType);    
    JNIUtil::checkException(env, SET_CONTENT_TYPE_METHOD, false);
    
    // Call setFileExtension
    jstring fileExtString(env->NewStringUTF(fileExtension.c_str()));
    mediaPropMid = JNIUtil::getJavaMethodID(env, mediaPropCls,
        SET_FILE_EXTENSION_METHOD, SET_FILE_EXTENSION_METHOD_SIGNATURE, false);
    env->CallVoidMethod(mediaProp, mediaPropMid, fileExtString);    
    JNIUtil::checkException(env, SET_FILE_EXTENSION_METHOD, false);
}

void
TestUtil::setContentTypeAndFileExtension(JNIEnv* env, java::MediaObject* mo,
    string& contentTypeStr, string& fileExtension) {
    jclass cls = env->FindClass("jakarta/activation/MimeType");
    if (cls == NULL) {
        throw logic_error("Could not find class jakarta/activation/MimeType");
    }
    jmethodID cId = JNIUtil::getJavaMethodID(env, cls, 
        "<init>", "(Ljava/lang/String;)V", true);
    jstring jContentTypeStr = env->NewStringUTF(contentTypeStr.c_str());
    jobject contentType = env->NewObject(cls, cId, jContentTypeStr);
    JNIUtil::checkException(env, "jakarta/activation/MimeType::<init>", true);
    
    mo->setContentType(contentType);
    mo->setFileExtension(fileExtension);
}

void
TestUtil::saveAs(JNIEnv* env, jobject mo, string& fileName) {

    // Now, the file is saved using Java classes. It might be
    // cleaner and easier to use C++ code but it is nice to use
    // the interface method getInputStream in IMediaObject when
    // this is the way it will be read by the application.    
    
    jstring fileNameStr = env->NewStringUTF(fileName.c_str());
    
    jclass cls = JNIUtil::getJavaClass(env, mo, MEDIAOBJECT_CLASS, true);
    jmethodID mid = JNIUtil::getJavaMethodID(env, cls, 
        "getInputStream", "()Ljava/io/InputStream;", true);
    jobject is = env->CallObjectMethod(mo, mid);
    JNIUtil::checkException(env, "getInputStream", true);
    
    cls = env->FindClass("java/io/File");
    if (cls == NULL) {
        throw logic_error("Failed to find class java/io/File");
    }
    mid = JNIUtil::getJavaMethodID(env, cls, 
        "<init>", "(Ljava/lang/String;)V", true);
    jobject file = env->NewObject(cls, mid, fileNameStr);
    JNIUtil::checkException(env, "java/io/File::init", true);
    
    jclass osCls = env->FindClass("java/io/FileOutputStream");
    if (osCls == NULL) {
        throw logic_error("Failed to find class java/io/FileOutputStream");
    }
    mid = JNIUtil::getJavaMethodID(env, osCls, 
        "<init>", "(Ljava/io/File;)V", true);
    jobject os = env->NewObject(osCls, mid, file);
    JNIUtil::checkException(env, "java/io/FileOutputStream::init", true);
    
    jclass isCls = JNIUtil::getJavaClass(env, is, 
        "java/io/InputStream", true);
    jmethodID writeMid = JNIUtil::getJavaMethodID(env, osCls, 
        "write", "(I)V", true);
    jmethodID readMid = JNIUtil::getJavaMethodID(env, isCls, 
        "read", "()I", true);

    int b(-1);
    while ((b = env->CallIntMethod(is, readMid)) != -1) {
        JNIUtil::checkException(env, "java/io/InputStream::read", true);
        env->CallVoidMethod(os, writeMid, b);
        JNIUtil::checkException(env, "java/io/FileOutputStream::write", true);
    }
    
    mid = JNIUtil::getJavaMethodID(env, osCls, "close", "()V", true);
    env->CallVoidMethod(os, mid);
    JNIUtil::checkException(env, "java/io/FileOutputStream::close", true);
    
    mid = JNIUtil::getJavaMethodID(env, isCls, "close", "()V", true);
    env->CallVoidMethod(is, mid);
    JNIUtil::checkException(env, "java/io/FileInputStream::close", true);
}
