#include "AmrTrackAtom.h"

using namespace quicktime;

AmrTrackAtom::AmrTrackAtom(AmrSampleDescription* sampleDescription) 
    : AudioTrackAtom((sampleDescriptionAtom*)sampleDescription)
{
}

void 
AmrTrackAtom::initialize(unsigned nOfFrames)
{
    m_timeToSampleAtom.initialize(1);
    m_sampleToChunkAtom.initialize(1);
    m_sampleSizeAtom.initialize(nOfFrames);
    m_chunkOffsetAtom.initialize(nOfFrames);
}


void 
AmrTrackAtom::setSampleToChunk(unsigned index, unsigned chunkSize)
{
    SampleToChunkEntry& 
	entry(m_sampleToChunkAtom.getSampleToChunkEntries()[0]);
    
    entry.firstChunk = 1;
    entry.samplesPerChunk = 1;
    entry.sampleDescriptionIndex = 1;

    m_sampleSizeAtom.getSampleSizeEntries()[index] = chunkSize;
}
