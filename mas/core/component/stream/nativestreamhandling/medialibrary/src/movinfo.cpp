#include "movinfo.h"

#include "MoovAtom.h"
#include "TrakAtom.h"
using namespace quicktime;

MovInfo::MovInfo()
{
}

MovInfo::~MovInfo()
{
}

void MovInfo::initialize(MoovAtom& moovAtom)
{
    m_videoTrack.initialize(moovAtom.getVideoTrackAtom());
    m_audioTrack.initialize(moovAtom.getAudioTrackAtom());
    m_hintTrack.initialize(moovAtom.getHintTrackAtom());
}

bool MovInfo::check() const
{
    if (m_videoTrack.check() == false)
        return false;
    if (m_audioTrack.check() == false)
        return false;
    if (m_hintTrack.check() == false)
        return false;

    return true;
}

int MovInfo::getFrameCount() const
{
    if (m_hintTrack.check())
        return m_hintTrack.getChunkOffsetCount();
    return 0;
}

int MovInfo::getAudioChunkCount() const
{
    if (m_audioTrack.check())
        return m_audioTrack.getChunkOffsetCount();
    return 0;
}

MovTrackInfo* MovInfo::getHintTrack()
{
    /*
     if (m_hintTrack.check()) return &m_hintTrack;
     return 0;
     */
    return &m_hintTrack;
}

MovTrackInfo* MovInfo::getVideoTrack()
{
    /*
     if (m_videoTrack.check()) return &m_videoTrack;
     return 0;
     */
    return &m_videoTrack;
}

MovTrackInfo* MovInfo::getAudioTrack()
{
    /*
     if (m_audioTrack.check()) return &m_audioTrack;
     return 0;
     */
    return &m_audioTrack;
}
