#include <stdexcept>
#include <iostream>

#include "jnimediaobject.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIMediaObject";

const char* JNIMediaObject::MEDIAOBJECT_INTERFACE_CLASSNAME = "com/mobeon/masp/mediaobject/IMediaObject";
const char* JNIMediaObject::GET_NATIVE_ACCESS_METHOD = "getNativeAccess";
const char* JNIMediaObject::GET_NATIVE_ACCESS_METHOD_SIGNATURE =
        "()Lcom/mobeon/masp/mediaobject/MediaObjectNativeAccess;";
jmethodID JNIMediaObject::getNativeAccessMID;
const char* JNIMediaObject::GET_MEDIA_PROPERTIES_METHOD = "getMediaProperties";
const char* JNIMediaObject::GET_MEDIA_PROPERTIES_METHOD_SIGNATURE = "()Lcom/mobeon/masp/mediaobject/MediaProperties;";
jmethodID JNIMediaObject::getMediaPropertiesMID;
const char* JNIMediaObject::GET_SIZE_METHOD = "getSize";
const char* JNIMediaObject::GET_SIZE_METHOD_SIGNATURE = "()J";
jmethodID JNIMediaObject::getSizeMID;
const char* JNIMediaObject::IS_IMMUTABLE_METHOD = "isImmutable";
const char* JNIMediaObject::IS_IMMUTABLE_METHOD_SIGNATURE = "()Z";
jmethodID JNIMediaObject::isImmutableMID;
const char* JNIMediaObject::SET_IMMUTABLE_METHOD = "setImmutable";
const char* JNIMediaObject::SET_IMMUTABLE_METHOD_SIGNATURE = "()V";
jmethodID JNIMediaObject::setImmutableMID;

const char* JNIMediaObject::MEDIA_PROPERTIES_CLASSNAME = "com/mobeon/masp/mediaobject/MediaProperties";
const char* JNIMediaObject::ADD_LENGTH_METHOD = "addLength";
const char* JNIMediaObject::ADD_LENGTH_METHOD_SIGNATURE = "(Lcom/mobeon/masp/mediaobject/MediaLength;)V";
jmethodID JNIMediaObject::addLengthMID;
const char* JNIMediaObject::SET_CONTENT_TYPE_METHOD = "setContentType";
const char* JNIMediaObject::SET_CONTENT_TYPE_METHOD_SIGNATURE = "(Ljakarta/activation/MimeType;)V";
jmethodID JNIMediaObject::setContentTypeMID;
const char* JNIMediaObject::SET_FILE_EXTENSION_METHOD = "setFileExtension";
const char* JNIMediaObject::SET_FILE_EXTENSION_METHOD_SIGNATURE = "(Ljava/lang/String;)V";
jmethodID JNIMediaObject::setFileExtensionMID;
const char* JNIMediaObject::GET_FILE_EXTENSION_METHOD = "getFileExtension";
const char* JNIMediaObject::GET_FILE_EXTENSION_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIMediaObject::getFileExtensionMID;
const char* JNIMediaObject::GET_CONTENT_TYPE_METHOD = "getContentType";
const char* JNIMediaObject::GET_CONTENT_TYPE_METHOD_SIGNATURE = "()Ljakarta/activation/MimeType;";
jmethodID JNIMediaObject::getContentTypeMID;

const char* JNIMediaObject::LENGTHUNIT_CLASSNAME = "com/mobeon/masp/mediaobject/MediaLength$LengthUnit";
const char* JNIMediaObject::LENGTHUNIT_MILLISECONDS_NAME = "MILLISECONDS";
const char* JNIMediaObject::LENGTHUNIT_MILLISECONDS_NAME_SIGNATURE =
        "Lcom/mobeon/masp/mediaobject/MediaLength$LengthUnit;";
jclass JNIMediaObject::mLengthUnitCls;
jfieldID JNIMediaObject::millisecondsFID;

const char* JNIMediaObject::MEDIALENGTH_CLASSNAME = "com/mobeon/masp/mediaobject/MediaLength";
const char* JNIMediaObject::MEDIALENGTH_CONSTRUCTOR_NAME = "<init>";
const char* JNIMediaObject::MEDIALENGTH_CONSTRUCTOR_NAME_SIGNATURE =
        "(Lcom/mobeon/masp/mediaobject/MediaLength$LengthUnit;J)V";
jclass JNIMediaObject::mediaLengthCls;
jmethodID JNIMediaObject::initMID;

