#include <string.h>
#include "HdlrAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

HdlrAtom::HdlrAtom() 
    : Atom(HDLR, 8+8+4+12+4),
      m_versionAndFlags(0),
      m_componentType(quicktime::MHLR),
      m_componentSubType(quicktime::ALIS),
      m_componentManufacturer(quicktime::APPL),
      m_componentFlags(0),
      m_componentFlagsMask(0)
{
    m_componentName = new char[4];
    strcpy(m_componentName, "AAA");
}

HdlrAtom::~HdlrAtom() {
    delete [] m_componentName;
}

bool
HdlrAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(8, AtomReader::SEEK_FORWARD);
    atomReader.readDW(m_componentSubType);
    atomReader.readDW(m_componentManufacturer);
    atomReader.readDW(m_componentFlags);
    atomReader.readDW(m_componentFlagsMask);
    delete [] m_componentName;
    // Reading the component name ensuring that the max lenght
    // does not exceede atom size due to previous offset-by-one-bug
    // in deposits by previous implementations (hence subtraction by 32+1).
    m_componentName = atomReader.readString(atomSize-33, false);
    return true;
}

bool
HdlrAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_componentType);
    atomWriter.writeDW(m_componentSubType);
    atomWriter.writeDW(m_componentManufacturer);
    atomWriter.writeDW(m_componentFlags);
    atomWriter.writeDW(m_componentFlagsMask);

    atomWriter.writeString(m_componentName, 255);
    return true;
}

bool HdlrAtom::operator==(HdlrAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_componentType != leftAtom.m_componentType) return false;
    if (m_componentSubType != leftAtom.m_componentSubType) return false;
    if (m_componentManufacturer != 
	leftAtom.m_componentManufacturer) return false;
    if (m_componentFlags != leftAtom.m_componentFlags) return false;
    if (m_componentFlagsMask != leftAtom.m_componentFlagsMask) return false;
    if (strcmp(m_componentName, leftAtom.m_componentName) != 0) return false;
    return true;
}

bool HdlrAtom::operator!=(HdlrAtom& leftAtom)
{
    return !(*this == leftAtom);
}

