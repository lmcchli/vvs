#include "movtrackinfo.h"

#include "TrakAtom.h"
#include "MdiaAtom.h"
#include "HdlrAtom.h"
#include "StcoAtom.h"
#include "SttsAtom.h"
#include "StssAtom.h"

using namespace quicktime;

#include "movatomid.h"
#include "movreader.h"

MovTrackInfo::MovTrackInfo() :
        m_trackAtom(0), m_handlerReferenceAtom(0), m_chunkOffsetAtom(0), m_timeToSampleAtom(0), m_sampleSizeAtom(0), m_sampleToChunkAtom(
                0)
{
}

MovTrackInfo::~MovTrackInfo()
{
}

void MovTrackInfo::initialize(quicktime::TrakAtom* trackAtom)
{
    m_trackAtom = trackAtom;
    m_handlerReferenceAtom = 0;
    m_chunkOffsetAtom = 0;
    m_timeToSampleAtom = 0;
    m_sampleSizeAtom = 0;
    m_sampleToChunkAtom = 0;
    if (m_trackAtom != 0) {
        MdiaAtom& mediaAtom(m_trackAtom->getMediaAtom());
        HdlrAtom& handlerReferenceAtom(mediaAtom.getHandlerReferenceAtom());
        MinfAtom& mediaInformationAtom(mediaAtom.getMediaInformationAtom());
        StblAtom& sampleTableAtom(mediaInformationAtom.getSampleTableAtom());
        StcoAtom& chunkOffsetAtom(sampleTableAtom.getChunkOffsetAtom());
        SttsAtom& timeToSampleAtom(sampleTableAtom.getTimeToSampleAtom());
        StszAtom& sampleSizeAtom(sampleTableAtom.getSampleSizeAtom());
        StscAtom& sampleToChunkAtom(sampleTableAtom.getSampleToChunkAtom());

        m_handlerReferenceAtom = &handlerReferenceAtom;
        m_chunkOffsetAtom = &chunkOffsetAtom;
        m_timeToSampleAtom = &timeToSampleAtom;
        m_sampleSizeAtom = &sampleSizeAtom;
        m_sampleToChunkAtom = &sampleToChunkAtom;
    }
}

bool MovTrackInfo::check() const
{
    if (m_trackAtom == 0) {
        return false;
    }

    if (getChunkOffsetCount() == 0) {
        return false;
    }

    return true;
}

unsigned MovTrackInfo::getComponentSubType() const
{
    if (m_handlerReferenceAtom == 0)
        return (unsigned) -1;
    return m_handlerReferenceAtom->getComponentSubType();
}

int MovTrackInfo::getChunkOffsetCount() const
{
    if (m_chunkOffsetAtom == 0)
        return (unsigned) -1;
    return m_chunkOffsetAtom->getChunkOffsetCount();
}

int MovTrackInfo::getChunkOffset(int index) const
{
    if (m_chunkOffsetAtom == 0)
        return (unsigned) -1;
    return m_chunkOffsetAtom->getChunkOffsetEntries()[index];
}

unsigned MovTrackInfo::getFrameTime(int index) const
{
    if (m_timeToSampleAtom == 0)
        return (unsigned) -1;
    return m_timeToSampleAtom->getTimeToSampleEntries()[index].sampleDuration;
}

unsigned MovTrackInfo::getSamplesPerChunk(int index) const
{
    if (m_sampleToChunkAtom == 0)
        return (unsigned) -1;
    return m_sampleToChunkAtom->getSampleToChunkEntries()[index].samplesPerChunk;
}
