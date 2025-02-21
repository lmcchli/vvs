#include "MdhdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

MdhdAtom::MdhdAtom() 
    : Atom(MDHD, 8+4+16+2+2),
      m_versionAndFlags(0),
      m_creationTime(0xbbf81e00),
      m_timeScale(0),
      m_duration(0),
      m_language(0),
      m_quality(0)
{
}

bool
MdhdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_creationTime);
    atomReader.readDW(m_creationTime);
    atomReader.readDW(m_timeScale);
    atomReader.readDW(m_duration);
    atomReader.readW(m_language);
    atomReader.readW(m_quality);
    return true;
}

bool
MdhdAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_creationTime);
    atomWriter.writeDW(m_creationTime);
    atomWriter.writeDW(m_timeScale);
    atomWriter.writeDW(m_duration);
    atomWriter.writeW(m_language);
    atomWriter.writeW(m_quality);
    return true;
}

bool MdhdAtom::operator==(MdhdAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_creationTime != leftAtom.m_creationTime) return false;
    if (m_timeScale != leftAtom.m_timeScale) return false;
    if (m_duration != leftAtom.m_duration) return false;
    if (m_language != leftAtom.m_language) return false;
    if (m_quality != leftAtom.m_quality) return false;
    return true;
}

bool MdhdAtom::operator!=(MdhdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

