#ifndef _MediaDescription_h_
#define _MediaDescription_h_

#include <base_include.h>

class MediaDescription {
public:
    void setAudioCodec(const base::String& audioCodec);
    const base::String& getAudioCodec();
    void setVideoCodec(const base::String& videoCodec);
    const base::String& getVideoCodec();
    void setDuration(unsigned duration);
    unsigned getDuration();
    unsigned getAudioStartTimeOffset();
    void setAudioStartTimeOffset(unsigned offset);
    unsigned getVideoStartTimeOffset();
    void setVideoStartTimeOffset(unsigned offset);

private:
    base::String m_audioCodec;
    base::String m_videoCodec;
    unsigned m_duration;
    unsigned m_audioStartTimeOffset;
    unsigned m_videoStartTimeOffset;
};

#endif
