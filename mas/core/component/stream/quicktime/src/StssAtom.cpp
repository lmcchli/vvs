#include "StssAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

StssAtom::StssAtom() 
    : Atom(STSS, 0),
      m_syncSampleEntryCount(0),
      m_syncSampleEntries(0)
{
}

bool
StssAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    if (m_atomSize == 0) return false;
    atomReader.seek(m_atomSize-8, AtomReader::SEEK_FORWARD);
    return true;
}

bool
StssAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("        (STBL) STSS " << getAtomSize());
    unsigned atomSize(getAtomSize());
    if (atomSize == 0) return true;
    atomWriter.writeDW(atomSize);
    atomWriter.writeDW(getName());
    for (unsigned int i(0); i < m_syncSampleEntryCount; i++)
	atomWriter.writeDW(m_syncSampleEntries[i]);
    return true;
}

bool StssAtom::operator==(StssAtom& leftAtom)
{
    if (getAtomSize() != 0) return false;
    if (getAtomSize() != leftAtom.getAtomSize()) return false;
    return true;
}

bool StssAtom::operator!=(StssAtom& leftAtom)
{
    return !(*this == leftAtom);
}

