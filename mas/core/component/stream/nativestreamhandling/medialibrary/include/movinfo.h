#ifndef MovInfo_h
#define MovInfo_h

#include "mediainfo.h"
#include "movtrackinfo.h"
#include "platform.h"

namespace quicktime {
class MoovAtom;
};

#include <vector>

class MEDIALIB_CLASS_EXPORT MovInfo: public MediaInfo
{
public:
    MovInfo();
    ~MovInfo();
    void initialize(quicktime::MoovAtom& moovAtom);
    bool check() const;
    int getFrameCount() const;
    int getAudioChunkCount() const;

    MovTrackInfo* getHintTrack();
    MovTrackInfo* getVideoTrack();
    MovTrackInfo* getAudioTrack();
    /*    void setReader(MovReader* reader);
     */
    /*
     private:
     MovReader* m_reader;
     unsigned m_mediaDataStart;
     unsigned m_mediaDataSize;
     unsigned m_movieTimeScale;
     unsigned m_movieDuration;
     unsigned m_nextTrackId;
     std::vector<MovTrackInfo*> m_tracks;
     */
    MovTrackInfo m_audioTrack;
    MovTrackInfo m_videoTrack;
    MovTrackInfo m_hintTrack;
};

/*
 inline void MovInfo::setReader(MovReader* reader)
 {
 m_reader = reader;
 }

 inline MovTrackInfo* MovInfo::getHintTrack()
 {
 return m_hintTrack;
 }

 inline MovTrackInfo* MovInfo::getVideoTrack()
 {
 return m_videoTrack;
 }

 inline MovTrackInfo* MovInfo::getAudioTrack()
 {
 return m_audioTrack;
 }
 */
#endif
