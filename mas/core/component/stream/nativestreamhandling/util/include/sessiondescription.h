#ifndef _SessionDescription_h_
#define _SessionDescription_h_

namespace java { class RTPPayload; };

class SessionDescription {
public:
    struct RtpPayload {
        bool defined;
        unsigned payloadType;
        unsigned clockRate;
    };

public:
    SessionDescription();

    void setPTime(unsigned pTime);
    unsigned getPTime();
    void setAudioPayload(unsigned payloadType, unsigned clockRate);
    SessionDescription::RtpPayload& getAudioPayload();
    void setVideoPayload(unsigned payloadType, unsigned clockRate);
    SessionDescription::RtpPayload& getVideoPayload();
    void setDtmfPayload(unsigned payloadType, unsigned clockRate);
    SessionDescription::RtpPayload& getDtmfPayload();

private:
    RtpPayload m_audioPayload;
    RtpPayload m_videoPayload;
    RtpPayload m_dtmfPayload;
    unsigned m_pTime;
};

#endif
