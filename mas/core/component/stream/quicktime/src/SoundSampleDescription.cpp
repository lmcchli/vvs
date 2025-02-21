#include "PCMSoundSampleDescription.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

PCMSoundSampleDescription::PCMSoundSampleDescription(AtomName codec) 
    : Atom(codec, 8+4+2+2+20),
      m_index(1)
{
    m_data[0] = 0x00000000;
    m_data[1] = 0x00000000;
    m_data[2] = 0x00010010;
    m_data[3] = 0x00000000;
    m_data[4] = 0x1f400000;
}

bool
PCMSoundSampleDescription::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(6, AtomReader::SEEK_FORWARD);
    atomReader.readW(m_index);
    for (int i(0); i < 5; i++)
	atomReader.readDW(m_data[i]);
    return true;
}

bool
PCMSoundSampleDescription::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.seek(6, AtomWriter::SEEK_FORWARD);
    atomWriter.writeW(m_index);
    for (int i(0); i < 5; i++)
	atomWriter.writeDW(m_data[i]);
    return true;
}

bool PCMSoundSampleDescription::operator==(PCMSoundSampleDescription& leftAtom)
{
    if (m_index != leftAtom.m_index) return false; 
    for (int i(0); i < 5; i++)
	if (m_data[i] != leftAtom.m_data[i]) return false;
    return true;
}

bool PCMSoundSampleDescription::operator!=(PCMSoundSampleDescription& leftAtom)
{
    return !(*this == leftAtom);
}

