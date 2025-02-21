#include "Atom.h"

using namespace quicktime;

Atom::Atom(AtomName name, unsigned size)
    : m_atomName(name),
      m_atomSize(size),
      m_innerData(0),
      m_innerSize(0)
{
	m_atomNameString=NULL;
	m_innerData=NULL;
}

Atom::~Atom()
{
    if (m_innerData != NULL) {
        delete [] m_innerData;
        m_innerData = NULL;
    }
	if (m_atomNameString != NULL ) {
		delete m_atomNameString;
		m_atomNameString = NULL;
	}
}

std::string* Atom::getAtomNameAsString() {
	if (m_atomNameString == NULL) { //first time populate the string.
		m_atomNameString = new std::string();
		*m_atomNameString+=(char)(m_atomName>>24);
		*m_atomNameString+=(char)m_atomName>>16;
		*m_atomNameString+=(char)m_atomName>>8;
		*m_atomNameString+=(char)m_atomName>>0;
	}
	
	return m_atomNameString;
}

unsigned Atom::getInnerSize() 
{
    return m_innerSize;
}

int Atom::getAsInt(unsigned index)
{
    int result(0);
    if (index < m_innerSize && (index +3) < m_innerSize) { 
        result += ((int)m_innerData[index+0])<<24;
	    result += ((int)m_innerData[index+1])<<16;
	    result += ((int)m_innerData[index+2])<<8;
	    result += ((int)m_innerData[index+3])<<0;
    }
    return result;
}

short Atom::getAsShort(unsigned index)
{
    short result(0);
    if (index < m_innerSize && (index +1) < m_innerSize) { 
	    result += ((int)m_innerData[index+0])<<8;
	    result += ((int)m_innerData[index+1])<<0;
    }
    return result;
}

char Atom::getAsChar(unsigned index)
{
    char result(0);
    if (index < m_innerSize && (index +0) < m_innerSize) { 
	    result += ((int)m_innerData[index+0])<<0;
    }
    return result;
}

