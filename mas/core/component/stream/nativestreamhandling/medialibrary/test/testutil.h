/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef TESTUTIL_H_
#define TESTUTIL_H_

#include <jni.h>
#include "mediaobject.h"
#include "mediaenvelope.h"
#include <stdexcept>

/**
 * Utility class for MediaLibrary Test
 * 
 * @author Mats Egland
 */
class TestUtil {
  
public:

     /** 
     * Creates a MediaObject-instance. This includes creating a Java
     * MediaObject instance as well.
     * 
     * @param env            Pointer to the JNI Environment
     * @param filename       The file to read data from.
     * @param bufferSize     Size of internal buffers in the MediaObject.
     * @param contentTypeStr Content type set in the media properties
     *                       of the media object.
     * @param fileExtension  File extension set in the media properties
     *                       of the media object.
     */
    static java::MediaObject* createReadOnlyCCMediaObject(JNIEnv* env, 
        const char* filename, jint bufferSize, 
        std::string& contentTypeStr, std::string& fileExtension);

    /**
     * Creates an empty Java MediaObject, i.e. a MediaObject without
     * any data appended.
     */
    static jobject createRecordableMediaObject(JNIEnv* env);

    /**
     * Creates a Java MediaObject that reads data from the specified
     * file.
     * 
     * @param env            Pointer to the JNI Environment
     * @param filename       The file to read data from.
     * @param bufferSize     Size of internal buffers in the MediaObject.
     * @param contentTypeStr Content type set in the media properties
     *                       of the media object.
     * @param fileExtension  File extension set in the media properties
     *                       of the media object.
     */ 
    static jobject createReadOnlyMediaObject(JNIEnv* env, 
        const char* filename, jint buffersize,
        std::string& contentTypeStr, std::string& fileExtension);
    
    /**
     * Sets the specified fileFormat on the Java MediaObject object.
     * @param env Pointer to the JNI Environment
     * @param mo The Java MediaObject object to set the fileformat on
     * @param fileFormat the fileformat, for example "WAV"
     * @throws std::logic_error if the operation fails
     */ 
    static void setFileFormatOnMediaObject(
                    JNIEnv* env, 
                    jobject mo, 
                    const char* fileFormat) throw (std::logic_error);
    /**
     * Appends data (buffer) to a Java MediaObject. Java NIO ByteBuffers will be 
     * created to wrap the data. 
     * 
     * @param env Pointer to the JNI Environment
     * @param mo The Java MediaObject object to set the fileformat on
     * @param buffersize the size of the buffer in bytes
     * 
     * @return Writeable buffer.
     * 
     * @throws std::logic_error if the operation fails
     */ 
    static char* appendDataToMediaObject(
                    JNIEnv* env, 
                    jobject mo, 
                    size_t bufferSize) throw (std::logic_error);
    
    
    /**
     * Sets the Java Media Object to immutable.
     * 
     * @param env Pointer to the JNI Environment
     * @param mo  The Java MediaObject object that shall be immutable.
     * 
     * @throws std::logic_error if the operation fails
     */
    static void setImmutable(JNIEnv* pEnv, jobject mo);
    
    /**
     * Sets contentType and fileExtension in the given RECORDABLE 
     * media object.
     * <p>
     * This method should be used on a RECORDABLE media object before it
     * is closed for writing.
     * 
     * @param env            Pointer to the JNI Environment.
     * @param mo             The media object.
     * @param contentTypeStr Content type.
     * @param fileExtension  File extension.
     */
    static void setContentTypeAndFileExtension(JNIEnv* env, java::MediaObject* mo,
        std::string& contentTypeStr, std::string& fileExtension);
        
    /**
     * Saves the content of the given media object to a file with
     * the given filename.
     * 
     * @param pEnv     Pointer to the JNI Environment.
     * @param mo       Source.
     * @param fileName Destination.
     */
    static void saveAs(JNIEnv* pEnv, jobject mo, std::string& fileName);
    
    /**
     * Sets contentType and fileExtension in the given IMMUTABLE 
     * media object.
     * <p>
     * This method should be used on a IMMUTABLE media object before it
     * is given to a parser.
     * 
     * @param env            Pointer to the JNI Environment.
     * @param mo             The media object.
     * @param contentTypeStr Content type.
     * @param fileExtension  File extension.
     */
    static void setContentTypeAndFileExtension(JNIEnv* env, jobject mo,
        std::string& contentTypeStr, std::string& fileExtension);
};

#endif /*TESTUTIL_H_*/