const char* JNIMediaObject::MIMETYPE_CLASSNAME = "jakarta/activation/MimeType";
const char* JNIMediaObject::TO_STRING_METHOD = "toString";
const char* JNIMediaObject::TO_STRING_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIMediaObject::toStringMID;

const char* JNIMediaObject::MEDIAOBJECT_NATIVE_ACCESS_CLASSNAME = "com/mobeon/masp/mediaobject/MediaObjectNativeAccess";
const char* JNIMediaObject::APPEND_METHOD = "append";
const char* JNIMediaObject::APPEND_METHOD_SIGNATURE = "(I)Ljava/nio/ByteBuffer;";
jmethodID JNIMediaObject::appendMID;
const char* JNIMediaObject::ITERATOR_METHOD = "iterator";
const char* JNIMediaObject::ITERATOR_METHOD_SIGNATURE = "()Lcom/mobeon/masp/mediaobject/IMediaObjectIterator;";
jmethodID JNIMediaObject::iteratorMID;

const char* JNIMediaObject::ITERATOR_CLASSNAME = "com/mobeon/masp/mediaobject/IMediaObjectIterator";
const char* JNIMediaObject::HAS_NEXT_METHOD = "hasNext";
const char* JNIMediaObject::HAS_NEXT_METHOD_SIGNATURE = "()Z";
jmethodID JNIMediaObject::hasNextMID;
const char* JNIMediaObject::NEXT_METHOD = "next";
const char* JNIMediaObject::NEXT_METHOD_SIGNATURE = "()Ljava/nio/ByteBuffer;";
jmethodID JNIMediaObject::nextMID;

const char* JNIMediaObject::BUFFER_CLASSNAME = "java/nio/Buffer";
const char* JNIMediaObject::LIMIT_METHOD = "limit";
const char* JNIMediaObject::LIMIT_METHOD_SIGNATURE = "()I";
jmethodID JNIMediaObject::limitMID;

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_MediaObjectOnLoad(void *reserved)
{
    JNIMediaObject::MediaObjectOnLoad(reserved);
}

