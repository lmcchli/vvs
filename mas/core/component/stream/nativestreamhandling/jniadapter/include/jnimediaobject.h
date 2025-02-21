/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIMEDIAOBJECTREF_H_
#define JNIMEDIAOBJECTREF_H_

#include "jni.h"

extern "C" {
extern void JNI_MediaObjectOnLoad(void*);
}

class JNIMediaObject
{
public:
    static const char* GET_MEDIA_PROPERTIES_METHOD;
    static const char* GET_NATIVE_ACCESS_METHOD;
    static const char* GET_FILE_EXTENSION_METHOD;
    static const char* GET_CONTENT_TYPE_METHOD;
    static const char* GET_SIZE_METHOD;
    static const char* IS_IMMUTABLE_METHOD;
    static const char* SET_IMMUTABLE_METHOD;

    static const char* ADD_LENGTH_METHOD;
    static const char* SET_FILE_EXTENSION_METHOD;
    static const char* SET_CONTENT_TYPE_METHOD;

    static const char* TO_STRING_METHOD;

    static const char* APPEND_METHOD;
    static const char* ITERATOR_METHOD;

    static const char* HAS_NEXT_METHOD;
    static const char* NEXT_METHOD;

    static const char* LIMIT_METHOD;

    static const char* MEDIALENGTH_CLASSNAME;

    inline static jmethodID getGetNativeAccessMID()
    {
        return getNativeAccessMID;
    };
    inline static jmethodID getGetMediaPropertiesMID()
    {
        return getMediaPropertiesMID;
    };
    inline static jmethodID getGetSizeMID()
    {
        return getSizeMID;
    };
    inline static jmethodID getIsImmutableMID()
    {
        return isImmutableMID;
    };
    inline static jmethodID getSetImmutableMID()
    {
        return setImmutableMID;
    };
    inline static jmethodID getIteratorMID()
    {
        return iteratorMID;
    };
    inline static jmethodID getAddLengthMID()
    {
        return addLengthMID;
    };
    inline static jmethodID getSetFileExtensionMID()
    {
        return setFileExtensionMID;
    };
    inline static jmethodID getGetFileExtensionMID()
    {
        return getFileExtensionMID;
    };
    inline static jmethodID getSetContentTypeMID()
    {
        return setContentTypeMID;
    };
    inline static jmethodID getGetContentTypeMID()
    {
        return getContentTypeMID;
    };
    inline static jclass getLengthUnitCls()
    {
        return mLengthUnitCls;
    };
    inline static jfieldID getMillisecondsFID()
    {
        return millisecondsFID;
    };
    inline static jclass getMediaLengthCls()
    {
        return mediaLengthCls;
    };
    inline static jmethodID getInitMID()
    {
        return initMID;
    };

    inline static jmethodID getToStringMID()
    {
        return toStringMID;
    };

    inline static jmethodID getAppendMID()
    {
        return appendMID;
    };
    inline static jmethodID getHasNextMID()
    {
        return hasNextMID;
    };
    inline static jmethodID getNextMID()
    {
        return nextMID;
    };
    inline static jmethodID getLimitMID()
    {
        return limitMID;
    };

    static void MediaObjectOnLoad(void *reserved);

private:
    static const char* MEDIAOBJECT_INTERFACE_CLASSNAME;
    static const char* GET_NATIVE_ACCESS_METHOD_SIGNATURE;
    static jmethodID getNativeAccessMID;
    static const char* GET_MEDIA_PROPERTIES_METHOD_SIGNATURE;
    static jmethodID getMediaPropertiesMID;
    static const char* GET_SIZE_METHOD_SIGNATURE;
    static jmethodID getSizeMID;
    static const char* IS_IMMUTABLE_METHOD_SIGNATURE;
    static jmethodID isImmutableMID;
    static const char* SET_IMMUTABLE_METHOD_SIGNATURE;
    static jmethodID setImmutableMID;

    static const char* MEDIA_PROPERTIES_CLASSNAME;
    static const char* ADD_LENGTH_METHOD_SIGNATURE;
    static jmethodID addLengthMID;
    static const char* SET_CONTENT_TYPE_METHOD_SIGNATURE;
    static jmethodID setContentTypeMID;
    static const char* SET_FILE_EXTENSION_METHOD_SIGNATURE;
    static jmethodID setFileExtensionMID;
    static const char* GET_FILE_EXTENSION_METHOD_SIGNATURE;
    static jmethodID getFileExtensionMID;
    static const char* GET_CONTENT_TYPE_METHOD_SIGNATURE;
    static jmethodID getContentTypeMID;

    static const char* LENGTHUNIT_CLASSNAME;
    static const char* LENGTHUNIT_MILLISECONDS_NAME;
    static const char* LENGTHUNIT_MILLISECONDS_NAME_SIGNATURE;
    static jclass mLengthUnitCls;
    static jfieldID millisecondsFID;

    static const char* MEDIALENGTH_CONSTRUCTOR_NAME; // this is actually done through the init method
    static const char* MEDIALENGTH_CONSTRUCTOR_NAME_SIGNATURE;
    static jclass mediaLengthCls;
    static jmethodID initMID;

    static const char* MIMETYPE_CLASSNAME;
    static const char* TO_STRING_METHOD_SIGNATURE;
    static jmethodID toStringMID;

    static const char* MEDIAOBJECT_NATIVE_ACCESS_CLASSNAME;
    static const char* APPEND_METHOD_SIGNATURE;
    static jmethodID appendMID;
    static const char* ITERATOR_METHOD_SIGNATURE;
    static jmethodID iteratorMID;

    static const char* ITERATOR_CLASSNAME;
    static const char* HAS_NEXT_METHOD_SIGNATURE;
    static jmethodID hasNextMID;
    static const char* NEXT_METHOD_SIGNATURE;
    static jmethodID nextMID;

    static const char* BUFFER_CLASSNAME;
    static const char* LIMIT_METHOD_SIGNATURE;
    static jmethodID limitMID;
};

#endif /* JNIMEDIAOBJECTREF_H_ */
