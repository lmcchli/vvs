#include "amrtrackinfo.h"

#include "TrakAtom.h"
#include "MdiaAtom.h"
#include "HdlrAtom.h"
#include "StcoAtom.h"
#include "SttsAtom.h"
#include "StssAtom.h"

using namespace quicktime;

#include "movatomid.h"
#include "movreader.h"

AmrTrackInfo::AmrTrackInfo() :
        m_trackAtom(0), m_handlerReferenceAtom(0), m_chunkOffsetAtom(0), m_timeToSampleAtom(0),
        m_sampleSizeAtom(0), m_sampleToChunkAtom(0)
{
}

AmrTrackInfo::~AmrTrackInfo()
{
}

void AmrTrackInfo::initialize(quicktime::TrakAtom* trackAtom)
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

bool AmrTrackInfo::check() const
{
    if (m_trackAtom == 0) {
        return false;
    }
    if (getChunkOffsetCount() == 0) {
        return false;
    }
    return true;
}

unsigned AmrTrackInfo::getComponentSubType() const
{
    if (m_handlerReferenceAtom == 0)
        return (unsigned) -1;
    return m_handlerReferenceAtom->getComponentSubType();
}

int AmrTrackInfo::getChunkOffsetCount() const
{
    if (m_chunkOffsetAtom == 0)
        return (unsigned) -1;
    return m_chunkOffsetAtom->getChunkOffsetCount();
}

int AmrTrackInfo::getChunkOffset(int index) const
{
    if (m_chunkOffsetAtom == 0)
        return (unsigned) -1;
    return m_chunkOffsetAtom->getChunkOffsetEntries()[index];
}

unsigned AmrTrackInfo::getFrameTime(int index) const
{
    if (m_timeToSampleAtom == 0)
        return (unsigned) -1;
    return m_timeToSampleAtom->getTimeToSampleEntries()[index].sampleDuration;
}

unsigned AmrTrackInfo::getSamplesPerChunk(int index) const
{
    if (m_sampleToChunkAtom == 0)
        return (unsigned) -1;
    return m_sampleToChunkAtom->getSampleToChunkEntries()[index].samplesPerChunk;
}

