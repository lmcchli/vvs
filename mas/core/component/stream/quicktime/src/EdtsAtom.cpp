#include "EdtsAtom.h"

#include "ElstAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>
using std::cout;
using std::endl;

using namespace quicktime;

EdtsAtom::EdtsAtom() 
    : Atom(EDTS)
{
}

EdtsAtom::~EdtsAtom()
{
}

bool
EdtsAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    int nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;

    while (nOfBytesLeft) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::ELST:
	    MOV_DEBUG("(EDTS) ELST " << subAtomSize);
	    m_editListAtom.restoreGuts(atomReader, subAtomSize);
	    break;
	    
	default:
	    MOV_DEBUG("Found unknown atom");
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("End of EDIT");
    return true;
}

bool
EdtsAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    m_editListAtom.saveGuts(atomWriter);
    return true;
}

unsigned
EdtsAtom::getAtomSize()
{
    unsigned size(8);

    size += m_editListAtom.getAtomSize();

    return size;
}

bool EdtsAtom::operator==(EdtsAtom& leftAtom)
{
    if (m_editListAtom != leftAtom.m_editListAtom) return false;
    return true;
}

bool EdtsAtom::operator!=(EdtsAtom& leftAtom)
{
    return !(*this == leftAtom);
}

