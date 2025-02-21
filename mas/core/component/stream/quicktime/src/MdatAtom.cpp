#include "MdatAtom.h"

using namespace quicktime;

MdatAtom::MdatAtom()
    : Atom(MDAT),
      m_size(0),
      m_offset(0)
{
}

MdatAtom::~MdatAtom()
{
}

unsigned
MdatAtom::seek(unsigned offset, unsigned mode)
{
    m_size += offset;
    return tell();
}

unsigned
MdatAtom::tell()
{
    return m_offset + 8 + m_size;
}

unsigned 
MdatAtom::writeByte(unsigned char word)
{
    m_size += 1;
    return 1;
}

unsigned 
MdatAtom::writeW(unsigned short word)
{
    m_size += 2;
    return 2;
}

unsigned 
MdatAtom::writeDW(unsigned word)
{
    m_size += 4;
    return 4;
}

unsigned
MdatAtom:: write(const char* data, unsigned size)
{
    m_size += size;
    return size;
}

bool
MdatAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    return true;
}

bool
MdatAtom::saveGuts(AtomWriter& atomWriter)
{
    return true;
}

bool MdatAtom::operator==(MdatAtom& leftAtom)
{
    return true;
}

bool MdatAtom::operator!=(MdatAtom& leftAtom)
{
    return !(*this == leftAtom);
}


