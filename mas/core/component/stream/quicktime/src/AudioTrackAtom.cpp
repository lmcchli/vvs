#include "AudioTrackAtom.h"

#include "SmhdAtom.h"
#include "SampleDescription.h"
#include "PCMSoundSampleDescription.h"

class SmhdAtom;
using namespace quicktime;

AudioTrackAtom::AudioTrackAtom(sampleDescriptionAtom* sdEntry)
{
    // Ensure that all headers indicate audio
    // Set up time scale and all ...
    setTrackId(1);
    setSubType(quicktime::SOUN);

    MinfAtom& mediaInformation (m_mediaAtom.getMediaInformationAtom());
	// By default this is ULAW/PCMU
	if (sdEntry == 0) {
		MOV_DEBUG("AudioTrackAtom(): Setting default PCM ULAW sample description");
		sdEntry = new PCMSoundSampleDescription(ULAW);
	}
	// Setting the STSD sub atom
    m_sampleDescriptionAtom.setSampleDescriptionEntry(sdEntry);

	MOV_DEBUG("AudioTrackAtom(): Setting new smhdAtom.")
    mediaInformation.setMediaInformationHeaderAtom(new SmhdAtom());
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
AudioTrackAtom::initialize(unsigned nOfChunks)
{
    m_timeToSampleAtom.initialize(1);
    m_sampleToChunkAtom.initialize(1);
    m_sampleSizeAtom.initialize(0);
    m_chunkOffsetAtom.initialize(nOfChunks);
}

void
AudioTrackAtom::setSampleSize(unsigned sampleSize)
{
    // Ad-hoc for PCMU
    m_sampleSizeAtom.setSampleSizeCount(sampleSize);
    m_sampleSizeAtom.setSampleSize(1);
}

void 
AudioTrackAtom::setSampleSize(unsigned sampleSize, unsigned sampleCount)
{
    m_sampleSizeAtom.setSampleSizeCount(sampleCount);
    m_sampleSizeAtom.setSampleSize(sampleSize);
}

void 
AudioTrackAtom::setSampleTime(unsigned sampleTime)
{
    TimeToSampleEntry& 
	entry(m_timeToSampleAtom.getTimeToSampleEntries()[0]);

    entry.sampleCount = sampleTime;
    entry.sampleDuration = 1;
}

void 
AudioTrackAtom::setSampleTime(unsigned sampleTime, unsigned sampleCount)
{
    TimeToSampleEntry& 
	entry(m_timeToSampleAtom.getTimeToSampleEntries()[0]);

    entry.sampleCount = sampleCount;
    entry.sampleDuration = sampleTime;
}

void 
AudioTrackAtom::setSampleToChunk(unsigned chunkSize)
{
    SampleToChunkEntry& 
	entry(m_sampleToChunkAtom.getSampleToChunkEntries()[0]);
    
    entry.firstChunk = 1;
    entry.samplesPerChunk = chunkSize;
    entry.sampleDescriptionIndex = 1;
}
