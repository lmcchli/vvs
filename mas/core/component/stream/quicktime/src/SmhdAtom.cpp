#include "SmhdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

SmhdAtom::SmhdAtom() 
    : Atom(SMHD, 8+4+2+2),
      m_versionAndFlags(0),
      m_balance(0),
      m_reserved(0)
{
}

bool
SmhdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
	MOV_DEBUG("		(SMHD) " << atomSize)
    atomReader.readDW(m_versionAndFlags);
    atomReader.readW(m_balance);
    atomReader.readW(m_reserved);
	MOV_DEBUG("		(SMHD) end");
    return true;
}

bool
SmhdAtom::saveGuts(AtomWriter& atomWriter)
{
	MOV_DEBUG("		(SMHD) save size: " << getAtomSize())
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeW(m_balance);
    atomWriter.writeW(m_reserved);
    return true;
}

bool SmhdAtom::operator==(SmhdAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_balance != leftAtom.m_balance) return false;
    if (m_reserved != leftAtom.m_reserved) return false;
    return true;
}

bool SmhdAtom::operator!=(SmhdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

