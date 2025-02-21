#include "StblAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

StblAtom::StblAtom() 
    : Atom(STBL)
{
}

bool
StblAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    int nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;

    while (nOfBytesLeft) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::STSD:
	    MOV_DEBUG("        (STBL) STSD " << subAtomSize);
	    m_sampleDescriptionAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::STTS:
	    MOV_DEBUG("        (STBL) STTS " << subAtomSize);
	    m_timeToSampleAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::STSS:
	    MOV_DEBUG("        (STBL) STTS " << subAtomSize);
	    m_syncSampleAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::STSC:
	    MOV_DEBUG("        (STBL) STSC " << subAtomSize);
	    m_sampleToChunkAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::STSZ:
	    MOV_DEBUG("        (STBL) STSZ " << subAtomSize);
	    m_sampleSizeAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::STCO:
	    MOV_DEBUG("        (STBL) STCO " << subAtomSize);
	    m_chunkOffsetAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	default:
		std::string sName;
		AtomReader::getAtomNameAsString((AtomName)subAtomName,sName);
	    MOV_DEBUG("        (STBL) " << sName << std::string(" (ignored) ") << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("      End of STBL");
    return true;
}

bool
StblAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("        (MINF) STBL " << getAtomSize());
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    m_sampleDescriptionAtom.saveGuts(atomWriter);
    m_timeToSampleAtom.saveGuts(atomWriter);
    m_syncSampleAtom.saveGuts(atomWriter);
    m_sampleToChunkAtom.saveGuts(atomWriter);
    m_sampleSizeAtom.saveGuts(atomWriter);
    m_chunkOffsetAtom.saveGuts(atomWriter);

    return true;
}

unsigned
StblAtom::getAtomSize()
{
    unsigned size(8);

    size += m_sampleDescriptionAtom.getAtomSize();
    size += m_timeToSampleAtom.getAtomSize();
    size += m_syncSampleAtom.getAtomSize();
    size += m_sampleToChunkAtom.getAtomSize();
    size += m_sampleSizeAtom.getAtomSize();
    size += m_chunkOffsetAtom.getAtomSize();
    return size;
}

bool StblAtom::operator==(StblAtom& leftAtom)
{
    if (m_sampleDescriptionAtom != 
	leftAtom.m_sampleDescriptionAtom) return false;
    if (m_timeToSampleAtom != leftAtom.m_timeToSampleAtom) return false;
    if (m_syncSampleAtom != leftAtom.m_syncSampleAtom) return false;
    if (m_sampleToChunkAtom != leftAtom.m_sampleToChunkAtom) return false;
    if (m_sampleToChunkAtom != leftAtom.m_sampleToChunkAtom) return false;
    if (m_chunkOffsetAtom != leftAtom.m_chunkOffsetAtom) return false;
    return true;
}

bool StblAtom::operator!=(StblAtom& leftAtom)
{
    return !(*this == leftAtom);
}

