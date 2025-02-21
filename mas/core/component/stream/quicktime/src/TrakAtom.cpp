#include "TrakAtom.h"

#include "TkhdAtom.h"
#include "MdiaAtom.h"
#include "MinfAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>

using std::cout;
using std::endl;

using namespace quicktime;

TrakAtom::TrakAtom() 
    : Atom(TRAK),
      m_handlerReferenceAtom(getMediaInformationAtom().getHandlerReferenceAtom()),
      m_sampleDescriptionAtom(getSampleTableAtom()
			      .getSampleDescriptionAtom()),
      m_timeToSampleAtom(getSampleTableAtom().getTimeToSampleAtom()),
      m_sampleToChunkAtom(getSampleTableAtom().getSampleToChunkAtom()),
      m_sampleSizeAtom(getSampleTableAtom().getSampleSizeAtom()),
      m_chunkOffsetAtom(getSampleTableAtom().getChunkOffsetAtom()) {
    firstChunk();
}

void
TrakAtom::setTrackId(unsigned id) {
    m_trackHeaderAtom.setId(id);
}

void 
TrakAtom::setSampleTime(unsigned sampleTime, unsigned index) {
    TimeToSampleEntry& 
	entry(m_timeToSampleAtom.getTimeToSampleEntries()[index]);

    entry.sampleCount = 1;
    entry.sampleDuration = sampleTime;
}

unsigned
TrakAtom::getSampleSize(unsigned index) {
    return m_sampleSizeAtom.getSampleSize(index);
}

void
TrakAtom::setSampleSize(unsigned chunkSize, unsigned index) {
    m_sampleSizeAtom.getSampleSizeEntries()[index] = chunkSize;
}

void
TrakAtom::setChunkOffset(unsigned offset, unsigned index) {
    m_chunkOffsetAtom.getChunkOffsetEntries()[index] = offset;
}

unsigned
TrakAtom::getChunkOffset(unsigned index) {
    return m_chunkOffsetAtom.getChunkOffsetEntries()[index];
}

void
TrakAtom::setStartTimeOffset(unsigned offset) {
    ElstAtom& editList(m_editAtom.getEditListAtom());
    int nOfElements(editList.getEditListEntryCount());

    if (nOfElements > 1) {
        editList.getEditListEntries()[0].trackDuration = offset;
    }
}

unsigned
TrakAtom::getStartTimeOffset() {
    ElstAtom& editList(m_editAtom.getEditListAtom());
    int nOfElements(editList.getEditListEntryCount());

    if (nOfElements > 1) {
        return editList.getEditListEntries()[0].trackDuration;
    }
    return 0;
}

unsigned
TrakAtom::getDataFormat() {
    return m_sampleDescriptionAtom.getDataFormat();
}

bool
TrakAtom::check() {
    if (getChunkOffsetCount() == 0) {
	MOV_DEBUG("No chunk offset");
	return false;
    }
    return true;
}

int
TrakAtom::getChunkOffsetCount() {
    return m_chunkOffsetAtom.getChunkOffsetCount();
}

unsigned
TrakAtom::getFrameTime(int index) {
    return m_timeToSampleAtom.
	getTimeToSampleEntries()[index].sampleDuration;
}

unsigned
TrakAtom::getChunkCount() {
    return m_chunkOffsetAtom.getChunkOffsetCount();
}

unsigned
TrakAtom::getSamplesPerChunk(unsigned wantedChunk) {
    return m_sampleToChunkAtom.getSamplesPerChunk(wantedChunk);
}

unsigned
TrakAtom::getMediaSize() {
    return m_sampleSizeAtom.getTotalSize();
}

