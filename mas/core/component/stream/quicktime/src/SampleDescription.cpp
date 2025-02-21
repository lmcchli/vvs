#include "SampleDescription.h"

using namespace quicktime;

sampleDescriptionAtom::sampleDescriptionAtom(AtomName codec)
    : Atom(codec,4+4+6+2), //size + atomname + reserved(6) + 2 index.
      m_index(1)
{
}

sampleDescriptionAtom::sampleDescriptionAtom(AtomName codec,unsigned size)
    : Atom(codec,size), //size + atomname + reserved(6) + 2 index.
      m_index(1)
{
}



sampleDescriptionAtom::~sampleDescriptionAtom()
{
	MOV_DEBUG("		   	(~" << getName() << ")" );
	if (m_innerData !=0 ) {
		delete m_innerData;
		m_innerData=0;
	}
}

unsigned
sampleDescriptionAtom::seek(unsigned offset, unsigned mode)
{
    m_size += offset;
    return tell();
}

unsigned
sampleDescriptionAtom::tell()
{
    return m_size + 8;
}

unsigned 
sampleDescriptionAtom::writeByte(unsigned char word)
{
    m_size += 1;
    return 1;
}

unsigned 
sampleDescriptionAtom::writeW(unsigned short word)
{
    m_size += 2;
    return 2;
}

unsigned 
sampleDescriptionAtom::writeDW(unsigned word)
{
    m_size += 4;
    return 4;
}


//use the specific sample saveGuts to do a real write
//or get the innerdata and alter it.
unsigned
sampleDescriptionAtom::write(const char* data, unsigned size)
{
    m_size += size;
    return size;
}

bool
sampleDescriptionAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
	m_atomSize = atomSize;
	m_innerSize = m_atomSize-8; // remove the size and atomname
	atomReader.seek(6,AtomReader::SEEK_FORWARD);// skip reserved
	m_innerSize-=6;
	atomReader.readW(m_index); //read the index
	m_innerSize-=2;
	m_innerData = new unsigned char[m_innerSize];
	atomReader.read(m_innerData, m_innerSize);
    return true;
}

bool
sampleDescriptionAtom::saveGuts(AtomWriter& atomWriter)
{
	atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.seek(6, AtomWriter::SEEK_FORWARD); //skip reserved, writes 0's
	atomWriter.writeW(m_index); //writeout the index 
	if (m_innerSize > 0) {
		//finnally write out the innerSize.
		for (unsigned i(0);i<m_innerSize;i++) {
			atomWriter.writeByte(m_innerData[i]);
		}
	}
    return true;
}

unsigned 
sampleDescriptionAtom::getAtomSize() {
	unsigned size(4+4+6+2); //size + atomname + reserved(6) + 2 index.
    size+=m_innerSize; //raw contents.
    return size;
}

bool sampleDescriptionAtom::operator==(sampleDescriptionAtom& leftAtom)
{
    return (*this == leftAtom);
}

bool sampleDescriptionAtom::operator!=(sampleDescriptionAtom& leftAtom)
{
    return !(*this == leftAtom);
}


