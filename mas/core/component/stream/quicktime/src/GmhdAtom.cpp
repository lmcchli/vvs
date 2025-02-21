#include "GmhdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

GmhdAtom::GmhdAtom() 
    : Atom(GMHD, 8+8+4+12),
      m_gminSize(8+4+12),
      m_gminName(GMIN),
      m_versionAndFlags(0),
      m_graphicsMode(0x0040),
      m_redOpColor(0x8000),
      m_greenOpColor(0x8000),
      m_blueOpColor(0x8000),
      m_balance(0),
      m_reserved(0)
{
}

bool
GmhdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_gminSize);
    atomReader.readDW(m_gminName);
    atomReader.readDW(m_versionAndFlags);
    atomReader.readW(m_graphicsMode);
    atomReader.readW(m_redOpColor);
    atomReader.readW(m_greenOpColor);
    atomReader.readW(m_blueOpColor);
    atomReader.readW(m_balance);
    atomReader.readW(m_reserved);
    return true;
}

bool
GmhdAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_gminSize);
    atomWriter.writeDW(m_gminName);
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeW(m_graphicsMode);
    atomWriter.writeW(m_redOpColor);
    atomWriter.writeW(m_greenOpColor);
    atomWriter.writeW(m_blueOpColor);
    atomWriter.writeW(m_balance);
    atomWriter.writeW(m_reserved);
    return true;
}

bool GmhdAtom::operator==(GmhdAtom& leftAtom)
{
    if (m_gminSize != leftAtom.m_gminSize) return false;
    if (m_gminName != leftAtom.m_gminName) return false;
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_graphicsMode != leftAtom.m_graphicsMode) return false;
    if (m_redOpColor != leftAtom.m_redOpColor) return false;
    if (m_greenOpColor != leftAtom.m_greenOpColor) return false;
    if (m_blueOpColor != leftAtom.m_blueOpColor) return false;
    if (m_balance != leftAtom.m_balance) return false;
    if (m_reserved != leftAtom.m_reserved) return false;
    return true;
}

bool GmhdAtom::operator!=(GmhdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

