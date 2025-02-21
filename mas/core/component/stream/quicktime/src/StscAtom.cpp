#include "StscAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

/*
 * aligned(8) class SampleToChunkBox
extends FullBox(‘stsc’, version = 0, 0) {
unsigned int(32) entry_count;
	for (i=1; i <= entry_count; i++) {
	unsigned int(32) first_chunk;
	unsigned int(32) samples_per_chunk;
	unsigned int(32) sample_description_index;
	}
}
*/

StscAtom::StscAtom()
    : Atom(STSC,4+4+4+4), //the actual size is calculated depeding on the sample table size, this assumes no chunks.
      m_versionAndFlags(0),
      m_sampleToChunkEntries(0),
	  m_entriesCount(0)
{
}

StscAtom::~StscAtom()
{
    if (m_sampleToChunkEntries != 0) delete [] m_sampleToChunkEntries;
}

void
StscAtom::initialize(unsigned noOfEntries)
{    
	m_entriesCount=noOfEntries;
	if (m_sampleToChunkEntries != 0) delete [] m_sampleToChunkEntries;
	if (noOfEntries > 0) {
		m_sampleToChunkEntries = new SampleToChunkEntry[noOfEntries];
	} else {
		m_sampleToChunkEntries=0;
	}
}

bool
StscAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
	MOV_DEBUG("(STSC)  (restoreGuts)" << m_entriesCount);
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_entriesCount);
    initialize( m_entriesCount ); //create the table array.
    MOV_DEBUG("(STSC)   nOfEntries: " << m_entriesCount);
    for (unsigned int i = 0; i < m_entriesCount; i++) {
		atomReader.readDW(m_sampleToChunkEntries[i].firstChunk);
		atomReader.readDW(m_sampleToChunkEntries[i].samplesPerChunk);
		atomReader.readDW(m_sampleToChunkEntries[i].sampleDescriptionIndex);
#ifdef MOV_PRINT_DEBUG
		MOV_DEBUG("(STSC) entry " << i << std::endl 
		  << "firstChunk: " << std::hex << m_sampleToChunkEntries[i].firstChunk << "0x " << std::endl
		  << "Samples Per Chunk: " << m_sampleToChunkEntries[i].samplesPerChunk << std::endl
		  << "Sample Desc Index: " << std::hex << m_sampleToChunkEntries[i].sampleDescriptionIndex << "0x")
#endif
	}
    return true;
}

bool
StscAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("        (STBL) STSC save" << getAtomSize());
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_entriesCount);
	MOV_DEBUG("        (STBL) STSC entries" << m_entriesCount);
    for (unsigned int i = 0; i < m_entriesCount; i++) {
		atomWriter.writeDW(m_sampleToChunkEntries[i].firstChunk);
		atomWriter.writeDW(m_sampleToChunkEntries[i].samplesPerChunk);
		atomWriter.writeDW(m_sampleToChunkEntries[i].sampleDescriptionIndex);
    }
    return true;
}

unsigned
StscAtom::getAtomSize()
{
	unsigned size(16); //16 being 4 size + 4 name + 4 version and flags + 4 entry count.
	size=size+(m_entriesCount*(unsigned)12); //12 being the size of a sample entry or 4+4+4
	return size;
}

unsigned
StscAtom::getSamplesPerChunk(unsigned wantedChunk) {
    unsigned wantedEntryIx = getEntryForChunk(wantedChunk);
    return m_sampleToChunkEntries[wantedEntryIx].samplesPerChunk;
}

unsigned
StscAtom::getEntryForChunk(unsigned wantedChunk) {
    ++wantedChunk; //To account for numbers starting from 1.
	unsigned nextEntryIx = 0;
    while (nextEntryIx < m_entriesCount
	   && m_sampleToChunkEntries[nextEntryIx].firstChunk <= wantedChunk) {
	++nextEntryIx;
    }
    return nextEntryIx - 1;
}
  
unsigned 
StscAtom::getEntryCount()
{
    return m_entriesCount;
}

SampleToChunkEntry* 
StscAtom::getSampleToChunkEntries()
{
    return m_sampleToChunkEntries;
}

bool StscAtom::operator==(StscAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_entriesCount != leftAtom.m_entriesCount) return false;
    for (unsigned int i = 0; i < m_entriesCount; i++) {
	if (m_sampleToChunkEntries[i].firstChunk != 
	    leftAtom.m_sampleToChunkEntries[i].firstChunk) return false;
	if (m_sampleToChunkEntries[i].samplesPerChunk != 
	    leftAtom.m_sampleToChunkEntries[i].samplesPerChunk) return false;
	if (m_sampleToChunkEntries[i].sampleDescriptionIndex != 
	    leftAtom.m_sampleToChunkEntries[i].sampleDescriptionIndex) 
	    return false;
    }
    return true;
}

bool StscAtom::operator!=(StscAtom& leftAtom)
{
    return !(*this == leftAtom);
}

