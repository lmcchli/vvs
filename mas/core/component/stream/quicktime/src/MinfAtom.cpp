#include "MinfAtom.h"

#include "StblAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

MinfAtom::MinfAtom() 
    : Atom(MINF),
      m_mediaInformationHeaderAtom(0)
{
}

MinfAtom::~MinfAtom()
{
	if (m_mediaInformationHeaderAtom != NULL ) {
		delete m_mediaInformationHeaderAtom;
		m_mediaInformationHeaderAtom=NULL;
	}
}

bool
MinfAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    int nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;

    while (nOfBytesLeft) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::DINF:
	    MOV_DEBUG("      (MINF) DINF " << subAtomSize);
	    m_dataInformationAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::STBL:
	    MOV_DEBUG("      (MINF) STBL " << subAtomSize);
	    m_sampleTableAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	default:
            char unknownAtom[5];
	    unknownAtom[0] = (char)(subAtomName>>24)&0xff;
	    unknownAtom[1] = (char)(subAtomName>>16)&0xff;
	    unknownAtom[2] = (char)(subAtomName>>8)&0xff;
	    unknownAtom[3] = (char)(subAtomName>>0)&0xff;
	    unknownAtom[4] = '\0';
	    MOV_DEBUG("      (MINF) " << unknownAtom << " " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("    End of MINF");
    return true;
}

bool
MinfAtom::saveGuts(AtomWriter& atomWriter)
{
	MOV_DEBUG("      (MINF) save " << getAtomSize() )
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    if (m_mediaInformationHeaderAtom != 0) {
		MOV_DEBUG("      (MINF) write mediaInformationHeaderAtom(SMHD or similar)  " )
		m_mediaInformationHeaderAtom->saveGuts(atomWriter);
    } else {
		MOV_DEBUG("      (MINF) WARN no mediaInformationHeaderAtom(SMHD or similar) - Mandatory. " )
	}
    m_handlerReferenceAtom.saveGuts(atomWriter);
    m_dataInformationAtom.saveGuts(atomWriter);
    m_sampleTableAtom.saveGuts(atomWriter);
    return true;
}

unsigned 
MinfAtom::getAtomSize()
{
    unsigned size(8);
    if (m_mediaInformationHeaderAtom != 0) {
		size += m_mediaInformationHeaderAtom->getAtomSize();
    }
    size += m_handlerReferenceAtom.getAtomSize();
    size += m_dataInformationAtom.getAtomSize();
    size += m_sampleTableAtom.getAtomSize();
    return size;
}

bool MinfAtom::operator==(MinfAtom& leftAtom)
{
    if (m_mediaInformationHeaderAtom != 0 && 
	leftAtom.m_mediaInformationHeaderAtom != 0) {
	/*
	if (*m_mediaInformationHeaderAtom != 
	    *(leftAtom.m_mediaInformationHeaderAtom)) {
	    return false;
	}
	*/
    } else if (m_mediaInformationHeaderAtom != 0 || 
	       leftAtom.m_mediaInformationHeaderAtom != 0) {
	return false;
    }
    if (m_handlerReferenceAtom != 
	leftAtom.m_handlerReferenceAtom) return false;
    if (m_dataInformationAtom != leftAtom.m_dataInformationAtom) return false;
    if (m_sampleTableAtom != leftAtom.m_sampleTableAtom) return false;
    return true;
}

bool MinfAtom::operator!=(MinfAtom& leftAtom)
{
    return !(*this == leftAtom);
}

