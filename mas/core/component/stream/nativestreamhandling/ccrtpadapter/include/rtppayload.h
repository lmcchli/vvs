/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef RTPPayload_H_
#define RTPPayload_H_

#include <base_std.h>
#include <base_include.h>
#include <jni.h>

class StreamRTPSession;
namespace ost {
class PayloadFormat;
}
;

/**
 * Wrapper class for a Java RTPPayload that hides the JNI details.
 * 
 * @author J�rgen Terner
 */
namespace java {
class RTPPayload
{
private:

    /** Payload type defined in RFC3551. */
    int mPayloadType;

    /** Number of channels. */
    int mChannels;

    /** Clockrate in Hz. */
    unsigned mClockRate;

    /** audio/video MIME-type in lower-case. */
    base::String mCodec;

    // Senders bandwidth in RTCP
    int mBwSender;

    // Receivers bandwidth in RTCP
    int mBwReceiver;

    // media specific paramaters SDP attribute fmtp. 
    base::String mMediaFormatParameters;

    // keep java env until deleteion
    JNIEnv* mEnv;

public:
    /**
     * Creates a new RTPPayload and reads necessary info from the given
     * Java object.
     * 
     * @param payloadType Java object containing the payload info.
     * @param env         Reference to Java environment.
     */
    RTPPayload(jobject payloadType, JNIEnv* env);

    /**
     * Destructor.
     */
    ~RTPPayload();

    /** Gets the payload type as defined in RFC3551. */
    int getPayloadType();

    /**
     * @return The file extension that should be used when saving media
     *         received with this payload type. Can never be <code>null</code>.
     */
    base::String& getFileExtension();

    /**
     * @return Number of channels.
     */
    int getChannels();

    /**
     * @return Clockrate in Hz. 
     */
    unsigned getClockRate();

    /**
     * @return The media codec (audio or video MIME-type in lower-case).
     */
    const base::String& getCodec();

    void setPayloadFormat(StreamRTPSession& session);

    //    ost::PayloadFormat& getPayloadFormat();

    bool isDynamic();

    bool isStatic();

    int getBwSender();

    int getBwReceiver();

    base::String toString();

    template<class T>
    std::string StringOf(const T& object);

    const base::String& getMediaFormatParameters() const;
};

}
;
#endif /*RTPPAYLOAD_H_*/
