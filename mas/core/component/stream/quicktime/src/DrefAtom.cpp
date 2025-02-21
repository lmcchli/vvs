#include "DrefAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

DrefAtom::DrefAtom()
    : Atom(DREF),
      m_versionAndFlags(0),
      m_dataReferenceEntryCount(0),
      m_dataReferenceEntries(0)
{
}

DrefAtom::~DrefAtom()
{
    if (m_dataReferenceEntries != 0) {
        delete [] m_dataReferenceEntries;
    }
}

void
DrefAtom::initialize(unsigned nOfEntries)
{
    if (m_dataReferenceEntries != 0) {
        delete [] m_dataReferenceEntries;
    }
    m_dataReferenceEntryCount = nOfEntries;
    m_dataReferenceEntries = new DataReferenceEntry[nOfEntries];
}

bool
DrefAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_dataReferenceEntryCount);
    initialize(m_dataReferenceEntryCount);
    MOV_DEBUG("(DREF)  nOfEntries: " << m_dataReferenceEntryCount);
    for (unsigned int i = 0; i < m_dataReferenceEntryCount; i++) {
	atomReader.readDW(m_dataReferenceEntries[i].refSize);
	atomReader.readDW(m_dataReferenceEntries[i].refType);
	atomReader.readDW(m_dataReferenceEntries[i].versionAndFlags);
    }
    return true;
}

bool
DrefAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_dataReferenceEntryCount);
    for (unsigned int i = 0; i < m_dataReferenceEntryCount; i++) {
	atomWriter.writeDW(m_dataReferenceEntries[i].refSize);
	atomWriter.writeDW(m_dataReferenceEntries[i].refType);
	atomWriter.writeDW(m_dataReferenceEntries[i].versionAndFlags);
    }

    return true;
}

unsigned
DrefAtom::getAtomSize()
{
    return 4*4 + m_dataReferenceEntryCount*(4+4+4);
}

bool DrefAtom::operator==(DrefAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_dataReferenceEntryCount != 
	leftAtom.m_dataReferenceEntryCount) return false;
    for (unsigned int i(0); i < m_dataReferenceEntryCount; i++) {
	if (m_dataReferenceEntries[i].refSize != 
	    leftAtom.m_dataReferenceEntries[i].refSize) return false;
	if (m_dataReferenceEntries[i].refType != 
	    leftAtom.m_dataReferenceEntries[i].refType) return false;
	if (m_dataReferenceEntries[i].versionAndFlags != 
	    leftAtom.m_dataReferenceEntries[i].versionAndFlags) return false;
    }
    return true;
}

bool DrefAtom::operator!=(DrefAtom& leftAtom)
{
    return !(*this == leftAtom);
}