bool
TrakAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize) {
    m_atomSize = atomSize;
    int nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;

    while (nOfBytesLeft) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::TKHD:
	    MOV_DEBUG("  (TRAK) TKHD " << subAtomSize);
	    m_trackHeaderAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::MDIA:
	    MOV_DEBUG("  (TRAK) MDIA " << subAtomSize);
	    m_mediaAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::EDTS:
	    MOV_DEBUG("  (TRAK) EDTS " << subAtomSize);
	    m_editAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::TREF:
	    MOV_DEBUG("  (TRAK) TREF " << subAtomSize);
	    m_trackReferenceAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	default:
            char unknownAtom[5];
	    unknownAtom[0] = (char)(subAtomName>>24)&0xff;
	    unknownAtom[1] = (char)(subAtomName>>16)&0xff;
	    unknownAtom[2] = (char)(subAtomName>>8)&0xff;
	    unknownAtom[3] = (char)(subAtomName>>0)&0xff;
	    unknownAtom[4] = '\0';
	    MOV_DEBUG("  (TRAK) " << unknownAtom << " " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("End of TRAK");
    return true;
}

bool
TrakAtom::saveGuts(AtomWriter& atomWriter) {
    MOV_DEBUG("\t\tNow at " <<std::hex <<atomWriter.tell() <<std::dec);
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    m_trackHeaderAtom.saveGuts(atomWriter);
    m_trackReferenceAtom.saveGuts(atomWriter);
    m_editAtom.saveGuts(atomWriter);
    m_mediaAtom.saveGuts(atomWriter);
    return true;
}

unsigned
TrakAtom::getAtomSize() {
    unsigned size(8);
    size += m_trackHeaderAtom.getAtomSize();
    size += m_trackReferenceAtom.getAtomSize(); //typically this is only initialized for video tracks. So 0 length.
    size += m_editAtom.getAtomSize();
    size += m_mediaAtom.getAtomSize();
    return size;
}

bool
TrakAtom::operator==(TrakAtom& leftAtom) {
    if (m_trackHeaderAtom != leftAtom.m_trackHeaderAtom) return false;
    if (m_trackReferenceAtom != leftAtom.m_trackReferenceAtom) return false;
    if (m_editAtom != leftAtom.m_editAtom) return false;
    if (m_mediaAtom != leftAtom.m_mediaAtom) return false;
    return true;
}

bool
TrakAtom::operator!=(TrakAtom& leftAtom) {
    return !(*this == leftAtom);
}


/****************************************************************
 * CHUNK ITERATION METHODS
 */
void
TrakAtom::firstChunk() {
    m_chunkItIndex = 0;
    m_chunkItStscEntryIndex = 0;
    m_chunkItSampleIndex = 0;
}

bool
TrakAtom::hasMoreChunks() {
    return  m_chunkItIndex < m_chunkOffsetAtom.getChunkOffsetCount();
}

void
TrakAtom::nextChunk() {
    MOV_DEBUG("Chunk=" <<m_chunkItIndex
	      <<" entry=" <<m_chunkItStscEntryIndex
	      <<std::hex
	      <<" offset=" <<getChunkOffset()
	      <<" size=" <<getChunkSize()
	      <<std::dec);
    ++m_chunkItIndex;
    m_chunkItSampleIndex += m_sampleToChunkAtom.getSampleToChunkEntries()[m_chunkItStscEntryIndex].samplesPerChunk;
    if (m_chunkItStscEntryIndex + 1 < m_sampleToChunkAtom.getEntryCount()
	&& m_sampleToChunkAtom.getSampleToChunkEntries()[m_chunkItStscEntryIndex + 1].firstChunk - 1 == m_chunkItIndex) {
	++m_chunkItStscEntryIndex;
    }
}

unsigned
TrakAtom::getChunkOffset() {
    return m_chunkOffsetAtom.getChunkOffsetEntries()[m_chunkItIndex];
}

void
TrakAtom::setChunkOffset(unsigned o) {
    m_chunkOffsetAtom.getChunkOffsetEntries()[m_chunkItIndex] = o;
}

unsigned
TrakAtom::getChunkSize() {
    unsigned result = 0;
    for (unsigned int i = 0;
	 i < m_sampleToChunkAtom.getSampleToChunkEntries()[m_chunkItStscEntryIndex].samplesPerChunk;
	 i++) {
	result += m_sampleSizeAtom.getSampleSize(i + m_chunkItSampleIndex);
    }
    return result;
}


/****************************************************************
 * SAMPLE ITERATION METHODS
 */
void
TrakAtom::firstSample() {
    m_sampItIndex = 0;
    m_sampItChunkIndex = 0;
    m_sampItStscEntryIndex = 0;
    m_sampleInChunk = 0;
    m_sampItOffset = getChunkOffset(m_sampItChunkIndex);
}

bool
TrakAtom::hasMoreSamples() {
    return  m_sampItIndex < m_sampleSizeAtom.getSampleSizeCount();
}

void
TrakAtom::nextSample() {
    m_sampItOffset += getSampleSize(); //Overwritten below if moving into new chunk
    ++m_sampItIndex;
    ++m_sampleInChunk;
    if (m_sampleInChunk >= m_sampleToChunkAtom.getSampleToChunkEntries()[m_sampItStscEntryIndex].samplesPerChunk) {
	++m_sampItChunkIndex;
	m_sampItOffset = getChunkOffset(m_sampItChunkIndex);
	m_sampleInChunk = 0;
	if (m_sampItStscEntryIndex + 1 < m_sampleToChunkAtom.getEntryCount()
	    && m_sampleToChunkAtom.getSampleToChunkEntries()[m_sampItStscEntryIndex + 1].firstChunk - 1 == m_sampItChunkIndex) {
	    ++m_sampItStscEntryIndex;
	}
    }
}

unsigned
TrakAtom::getChunkOfSample() {
    return m_sampItChunkIndex;
}

unsigned
TrakAtom::getSampleOffset() {
    return m_sampItOffset;
}

unsigned
TrakAtom::getSampleSize() {
    return m_sampleSizeAtom.getSampleSize(m_sampItIndex);
}

unsigned
TrakAtom::getSampleDuration() {
    /*Simplified implementation relying on one stts entry per sample */
    return m_timeToSampleAtom.getTimeToSampleEntries()[m_sampItIndex].sampleDuration;
}

unsigned
TrakAtom::getSampleIndex() {
    return m_sampItIndex;
}
