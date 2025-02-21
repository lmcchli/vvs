#include "MdiaAtom.h"

#include "HdlrAtom.h"
#include "MinfAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

MdiaAtom::MdiaAtom() 
    : Atom(MDIA)
{
}

bool
MdiaAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    unsigned nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;

    while (nOfBytesLeft) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::MDHD:
	    MOV_DEBUG("    (MDIA) MDHD " << subAtomSize);
	    m_mediaHeaderAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::HDLR:
	    MOV_DEBUG("    (MDIA) HDLR " << subAtomSize);
	    m_handlerReferenceAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::MINF:
	    MOV_DEBUG("    (MDIA) MINF " << subAtomSize);
	    m_mediaInformationAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	default:
            char unknownAtom[5];
	    unknownAtom[0] = (char)(subAtomName>>24)&0xff;
	    unknownAtom[1] = (char)(subAtomName>>16)&0xff;
	    unknownAtom[2] = (char)(subAtomName>>8)&0xff;
	    unknownAtom[3] = (char)(subAtomName>>0)&0xff;
	    unknownAtom[4] = '\0';
	    MOV_DEBUG("    (MDIA) " << unknownAtom << " " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("  End of MDIA");
    return true;
}

bool
MdiaAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    m_mediaHeaderAtom.saveGuts(atomWriter);
    m_handlerReferenceAtom.saveGuts(atomWriter);
    m_mediaInformationAtom.saveGuts(atomWriter);
    return true;
}

unsigned
MdiaAtom::getAtomSize() 
{
    unsigned size(8);

    size += m_mediaHeaderAtom.getAtomSize();
    size += m_handlerReferenceAtom.getAtomSize();
    size += m_mediaInformationAtom.getAtomSize();
    return size;
}

bool MdiaAtom::operator==(MdiaAtom& leftAtom)
{
    if (m_mediaHeaderAtom != 
	leftAtom.m_mediaHeaderAtom) return false;
    if (m_handlerReferenceAtom != 
	leftAtom.m_handlerReferenceAtom) return false;
    if (m_mediaInformationAtom != 
	leftAtom.m_mediaInformationAtom) return false;
    return true;
}

bool MdiaAtom::operator!=(MdiaAtom& leftAtom)
{
    return !(*this == leftAtom);
}

