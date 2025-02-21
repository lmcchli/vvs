#include "SttsAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

SttsAtom::SttsAtom()
    : Atom(STTS),
      m_versionAndFlags(0),
      m_timeToSampleCount(0),
      m_timeToSampleEntries(0)
{
}

SttsAtom::~SttsAtom()
{
    if (m_timeToSampleEntries != 0) delete [] m_timeToSampleEntries;
}

void
SttsAtom::initialize(unsigned nOfSamples)
{
    m_timeToSampleCount = nOfSamples;
    if (m_timeToSampleEntries != 0) delete [] m_timeToSampleEntries;
    m_timeToSampleEntries = new TimeToSampleEntry[nOfSamples];
}

bool
SttsAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_timeToSampleCount);
    initialize(m_timeToSampleCount);
    MOV_DEBUG("(STTS)  nOfEntries: " << m_timeToSampleCount);
    for (unsigned int i = 0; i < m_timeToSampleCount; i++) {
	    atomReader.readDW(m_timeToSampleEntries[i].sampleCount);
	    atomReader.readDW(m_timeToSampleEntries[i].sampleDuration);
	    MOV_DEBUG("(STTS) " << m_timeToSampleEntries[i].sampleCount
		                    << " : " << m_timeToSampleEntries[i].sampleDuration);
    }
    return true;
}

bool
SttsAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("        (STBL) STTS " << getAtomSize());
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_timeToSampleCount);
    for (unsigned int i = 0; i < m_timeToSampleCount; i++) {
	    atomWriter.writeDW(m_timeToSampleEntries[i].sampleCount);
	    atomWriter.writeDW(m_timeToSampleEntries[i].sampleDuration);
    }
    return true;
}

unsigned
SttsAtom::getAtomSize()
{
    return 4*4 + m_timeToSampleCount*(4+4);
}

bool SttsAtom::operator==(SttsAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_timeToSampleCount != leftAtom.m_timeToSampleCount) return false;
    for (unsigned int i = 0; i < m_timeToSampleCount; i++) {
	    if (m_timeToSampleEntries[i].sampleCount != 
	        leftAtom.m_timeToSampleEntries[i].sampleCount) return false;
	    if (m_timeToSampleEntries[i].sampleDuration != 
	        leftAtom.m_timeToSampleEntries[i].sampleDuration) return false;
    }
    return true;
}

bool SttsAtom::operator!=(SttsAtom& leftAtom)
{
    return !(*this == leftAtom);
}

