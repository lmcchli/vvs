#include "VmhdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

VmhdAtom::VmhdAtom() 
    : Atom(VMHD, 8+4+2+2+2+2),
      m_versionAndFlags(1),
      m_graphicsMode(0x0040),
      m_redOpColor(0x8000),
      m_greenOpColor(0x8000),
      m_blueOpColor(0x8000)
{
}

bool
VmhdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readW(m_graphicsMode);
    atomReader.readW(m_redOpColor);
    atomReader.readW(m_greenOpColor);
    atomReader.readW(m_blueOpColor);
    return true;
}

bool
VmhdAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeW(m_graphicsMode);
    atomWriter.writeW(m_redOpColor);
    atomWriter.writeW(m_greenOpColor);
    atomWriter.writeW(m_blueOpColor);
    return true;
}

bool VmhdAtom::operator==(VmhdAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_graphicsMode != leftAtom.m_graphicsMode) return false;
    if (m_redOpColor != leftAtom.m_redOpColor) return false;
    if (m_greenOpColor != leftAtom.m_greenOpColor) return false;
    if (m_blueOpColor != leftAtom.m_blueOpColor) return false;
    return true;
}

bool VmhdAtom::operator!=(VmhdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