void JNIMediaObject::MediaObjectOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // MediaObject - MID's
    jclass moIFClass = (jclass) env->NewGlobalRef(env->FindClass(MEDIAOBJECT_INTERFACE_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", MEDIAOBJECT_INTERFACE_CLASSNAME);
        abort();
    }

    getMediaPropertiesMID = env->GetMethodID(moIFClass, GET_MEDIA_PROPERTIES_METHOD,
            GET_MEDIA_PROPERTIES_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", GET_MEDIA_PROPERTIES_METHOD,
                GET_MEDIA_PROPERTIES_METHOD_SIGNATURE);
        abort();
    }

    getNativeAccessMID = env->GetMethodID(moIFClass, GET_NATIVE_ACCESS_METHOD, GET_NATIVE_ACCESS_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", GET_NATIVE_ACCESS_METHOD,
                GET_NATIVE_ACCESS_METHOD_SIGNATURE);
        abort();
    }

    getSizeMID = env->GetMethodID(moIFClass, GET_SIZE_METHOD, GET_SIZE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", GET_SIZE_METHOD,
                GET_SIZE_METHOD_SIGNATURE);
        abort();
    }

    isImmutableMID = env->GetMethodID(moIFClass, IS_IMMUTABLE_METHOD, IS_IMMUTABLE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", IS_IMMUTABLE_METHOD,
                IS_IMMUTABLE_METHOD_SIGNATURE);
        abort();
    }

    setImmutableMID = env->GetMethodID(moIFClass, SET_IMMUTABLE_METHOD, SET_IMMUTABLE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", SET_IMMUTABLE_METHOD,
                SET_IMMUTABLE_METHOD_SIGNATURE);
        abort();
    }

    // MediaProperties - MID's
    jclass mpClass = (jclass) env->NewGlobalRef(env->FindClass(MEDIA_PROPERTIES_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", MEDIA_PROPERTIES_CLASSNAME);
        abort();
    }

    addLengthMID = env->GetMethodID(mpClass, ADD_LENGTH_METHOD, ADD_LENGTH_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", ADD_LENGTH_METHOD,
                ADD_LENGTH_METHOD_SIGNATURE);
        abort();
    }

    setContentTypeMID = env->GetMethodID(mpClass, SET_CONTENT_TYPE_METHOD, SET_CONTENT_TYPE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", SET_CONTENT_TYPE_METHOD,
                SET_CONTENT_TYPE_METHOD_SIGNATURE);
        abort();
    }

    getContentTypeMID = env->GetMethodID(mpClass, GET_CONTENT_TYPE_METHOD, GET_CONTENT_TYPE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", GET_CONTENT_TYPE_METHOD,
                GET_CONTENT_TYPE_METHOD_SIGNATURE);
        abort();
    }

    setFileExtensionMID = env->GetMethodID(mpClass, SET_FILE_EXTENSION_METHOD, SET_FILE_EXTENSION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", SET_FILE_EXTENSION_METHOD,
                SET_FILE_EXTENSION_METHOD_SIGNATURE);
        abort();
    }

    getFileExtensionMID = env->GetMethodID(mpClass, GET_FILE_EXTENSION_METHOD, GET_FILE_EXTENSION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", GET_FILE_EXTENSION_METHOD,
                GET_FILE_EXTENSION_METHOD_SIGNATURE);
        abort();
    }

    // MediaLength$LengthUnit FID's
    mLengthUnitCls = (jclass) env->NewGlobalRef(env->FindClass(LENGTHUNIT_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", mLengthUnitCls);
        abort();
    }

    millisecondsFID = env->GetStaticFieldID(mLengthUnitCls, LENGTHUNIT_MILLISECONDS_NAME,
            LENGTHUNIT_MILLISECONDS_NAME_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", LENGTHUNIT_MILLISECONDS_NAME,
                LENGTHUNIT_MILLISECONDS_NAME_SIGNATURE);
        abort();
    }

    // MediaLength MID's
    mediaLengthCls = (jclass) env->NewGlobalRef(env->FindClass(MEDIALENGTH_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", mediaLengthCls);
        abort();
    }

    initMID = env->GetMethodID(mediaLengthCls, MEDIALENGTH_CONSTRUCTOR_NAME, MEDIALENGTH_CONSTRUCTOR_NAME_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", MEDIALENGTH_CONSTRUCTOR_NAME,
                MEDIALENGTH_CONSTRUCTOR_NAME_SIGNATURE);
        abort();
    }

    // MimeType - MID's
    jclass mimeCls = (jclass) env->NewGlobalRef(env->FindClass(MIMETYPE_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", mimeCls);
        abort();
    }

    toStringMID = env->GetMethodID(mimeCls, TO_STRING_METHOD, TO_STRING_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", TO_STRING_METHOD,
                TO_STRING_METHOD_SIGNATURE);
        abort();
    }

    // MediaObjectNativeAccess - MID's
    jclass nativeAccessCls = (jclass) env->NewGlobalRef(env->FindClass(MEDIAOBJECT_NATIVE_ACCESS_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", nativeAccessCls);
        abort();
    }

    appendMID = env->GetMethodID(nativeAccessCls, APPEND_METHOD, APPEND_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", APPEND_METHOD, APPEND_METHOD_SIGNATURE);
        abort();
    }

    iteratorMID = env->GetMethodID(nativeAccessCls, ITERATOR_METHOD, ITERATOR_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", ITERATOR_METHOD,
                ITERATOR_METHOD_SIGNATURE);
        abort();
    }

    jclass iterMoIFClass = (jclass) env->NewGlobalRef(env->FindClass(ITERATOR_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", iterMoIFClass);
        abort();
    }

    hasNextMID = env->GetMethodID(iterMoIFClass, HAS_NEXT_METHOD, HAS_NEXT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", HAS_NEXT_METHOD,
                HAS_NEXT_METHOD_SIGNATURE);
        abort();
    }

    nextMID = env->GetMethodID(iterMoIFClass, NEXT_METHOD, NEXT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", NEXT_METHOD, NEXT_METHOD_SIGNATURE);
        abort();
    }

    jclass nioBufferClass = (jclass) env->NewGlobalRef(env->FindClass(BUFFER_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", nioBufferClass);
        abort();
    }

    limitMID = env->GetMethodID(nioBufferClass, LIMIT_METHOD, LIMIT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s from %s", LIMIT_METHOD, LIMIT_METHOD_SIGNATURE);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(moIFClass);
    env->DeleteGlobalRef(mpClass);
    env->DeleteGlobalRef(mimeCls);
    env->DeleteGlobalRef(iterMoIFClass);
    env->DeleteGlobalRef(nioBufferClass);
}

