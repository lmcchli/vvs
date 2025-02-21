#include "MvhdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>
using std::cout;
using std::endl;

using namespace quicktime;

MvhdAtom::MvhdAtom() 
    : Atom(MVHD, 4+4+1+3+4+4+4+4+2+10+36+24+4+4),
      m_versionAndFlags(0),
      m_creationTime(0xbbf81e00),
      m_movieTimeScale(1000),
      m_movieDuration(1),
      m_preferredRate(0x10000),
      m_preferredVolume(0x100),
      m_nextTrackId(4)
{
    m_matrix[0] = 0x10000;
    m_matrix[1] = 0;
    m_matrix[2] = 0;
    
    m_matrix[3] = 0;
    m_matrix[4] = 0x10000;
    m_matrix[5] = 0;

    m_matrix[6] = 0;
    m_matrix[7] = 0;
    m_matrix[8] = 0x40000000;
}

bool
MvhdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(12,AtomReader::SEEK_FORWARD);
    atomReader.readDW(m_movieTimeScale);
    atomReader.readDW(m_movieDuration);
    atomReader.seek(76, AtomReader::SEEK_FORWARD);
    atomReader.readDW(m_nextTrackId);
    return true;
}

bool
MvhdAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.writeDW(m_versionAndFlags);
    atomWriter.writeDW(m_creationTime);
    atomWriter.writeDW(m_creationTime);
    atomWriter.writeDW(m_movieTimeScale);
    atomWriter.writeDW(m_movieDuration);
    atomWriter.writeDW(m_preferredRate);
    atomWriter.writeW(m_preferredVolume);
    atomWriter.seek(10, AtomWriter::SEEK_FORWARD);
    for (int i(0); i < 9; i++) {
	atomWriter.writeDW(m_matrix[i]);
    }
    atomWriter.seek(24, AtomWriter::SEEK_FORWARD);
    atomWriter.writeDW(m_nextTrackId);
    return true;
}

bool MvhdAtom::operator==(MvhdAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_movieTimeScale != leftAtom.m_movieTimeScale) return false;
    if (m_creationTime != leftAtom.m_creationTime) return false;
    if (m_movieDuration != leftAtom.m_movieDuration) return false;
    if (m_preferredRate != leftAtom.m_preferredRate) return false;
    if (m_preferredVolume != leftAtom.m_preferredVolume) return false;
    for (int i(0); i < 9; i++) {
	if (m_matrix[i] != leftAtom.m_matrix[i]) return false;
    }
    if (m_nextTrackId != leftAtom.m_nextTrackId) return false;
    return true;
}

bool MvhdAtom::operator!=(MvhdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

