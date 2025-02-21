#include "HintSampleDescription.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

HintSampleDescription::HintSampleDescription() 
    : sampleDescriptionAtom(RTP, 8+4+2+2+20)
{
	m_index=1;
    m_data[0] = 0x00010001;
    m_data[1] = 0x000005dc;
    m_data[2] = 0x0000000c;
    m_data[3] = 0x74696d73;
    m_data[4] = 0x00015f90;
}

bool
HintSampleDescription::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(6, AtomReader::SEEK_FORWARD);
    atomReader.readW(m_index);
    for (int i(0); i < 5; i++)
	atomReader.readDW(m_data[i]);
    return true;
}

bool
HintSampleDescription::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.seek(6, AtomWriter::SEEK_FORWARD);
    atomWriter.writeW(m_index);
    for (int i(0); i < 5; i++)
	atomWriter.writeDW(m_data[i]);
    return true;
}

bool HintSampleDescription::operator==(HintSampleDescription& leftAtom)
{
    if (m_index != leftAtom.m_index) return false; 
    for (int i(0); i < 5; i++)
	if (m_data[i] != leftAtom.m_data[i]) return false;
    return true;
}

bool HintSampleDescription::operator!=(HintSampleDescription& leftAtom)
{
    return !(*this == leftAtom);
}

