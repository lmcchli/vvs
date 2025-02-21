#include "AmrSpecificAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"
#include <iostream> 

using namespace quicktime;

/*
 from 26244 3gp section 6.7 AmrSpecificBox
 BoxHeader.Size Unsigned int(32)
BoxHeader.Type Unsigned int(32) 'damr'

 * followed by:
 struct AMRDecSpecStruc{
	Unsigned int (32)	vendor
	Unsigned int (8)	decoder_version
	Unsigned int (16)	mode_set
	Unsigned int (8)	mode_change_period
	Unsigned int (8)	frames_per_sample
}
*/
AmrSpecificAtom::AmrSpecificAtom() :
    Atom(DAMR, 4 //size
	 + 4 //type=damr
	 + 4 //vendor
	 + 1 //decoder version
	 + 2 //mode set
	 + 1 //mode change period
	 + 1), //frames per sample
	m_vendor(vendor),
	m_decoderVersion(0),
	m_modeSet(modeSetNB), //assume NB...
	m_modeChangePeriod(modeChangePeriod),
	m_framesPerSample(framesPerSample)
{
}

//constructor - makes WB (true) or NB (false)
AmrSpecificAtom::AmrSpecificAtom(bool wideBand) :
	Atom(DAMR, 4 //size
	 + 4 //type=damr
	 + 4 //vendor
	 + 1 //decoder version
	 + 2 //mode set
	 + 1 //mode change period
	 + 1), //frames per sample
	m_vendor(vendor),
	m_decoderVersion(0),
	m_modeChangePeriod(modeChangePeriod),
	m_framesPerSample(framesPerSample)
{
	if (wideBand == true ) {
		m_modeSet = modeSetWB;
	} else {
		m_modeSet = modeSetNB;
	}
}

/*
 from 26244 3gp section 6.7 AmrSpecificBox
 BoxHeader.Size Unsigned int(32)
BoxHeader.Type Unsigned int(32) 'damr'

 * followed by:
 struct AMRDecSpecStruc{
	Unsigned int (32)	vendor
	Unsigned int (8)	decoder_version
	Unsigned int (16)	mode_set
	Unsigned int (8)	mode_change_period
	Unsigned int (8)	frames_per_sample
}
*/
bool
AmrSpecificAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
	MOV_DEBUG("		   	(DAMR) restoreGuts");
    m_atomSize = atomSize;
	MOV_DEBUG("		   	(DAMR) :");
	atomReader.readDW(m_vendor);
	MOV_DEBUG("					vendor: " << m_vendor)
	atomReader.readByte(m_decoderVersion);
	MOV_DEBUG("					decoder_version: " << (unsigned)m_decoderVersion)
	atomReader.readW(m_modeSet);
	MOV_DEBUG("					mode_set: 0x" << std::hex << m_modeSet )
	atomReader.readByte(m_modeChangePeriod);
	MOV_DEBUG("					mode_change_period: " << (unsigned)m_modeChangePeriod)
	atomReader.readByte(m_framesPerSample);
	MOV_DEBUG("					frames_per_sample: " << (unsigned)m_framesPerSample)
	MOV_DEBUG("        	(DAMR) End");
	return true;
}

/*
 from 26244 3gp section 6.7 AmrSpecificBox
 BoxHeader.Size Unsigned int(32)
BoxHeader.Type Unsigned int(32) 'damr'

 * followed by:
 struct AMRDecSpecStruc{
	Unsigned int (32)	vendor
	Unsigned int (8)	decoder_version
	Unsigned int (16)	mode_set
	Unsigned int (8)	mode_change_period
	Unsigned int (8)	frames_per_sample
}
*/
bool
AmrSpecificAtom::saveGuts(AtomWriter& atomWriter)
{
	MOV_DEBUG("		   	(DAMR) saveGuts");
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_vendor); //eric typically
    atomWriter.writeByte(m_decoderVersion);
    atomWriter.writeW(m_modeSet);
    atomWriter.writeByte(m_modeChangePeriod);
    atomWriter.writeByte(m_framesPerSample);
    return true;
}

/*
 from 26244 3gp section 6.7 AmrSpecificBox
 BoxHeader.Size Unsigned int(32)
BoxHeader.Type Unsigned int(32) 'damr'

 * followed by:
 struct AMRDecSpecStruc{
	Unsigned int (32)	vendor
	Unsigned int (8)	decoder_version
	Unsigned int (16)	mode_set
	Unsigned int (8)	mode_change_period
	Unsigned int (8)	frames_per_sample
}
*/
unsigned AmrSpecificAtom::getAtomSize() {
	unsigned size(4 //size
	 + 4 //type=damr
	 + 4 //vendor
	 + 1 //decoder version
	 + 2 //mode set
	 + 1 //mode change period
	 + 1); //frames per sample
    return size;
}

bool AmrSpecificAtom::operator==(AmrSpecificAtom& leftAtom)
{
    return m_modeSet==m_modeSet && m_modeSet == m_modeSet &&
	m_modeChangePeriod==m_modeChangePeriod && m_framesPerSample == m_framesPerSample;
}

bool AmrSpecificAtom::operator!=(AmrSpecificAtom& leftAtom)
{
    return !(*this == leftAtom);
}

