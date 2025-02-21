/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef STREAMCONTENTINFO_H_
#define STREAMCONTENTINFO_H_

#include "jni.h"
#include <base_include.h>
#include <base_std.h>
#include <boost/ptr_container/ptr_vector.hpp>

namespace java {
class RTPPayload;

/**
 * Wrapper class for a Java StreamContentInfo class that hides the JNI details.
 * 
 * @author J�rgen Terner
 */
class StreamContentInfo
{
private:
    StreamContentInfo(const StreamContentInfo& rhs);
    StreamContentInfo& operator=(const StreamContentInfo& rhs);

    std::auto_ptr<RTPPayload> mAudioPayload;
    std::auto_ptr<RTPPayload> mVideoPayload;
    std::auto_ptr<RTPPayload> mDTMFPayload;
    std::auto_ptr<RTPPayload> mCNPayload;

    /** 
     * Content type ("audio/pcmu"). Is only set for inbound streams. 
     * This must be a global reference and thus must be deleted in the 
     * destructor.
     */
    jobject mContentType;

    /** 
     * The file extension that should be used when saving media
     * received with this payload type. Is only set for inbound streams.
     */
    base::String mFileExtension;

    /** 
     * Used as identifier in the RTP packets sent by this stream. 
     * See RFC 1550 for more information about CNAME. 
     */
    base::String mCNAME;

    /**
     * If information for an outbound stream, this is all supported 
     * payload types.
     */
    boost::ptr_vector<RTPPayload> mPayloads;

    /** 
     * If information for an outbound stream, this is the number of
     * supported payload types.
     */
    int mPayloadSize;

    /** 
     * Recommended length of time in milliseconds represented by the media in 
     * a packet.
     */
    int mPTime;

    /** 
     * Maximum length of time in milliseconds represented by the media in 
     * a packet.
     */
    int mMaxPTime;

    /**
     * <code>true</code> if this is a video-session, <code>false</code> if
     * this is an audio-session.
     */
    bool mIsVideo;

    /**
     * Creates a new StreamContentInfo and reads necessary info for an
     * inbound stream from the given Java object.
     * 
     * @param contentInfo Java object containing the information.
     * @param env         Reference to Java environment.
     */
    StreamContentInfo(jobject contentInfo, JNIEnv* env, bool forInbound);

    /**
     * Creates a new StreamContentInfo and reads necessary info for an 
     * outbound stream from the given Java object.
     * 
     * @param contentInfo Java object containing the information.
     * @param env         Reference to Java environment.
     */
    StreamContentInfo(jobject contentInfo, JNIEnv* env);

public:

    /**
     * Creates a new instance and reads information for an inbound stream from
     * the given Java instance.
     * 
     * @param contentInfo Java object containing the information.
     * @param env         Reference to Java environment.
     */
    static std::auto_ptr<StreamContentInfo> getInbound(jobject contentInfo, JNIEnv* env);

    /**
     * Creates a new instance and reads information for an outbound stream from
     * the given Java instance.
     * 
     * @param contentInfo Java object containing the information.
     * @param env         Reference to Java environment.
     */
    static std::auto_ptr<StreamContentInfo> getOutbound(jobject contentInfo, JNIEnv* env);

    /**
     * Destructor. If possible, call <code>cleanUp</code> before deleting
     * an instance.
     */
    virtual ~StreamContentInfo();

    /**
     * @return The audio payload, is <code>null</code> for outbound streams.
     */
    RTPPayload* getAudioPayload();

    /**
     * Tries to map the given codec to an RTPPayload. 
     * <p>
     * The codec is specified as a MIME type (lower-case).
     * 
     * @return The RTPPayload that maps to the given codec or
     *         <code>null</code> if no mapping was found.
     */
    RTPPayload* getPayload(const base::String& codec);

    /**
     * @return The video payload, is <code>null</code> for
     *         audio streams.
     */
    RTPPayload* getVideoPayload();

    /**
     * @return The DTMF payload, is never <code>null</code>.
     */
    RTPPayload& getDTMFPayload();

    /**
     * @return The CN payload, is never <code>null</code>.
     */
    RTPPayload* getCNPayload();

    /**
     * @return The content type, <code>null</code> for outbound streams.
     */
    jobject getContentType();

    /**
     * @return The file extension that should be used when saving media
     *         received with this payload type, <code>null</code>
     *         for outbound streams.
     */
    base::String& getFileExtension();

    /** 
     * Gets the recommended length of time in milliseconds 
     * represented by the media in a packet.
     */
    int getPTime();

    /** 
     * Gets the maximum length of time in milliseconds 
     * represented by the media in a packet.
     */
    int getMaxPTime();

    /**
     * Gets the CNAME used by this stream.
     * 
     */
    base::String& getCNAME();

    /**
     * @return <code>true</code> if this is a video-session, 
     *         <code>false</code> if this is an audio-session.
     */
    bool isVideo();
};

}
;
#endif /*STREAMCONTENTINFO_H_*/
