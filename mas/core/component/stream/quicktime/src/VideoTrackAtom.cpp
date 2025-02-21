#include "VideoTrackAtom.h"

#include "VmhdAtom.h"
#include "SampleDescription.h"
#include "VideoSampleDescription.h"

using namespace quicktime;

VideoTrackAtom::VideoTrackAtom(sampleDescriptionAtom* videoSampleDescription)
{
    m_trackHeaderAtom.setWidth(0x00b00000); // Fixed point 174.000
    m_trackHeaderAtom.setHeight(0x00900000); // Fixed point 144.000
    m_sampleToChunkAtom.initialize(1);
    SampleToChunkEntry& 
	entry(m_sampleToChunkAtom.getSampleToChunkEntries()[0]);
    
    entry.firstChunk = 1;
    entry.samplesPerChunk = 1;
    entry.sampleDescriptionIndex = 1;
    setTrackId(2);
    setSubType(quicktime::VIDE);
    MinfAtom& mediaInformation (m_mediaAtom.getMediaInformationAtom());
    if (videoSampleDescription == NULL) {
        videoSampleDescription = new VideoSampleDescription();
    }
    m_sampleDescriptionAtom.setSampleDescriptionEntry(videoSampleDescription);

    // TODO : make atom member instead, should save some heap alloc
    mediaInformation.setMediaInformationHeaderAtom(new VmhdAtom()); 
    mediaInformation.getHandlerReferenceAtom()
	.setComponentType(quicktime::DHLR);

    DrefAtom& dataReference(mediaInformation
			    .getDataInformationAtom()
			    .getDataReferenceAtom());

    
    dataReference.initialize(1);
    dataReference.getDataReferenceEntries()[0].refSize = 4+4+4;
    dataReference.getDataReferenceEntries()[0].refType = quicktime::ALIS;
    dataReference.getDataReferenceEntries()[0].versionAndFlags = 0x00000001;
}


void 
VideoTrackAtom::initialize(unsigned nOfFrames)
{
    m_timeToSampleAtom.initialize(nOfFrames);
    m_sampleSizeAtom.initialize(nOfFrames);
    m_chunkOffsetAtom.initialize(nOfFrames);
}
