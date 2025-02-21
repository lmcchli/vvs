#include "TrefAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

TrefAtom::TrefAtom()
    : Atom(TREF),
      m_trackReferenceSize(0),
      m_trackReferenceType(0),
      m_trackIdEntryCount(0),
      m_trackIdEntries(0)
{
}

TrefAtom::~TrefAtom()
{
    if (m_trackIdEntries != 0) delete [] m_trackIdEntries;
}

void
TrefAtom::initialize(unsigned trackType, unsigned trackId)
{
    m_trackReferenceSize = 12;
    m_trackReferenceType = trackType;
    m_trackIdEntryCount = 1;
    if (m_trackIdEntries != 0) delete [] m_trackIdEntries;
    m_trackIdEntries = new unsigned[1];
    m_trackIdEntries[0] = trackId;
}

bool
TrefAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    if (atomSize > 0 && atomSize - 8 > 0) {
	atomReader.readDW(m_trackReferenceSize);
	atomReader.readDW(m_trackReferenceType);
	initialize(m_trackReferenceType, 2);
	for (unsigned int i = 0; i < m_trackIdEntryCount; i++) {
	    atomReader.readDW(m_trackIdEntries[i]);
	}
    }
    return true;
}

bool
TrefAtom::saveGuts(AtomWriter& atomWriter)
{
    if (m_trackReferenceSize > 0) {
	atomWriter.writeDW(getAtomSize());
	atomWriter.writeDW(getName());
	atomWriter.writeDW(m_trackReferenceSize);
	atomWriter.writeDW(m_trackReferenceType);
	for (unsigned int i = 0; i < m_trackIdEntryCount; i++) {
	    atomWriter.writeDW(m_trackIdEntries[i]);
	}
    }

    return true;
}

unsigned
TrefAtom::getAtomSize()
{
    if (m_trackReferenceSize == 0) return 0;
    return 4*4 + m_trackIdEntryCount*4;
}

bool TrefAtom::operator==(TrefAtom& leftAtom)
{
    if (m_trackReferenceSize != leftAtom.m_trackReferenceSize) return false;
    if (m_trackIdEntryCount != leftAtom.m_trackIdEntryCount) return false;
    for (unsigned int i(0); i < m_trackIdEntryCount; i++) {
	if (m_trackIdEntries[i] != leftAtom.m_trackIdEntries[i]) return false;
    }
    return true;
}

bool TrefAtom::operator!=(TrefAtom& leftAtom)
{
    return !(*this == leftAtom);
}

