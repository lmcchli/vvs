#include "AmrSampleDescription.h"

#include "AtomReader.h"
#include "AtomWriter.h"
#include <string>

using namespace quicktime;
//codec is either SAMR or SAWB
/*AMRSampleEntry ::=	BoxHeader
	Reserved_6
	Data-reference-index 2
	Reserved_8
	Reserved_2
	Reserved_2
	Reserved_4
	TimeScale 2
	Reserved_2
	AMRSpecificBox (DAMR)
*/
AmrSampleDescription::AmrSampleDescription(AtomName codec) :
    sampleDescriptionAtom(codec, 4 //size
	 + 4 //format=samr (nb) or sawb(wb)
	 + 6 //reserved_6
	 + 2 //data reference index
	 + 8 //reserved_8
	 + 2 //reserved_2
	 + 4 //reserved_4
	 + 2 //timescale
	 + 2 //reserved_2
	 ), //frames per sample
    //m_index(1), ///base class
    m_timeScale(0), //this class
	m_AMRSpecificBox(0)
{
		m_index=1;
		if (codec==SAMR) {
			m_codecName="SAMR";
		} else if (codec==SAWB) {
			m_codecName="SAWB";
		} else {
			//default.
			m_codecName="SAMR";
		}
}

AmrSampleDescription::~AmrSampleDescription() {
	#ifdef MOV_PRINT_DEBUG
		MOV_DEBUG(std::string("        ~(") << m_codecName << ")")
	#endif
	if ( m_AMRSpecificBox != 0 ) {
		#ifdef MOV_PRINT_DEBUG
			MOV_DEBUG(std::string("        ~(") << m_codecName << ") ~DAMR")
		#endif
		delete m_AMRSpecificBox;
		m_AMRSpecificBox=0;
	}
}

bool
AmrSampleDescription::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
	MOV_DEBUG("        (" << m_codecName << ") " << atomSize)
    m_atomSize = atomSize;
	unsigned readSize=8;
	//read AMRSampleEntry box 6.5 of 26244-f00 3gp.
    atomReader.seek(6, quicktime::AtomReader::SEEK_FORWARD); //skip reserved
	readSize+=6;
    atomReader.readW(m_index); //read data reference index
	readSize+=2;
    atomReader.seek(8 + 2 + 2 + 4, AtomReader::SEEK_FORWARD); //skip reserved_8,reserved_2,reserved_2,reserved_4
	readSize+=(8+2+2+4);
    atomReader.readW(m_timeScale); //read timescale
	readSize+=2;
	//skip reserved_2 of AMRSampleEntry
    atomReader.seek(2, AtomReader::SEEK_FORWARD);
	readSize+=2;
	if (atomSize > readSize) {
		unsigned subAtomName;
		unsigned subAtomSize;
		atomReader.readDW(subAtomSize);
		readSize+=4;
		atomReader.readDW(subAtomName);
		readSize+=4;
			if (m_AMRSpecificBox != 0) {
				//Delete it if a default one was created before the restore.
				delete m_AMRSpecificBox;
				m_AMRSpecificBox=0;
			}
			switch (subAtomName) {
				case quicktime::DAMR:
					MOV_DEBUG(std::string("        (") << m_codecName << ") " << "(DAMR) found " << subAtomSize)
					m_AMRSpecificBox = new AmrSpecificAtom();
					m_AMRSpecificBox->restoreGuts(atomReader,subAtomSize);
					break;
				default:
					#ifdef MOV_PRINT_DEBUG
						std::string aName;
						AtomReader::getAtomNameAsString((quicktime::AtomName)subAtomName,aName);
						MOV_DEBUG(std::string("        (") << m_codecName << ") " << "(" << aName << ") ATOM where DAMR should be - creating default")
					#endif
					m_AMRSpecificBox=0; //default - will be initialized below.
					//skip unknown..
					atomReader.seek(atomSize-readSize, AtomReader::SEEK_FORWARD); 
				break;
			}
	} else {
		MOV_DEBUG(std::string("        (") << m_codecName << ") (DAMR) missing - creating default")
	}
	if (m_AMRSpecificBox == 0 ) {
		switch (m_atomName) {
				case quicktime::SAMR:
					m_AMRSpecificBox= new AmrSpecificAtom(false); // default amr nb.
					break;
				case quicktime::SAWB:
					m_AMRSpecificBox= new AmrSpecificAtom(true); // default amr wb.
					break;
				default:
					//should never happen but just in case someone constructed wrong or new type.
					MOV_DEBUG(std::string("        AMR SAMPLE DESC - WARN unknown AMR type, assuming SAMR)"))
					m_AMRSpecificBox= new AmrSpecificAtom(false); // default amr nb.
					break;
		}
	}
	MOV_DEBUG(std::string("        End of (") << m_codecName << ")");
    return true;
}

