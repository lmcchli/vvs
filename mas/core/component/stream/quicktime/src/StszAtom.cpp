#include "StszAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

StszAtom::StszAtom()
    : Atom(STSZ),
      m_versionAndFlags(0),
      m_sizeOfSample(0),
      m_sampleSizeCount(0),
      m_sampleSizeEntries(0)
{
}

StszAtom::~StszAtom()
{
    // Ensure that previous allocated is deallocated
    if (m_sampleSizeEntries != 0) delete [] m_sampleSizeEntries;
}

void
StszAtom::initialize(unsigned nOfSamples)
{
    // Ensure that previous allocated is deallocated
    if (m_sampleSizeEntries != 0) {
        delete [] m_sampleSizeEntries;
        m_sampleSizeEntries = 0;
    }
    m_sampleSizeCount = nOfSamples;
    // Allocate memory, if nescessary
    if (nOfSamples > 0) m_sampleSizeEntries = new unsigned[nOfSamples];
}

bool
StszAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_sizeOfSample);
    atomReader.readDW(m_sampleSizeCount);
    MOV_DEBUG("(STSZ)  nOfEntries:" <<m_sampleSizeCount <<" sizeOfSample:" <<m_sizeOfSample);
    if (m_sizeOfSample > 0) {
	m_totalSize = m_sizeOfSample * m_sampleSizeCount;
    } else {
	m_totalSize = 0;
	initialize(m_sampleSizeCount);
	for (unsigned int i = 0; i < m_sampleSizeCount; i++) {
	    atomReader.readDW(m_sampleSizeEntries[i]);
	    m_totalSize += m_sampleSizeEntries[i];
	    MOV_DEBUG("(STSZ) " << m_sampleSizeEntries[i]);
	}
    }
    return true;
}

bool
StszAtom::saveGuts(AtomWriter& atomWriter)
{ 
    MOV_DEBUG("        (STBL) STSZ " << getAtomSize());
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_sizeOfSample);
    atomWriter.writeDW(m_sampleSizeCount);
    if (m_sizeOfSample == 0) {
	for (unsigned int i = 0; i < m_sampleSizeCount; i++) {
	    atomWriter.writeDW(m_sampleSizeEntries[i]);
	}
    }
    return true;
}

unsigned
StszAtom::getAtomSize()
{
    unsigned size(5*4);

    if (m_sizeOfSample == 0) size += m_sampleSizeCount*(4);
    
    return size;
}

void
StszAtom::setSampleSize(unsigned size) {
    m_sizeOfSample = size;
    if (m_sizeOfSample > 0) {
	    m_totalSize = m_sampleSizeCount * m_sizeOfSample;
    }
}

void
StszAtom::setSampleSizeCount(unsigned count) {
    m_sampleSizeCount = count;
    if (m_sizeOfSample > 0) {
	    m_totalSize = m_sampleSizeCount * m_sizeOfSample;
    }
}

unsigned
StszAtom::getSampleSize(unsigned index) {
    if (m_sizeOfSample > 0) {
	    return m_sizeOfSample;
    } else {
	    return m_sampleSizeEntries[index];
    }
}

bool StszAtom::operator==(StszAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_sizeOfSample != leftAtom.m_sizeOfSample) return false;
    if (m_sampleSizeCount != leftAtom.m_sampleSizeCount) return false;
    if (m_sizeOfSample == 0) {
	for (unsigned int i = 0; i < m_sampleSizeCount; i++) {
	    if (m_sampleSizeEntries[i] != 
		leftAtom.m_sampleSizeEntries[i]) return false;
	}
    }
    return true;
}

bool StszAtom::operator!=(StszAtom& leftAtom)
{
    return !(*this == leftAtom);
}

