#include "StsdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"
#include "AmrSampleDescription.h"
#include "PCMSoundSampleDescription.h"
#include "H263SampleDescription.h"

#include <string>


using namespace quicktime;
/*
 * Sample Description Table
 * contained in STBL.
 **/
 
StsdAtom::StsdAtom()
	//size here does not cover the sample entry atoms.
    : Atom(STSD, 4+4+1+3+4), //size + name(format) + version(1) flags (3) number of entries(4)
      m_sampleDescriptionEntry(NULL),
      m_versionAndFlags(0),
      m_entryCount(0) //the number of sample entries should be 1 or 0.
{
}

StsdAtom::~StsdAtom() {
	MOV_DEBUG("        (~STSD) ");
    if (m_sampleDescriptionEntry != NULL) {
		MOV_DEBUG("        (~STSD ~DAMR ");
        delete m_sampleDescriptionEntry;
        m_sampleDescriptionEntry = NULL;
    }
}

bool
StsdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_entryCount);
	MOV_DEBUG("        (STSD) restoreGuts");
    if (m_entryCount > 0) {
		if (m_sampleDescriptionEntry != NULL) {
			delete m_sampleDescriptionEntry;
		}
		unsigned subAtomName;
		unsigned subAtomSize;
		atomReader.readDW(subAtomSize);
		atomReader.readDW(subAtomName);
		switch (subAtomName) {
			case quicktime::SAMR:
				MOV_DEBUG("        (STSD) SAMR " << subAtomSize);
				m_sampleDescriptionEntry = (sampleDescriptionAtom*)new AmrSampleDescription(quicktime::SAMR);
				break;
			case quicktime::SAWB:
				MOV_DEBUG("        (STSD) SAWB " << subAtomSize);
				m_sampleDescriptionEntry = (sampleDescriptionAtom*) new AmrSampleDescription(quicktime::SAWB);
				break;
			case quicktime::ULAW:
				MOV_DEBUG("        (STSD) ULAW " << subAtomSize);
				m_sampleDescriptionEntry = (sampleDescriptionAtom*)new PCMSoundSampleDescription(quicktime::ULAW);
				break;
			case quicktime::ALAW:
				MOV_DEBUG("        (STSD) ALAW " << subAtomSize);
				m_sampleDescriptionEntry = (sampleDescriptionAtom*)new PCMSoundSampleDescription(quicktime::ALAW);
				break;
			case quicktime::S263:
				//Video.
				MOV_DEBUG("        (STSD) S263 " << subAtomSize);
				m_sampleDescriptionEntry = (sampleDescriptionAtom*)new H263SampleDescription();
				break;
			default:
				std::string sName;
				AtomReader::getAtomNameAsString((AtomName)subAtomName,sName);
				MOV_DEBUG("        (STSD) other: [" << sName << std::string("] size ") << subAtomSize);
				
				m_sampleDescriptionEntry = new sampleDescriptionAtom((AtomName)subAtomName);
				break;
		}
		m_sampleDescriptionEntry->restoreGuts(atomReader, subAtomSize);
	}
	if (m_entryCount > 1) {
			//for now we only support one sample type per moov track, so we just skip the rest.
			//for video and audio, they are usually in seperate tracks.
			unsigned subAtomSize;
			MOV_DEBUG("        (STSD) WARN more than one entry count Skipping remaining " << m_entryCount);
			m_entryCount=1;
			for (unsigned i(1) ;i < m_entryCount;i++) {
				unsigned length=atomReader.readDW(subAtomSize);
				 atomReader.seek(length-4, AtomReader::SEEK_FORWARD); //skipping the rest of the ATOM.
			}   
	}
    return true;
}

bool
StsdAtom::saveGuts(AtomWriter& atomWriter)
{
    MOV_DEBUG("        (STBL) STSD saveGuts " << getAtomSize());
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
	atomWriter.writeDW(m_entryCount);
    if (m_sampleDescriptionEntry != NULL) {
		m_sampleDescriptionEntry->saveGuts(atomWriter);
    } else {
		MOV_DEBUG("        (STBL) STSD no m_sampleDescriptionEntry written!") 
	}
    return true;
}


unsigned
StsdAtom::getAtomSize()
{
	unsigned size(4+4+1+3+4); //size + name(format) + version(1) flags (3) number of entries(4)
    if (m_sampleDescriptionEntry != NULL ) {
		size+=m_sampleDescriptionEntry->getAtomSize();
	}
    return size;
}

void 
StsdAtom::setSampleDescriptionEntry(sampleDescriptionAtom* sampleDescriptionEntry) {
	if ( sampleDescriptionEntry == NULL ) {
		MOV_DEBUG("STSD Setting sample description set to NULL");
		if ( m_sampleDescriptionEntry != NULL ) {
			MOV_DEBUG("STSD Setting sample description (deleted old value).")
			delete m_sampleDescriptionEntry;  //delete the old one if exists.
		}
		m_sampleDescriptionEntry=NULL;
		m_entryCount=0;
		return;
	}
	
	if ( m_sampleDescriptionEntry != NULL ) {
		delete m_sampleDescriptionEntry;  //delete the old one just to be safe.
		m_sampleDescriptionEntry=NULL;
		MOV_DEBUG("STSD Setting sample description (deleted old value).")
	}
	m_sampleDescriptionEntry=sampleDescriptionEntry;
	m_entryCount=1; //if we add an entry make sure the entry is set to 1.
	
	#ifdef MOV_DEBUG
		std::string *sName = 0;
		sName = m_sampleDescriptionEntry->getAtomNameAsString();
		MOV_DEBUG("STSD Setting sample description entry name (" << *sName << ")")
		MOV_DEBUG("STSD Setting sample description entry " <<std::hex <<((void*) sampleDescriptionEntry) <<" size " << sampleDescriptionEntry->getAtomSize() <<" bytes");
		if (m_sampleDescriptionEntry->getName() == quicktime::SAWB || m_sampleDescriptionEntry->getName() == quicktime::SAMR ) {
			AmrSpecificAtom *amrS = ((AmrSampleDescription*)m_sampleDescriptionEntry)->getAMRSpecificAtom();
			if (amrS == 0) {
				MOV_DEBUG("STSD Setting sample description entry (DAMR not yet initialized)")
			} else {
				MOV_DEBUG("STSD Setting sample description entry (DAMR)" << std::hex << ((void*) amrS) << " size " << amrS->getAtomSize())
			}
		} 
	#endif
}

/*This basically returns the name of the sample entry.
 * Which tells you basically what codec this trak is.
*/
unsigned 
StsdAtom::getDataFormat()
{
	if (m_sampleDescriptionEntry != 0 ) {
		return m_sampleDescriptionEntry->getName();
	} else {
		return 0;
	}
	
}

bool
StsdAtom::operator==(StsdAtom& leftAtom)
{
    return true;
}

bool
StsdAtom::operator!=(StsdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

