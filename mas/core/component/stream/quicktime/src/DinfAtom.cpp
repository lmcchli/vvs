#include "DinfAtom.h"

#include "DrefAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>
using std::cout;
using std::endl;

using namespace quicktime;

DinfAtom::DinfAtom() 
    : Atom(DINF)
{
}

DinfAtom::~DinfAtom()
{
}

bool
DinfAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    int nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;

    while (nOfBytesLeft > 0) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::DREF:
	    MOV_DEBUG("(DINF) DREF " << subAtomSize);
	    m_dataReferenceAtom.restoreGuts(atomReader, subAtomSize);
	    break;
	    
	default:
	    MOV_DEBUG("DINF Found unknown atom [" 
		      << (char)((subAtomName>>0x18)&0xff)
		      << (char)((subAtomName>>0x10)&0xff)
		      << (char)((subAtomName>>0x08)&0xff)
		      << (char)((subAtomName>>0x00)&0xff)
		      << "]"
		      << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("End of DINF");
    return true;
}

bool
DinfAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("Save DINF " << getAtomSize());
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    m_dataReferenceAtom.saveGuts(atomWriter);
    return true;
}

unsigned
DinfAtom::getAtomSize()
{
    unsigned size(8);

    size += m_dataReferenceAtom.getAtomSize();

    return size;
}

bool DinfAtom::operator==(DinfAtom& leftAtom)
{
    if (m_dataReferenceAtom != leftAtom.m_dataReferenceAtom) return false;
    return true;
}

bool DinfAtom::operator!=(DinfAtom& leftAtom)
{
    return !(*this == leftAtom);
}