/*AMRSampleEntry ::=	BoxHeader (size 4 , format 4)
	Reserved_6
	Data-reference-index 2
	Reserved_8
	Reserved_2
	Reserved_2
	Reserved_4
	TimeScale 2
	Reserved_2
	AMRSpecificBox (DAMR)
*/
bool
AmrSampleDescription::saveGuts(AtomWriter& atomWriter)
{
	getAMRSpecificAtom(); //create the atom if null.
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.seek(6, AtomWriter::SEEK_FORWARD); //skip reserved 6
    atomWriter.writeW(m_index); 
    atomWriter.seek(8 + 2 + 2 + 4, AtomWriter::SEEK_FORWARD); //skip Reserved_8,Reserved_2,Reserved_2,Reserved_4
    atomWriter.writeW(m_timeScale);
    atomWriter.seek(2, AtomWriter::SEEK_FORWARD); //skip the reserved 2
    //atomWriter.writeDW(4 + 4 + 4 + 1 + 2 + 1 + 1); //old code to write out damr size?
	m_AMRSpecificBox->saveGuts(atomWriter); //write out the damr.
	return true;
}

/*AMRSampleEntry ::=	BoxHeader (size 4 , format 4)
	Reserved_6
	Data-reference-index 2
	Reserved_8
	Reserved_2
	Reserved_2
	Reserved_4
	TimeScale 2
	Reserved_2
	AMRSpecificBox (DAMR)
*/
unsigned AmrSampleDescription::getAtomSize() {
	unsigned size(4 //size
	 + 4 //format=samr or sawb
	 + 6 //reserved_6
	 + 2 //data reference index
	 + 8 //reserved_8
	 + 2 //reserved_2
	 + 2 //reserved_2
	 + 4 //reserved_4
	 + 2 //timescale
	 + 2 //reserved_2
	 ); //size before the DAMR or specificBox
	 if (m_AMRSpecificBox != 0 ) { //NOTE make sure to set the box before getting the size when writing.0
		 //add on the specific box.
		size+=m_AMRSpecificBox->getAtomSize();
	 }
    return size;
}

AmrSpecificAtom * AmrSampleDescription::getAMRSpecificAtom() {
	if (m_AMRSpecificBox == 0 )
	{
		switch (m_atomName) {
				case quicktime::SAMR:
					MOV_DEBUG(std::string("        (SAMR) (DAMR) creating NB DAMR)"))
					m_AMRSpecificBox= new AmrSpecificAtom(false); // default amr nb.
					break;
				case quicktime::SAWB:
					MOV_DEBUG("        (SAWB) (DAMR) creating WB DAMR)")
					m_AMRSpecificBox= new AmrSpecificAtom(true); // default amr wb
					break;
				default:
					//should never happen but just in case someone constructed wrong or new type.
					MOV_DEBUG("        (SAMPLEDESC) (DAMR) WARN unknown AMR type, assuming SAMR)")
					m_AMRSpecificBox= new AmrSpecificAtom(false); // default amr nb.
					break;
		}
	}
	return m_AMRSpecificBox;
}

bool AmrSampleDescription::operator==(AmrSampleDescription& leftAtom)
{
    return m_index == leftAtom.m_index
	&& m_timeScale == leftAtom.m_timeScale;
}

bool AmrSampleDescription::operator!=(AmrSampleDescription& leftAtom)
{
    return !(*this == leftAtom);
}

