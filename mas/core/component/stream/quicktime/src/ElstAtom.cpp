#include "ElstAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

ElstAtom::ElstAtom()
    : Atom(ELST),
      m_versionAndFlags(0),
      m_editListEntryCount(0),
      m_editListEntries(0)
{
    initialize(2);
    m_editListEntries[0].mediaTime = (unsigned)-1;
}

ElstAtom::~ElstAtom()
{
    if (m_editListEntries != 0) delete [] m_editListEntries;
}

void
ElstAtom::initialize(unsigned nOfEntries)
{
    m_editListEntryCount = nOfEntries;

    if (m_editListEntries != 0) delete [] m_editListEntries;
    m_editListEntries = new EditListEntry[nOfEntries];
    for (unsigned int i(0); i <  m_editListEntryCount; i++)  {
        m_editListEntries[i].trackDuration = 0;
        m_editListEntries[i].mediaTime = 0;
        m_editListEntries[i].mediaRate = 0;
    }
}

bool
ElstAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_editListEntryCount);
    initialize(m_editListEntryCount);
    MOV_DEBUG("(ELST)  nOfEntries: " << m_editListEntryCount);
    for (unsigned int i = 0; i < m_editListEntryCount; i++) {
	atomReader.readDW(m_editListEntries[i].trackDuration);
	atomReader.readDW(m_editListEntries[i].mediaTime);
	atomReader.readDW(m_editListEntries[i].mediaRate);
    }
    return true;
}

bool
ElstAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_editListEntryCount);
    for (unsigned int i = 0; i < m_editListEntryCount; i++) {
	atomWriter.writeDW(m_editListEntries[i].trackDuration);
	atomWriter.writeDW(m_editListEntries[i].mediaTime);
	atomWriter.writeDW(m_editListEntries[i].mediaRate);
    }

    return true;
}

unsigned
ElstAtom::getAtomSize()
{
    return 4*4 + m_editListEntryCount*(4+4+4);
}

bool ElstAtom::operator==(ElstAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_editListEntryCount != leftAtom.m_editListEntryCount) return false;
    for (unsigned int i(0); i < m_editListEntryCount; i++) {
	if (m_editListEntries[i].trackDuration != 
	    leftAtom.m_editListEntries[i].trackDuration) return false;
	if (m_editListEntries[i].mediaTime != 
	    leftAtom.m_editListEntries[i].mediaTime) return false;
	if (m_editListEntries[i].mediaRate != 
	    leftAtom.m_editListEntries[i].mediaRate) return false;
    }
    return true;
}

bool ElstAtom::operator!=(ElstAtom& leftAtom)
{
    return !(*this == leftAtom);
}

ElstAtom&
ElstAtom::operator=(const ElstAtom& o) {
    if (this != &o) {
	delete [] m_editListEntries;
	m_versionAndFlags = o.m_versionAndFlags;
	m_editListEntryCount = o.m_editListEntryCount;
	m_editListEntries = new EditListEntry[m_editListEntryCount];
	for (unsigned int i = 0; i < m_editListEntryCount; i++) {
	    m_editListEntries[i].trackDuration = o.m_editListEntries[i].trackDuration;
	    m_editListEntries[i].mediaTime = o.m_editListEntries[i].mediaTime;
	    m_editListEntries[i].mediaRate = o.m_editListEntries[i].mediaRate;
	}
    }
    return *this;
}
