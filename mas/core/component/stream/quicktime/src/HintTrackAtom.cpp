#include "HintTrackAtom.h"

#include "GmhdAtom.h"
#include "HintSampleDescription.h"

using namespace quicktime;

HintTrackAtom::HintTrackAtom()
{
    m_sampleToChunkAtom.initialize(1);
    SampleToChunkEntry& 
	entry(m_sampleToChunkAtom.getSampleToChunkEntries()[0]);
    entry.firstChunk = 1;
    entry.samplesPerChunk = 1;
    entry.sampleDescriptionIndex = 1;

    m_trackHeaderAtom.setFlags(0);
    m_trackReferenceAtom.initialize(quicktime::HINT, 2);
    
    setTrackId(3);
    setSubType(quicktime::HINT);

    MinfAtom& mediaInformation (m_mediaAtom.getMediaInformationAtom());
    m_sampleDescriptionAtom.setSampleDescriptionEntry(new HintSampleDescription());

    // TODO : make atom member instead, should save some heap alloc
    /*
    mediaInformation.setMediaInformationHeaderAtom(new GmhdAtom());
    mediaInformation.getHandlerReferenceAtom()
	.setComponentType(quicktime::MHLR);
    */
    mediaInformation.setMediaInformationHeaderAtom(new GmhdAtom());
    mediaInformation.getHandlerReferenceAtom()
	.setComponentSubType(quicktime::HINT);

    DrefAtom& dataReference(mediaInformation
			    .getDataInformationAtom()
			    .getDataReferenceAtom());

    
    dataReference.initialize(1);
    dataReference.getDataReferenceEntries()[0].refSize = 4+4+4;
    dataReference.getDataReferenceEntries()[0].refType = quicktime::ALIS;
    dataReference.getDataReferenceEntries()[0].versionAndFlags = 0x00000001;
}

void 
HintTrackAtom::initialize(unsigned nOfFrames)
{
    m_timeToSampleAtom.initialize(nOfFrames);
    m_sampleSizeAtom.initialize(nOfFrames);
    m_chunkOffsetAtom.initialize(nOfFrames);
    for (unsigned int t(0); t < nOfFrames; t++) {
	TimeToSampleEntry& 
	    entry(m_timeToSampleAtom.getTimeToSampleEntries()[t]);
	entry.sampleCount = 1;
	entry.sampleDuration = 0;
    }
}
