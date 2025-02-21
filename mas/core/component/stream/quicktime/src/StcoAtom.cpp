#include "StcoAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>
using std::cout;
using std::endl;

using namespace quicktime;

StcoAtom::StcoAtom()
    : Atom(STCO),
      m_versionAndFlags(0),
      m_chunkOffsetCount(0),
      m_chunkOffsetEntries(0)
{
}

StcoAtom::~StcoAtom()
{
    if (m_chunkOffsetEntries != 0) delete [] m_chunkOffsetEntries;
}

void
StcoAtom::initialize(unsigned nOfSamples)
{
    m_chunkOffsetCount = nOfSamples;
    if (m_chunkOffsetEntries != 0) delete [] m_chunkOffsetEntries;
    m_chunkOffsetEntries = new unsigned[nOfSamples];
}

bool
StcoAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);  //4 byte version
    atomReader.readDW(m_chunkOffsetCount); //4 byte count
    initialize(m_chunkOffsetCount); //create array and set number of samples
    MOV_DEBUG("        		(STCO) nOfEntries: " << m_chunkOffsetCount);
    for (unsigned int i = 0; i < m_chunkOffsetCount; i++) {
	atomReader.readDW(m_chunkOffsetEntries[i]);
	MOV_DEBUG("        				(STCO) entry[" << i <<"] value: " << std::hex << m_chunkOffsetEntries[i] << "0x");
    }
	MOV_DEBUG("        		(STCO) END") 
    return true;
}

bool
StcoAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("   save     (STBL) STCO size: " << getAtomSize() << " numEntries: " << m_chunkOffsetCount);
    atomWriter.writeDW(StcoAtom::getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_chunkOffsetCount);
    for (unsigned int i = 0; i < m_chunkOffsetCount; i++) {
		atomWriter.writeDW(m_chunkOffsetEntries[i]);
    }
    return true;
}

unsigned
StcoAtom::getAtomSize()
{
    return (4*4) + m_chunkOffsetCount*4;
}

bool StcoAtom::operator==(StcoAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_chunkOffsetCount != leftAtom.m_chunkOffsetCount) return false;
    for (unsigned int i = 0; i < m_chunkOffsetCount; i++) {
	if (m_chunkOffsetEntries[i] != 
	    leftAtom.m_chunkOffsetEntries[i]) return false;
    }
    return true;
}

bool StcoAtom::operator!=(StcoAtom& leftAtom)
{
    return !(*this == leftAtom);
}

