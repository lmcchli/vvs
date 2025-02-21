#include "FtypAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

FtypAtom::FtypAtom()
    : Atom(FTYP),
      m_majorBrand(createAtomName('3', 'g', 'p', '5')),
      m_minorVersion(7 * 256 + 0), //For 5.7.0
      m_compatibleVersion1(m_majorBrand) {
}

FtypAtom::~FtypAtom() {
}

bool
FtypAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize) {
    m_atomSize = atomSize;
    atomReader.readDW(m_majorBrand);
    atomReader.readDW(m_minorVersion);
    atomReader.readDW(m_compatibleVersion1);
    // TODO: fix this cludge due to correct differences in our an QT
    if (atomSize-20 > 0) atomReader.seek(atomSize-20, AtomReader::SEEK_FORWARD);
    MOV_DEBUG("(FTYP)  majorBrand: " << m_majorBrand);
    MOV_DEBUG("(FTYP)  minorVersion: " << m_minorVersion);
    MOV_DEBUG("(FTYP)  compatibleVersio1: " << m_compatibleVersion1);
    return true;
}

bool
FtypAtom::saveGuts(AtomWriter& atomWriter) {
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_majorBrand);
    atomWriter.writeDW(m_minorVersion);
    atomWriter.writeDW(m_compatibleVersion1);
    return true;
}

unsigned
FtypAtom::getAtomSize() {
    return 8 + 3*4;
}

bool FtypAtom::operator==(FtypAtom& leftAtom) {
    return m_majorBrand == leftAtom.m_majorBrand
        && m_minorVersion == leftAtom.m_minorVersion
        && m_compatibleVersion1 == leftAtom.m_compatibleVersion1;
}

bool FtypAtom::operator!=(FtypAtom& leftAtom) {
    return !(*this == leftAtom);
}

