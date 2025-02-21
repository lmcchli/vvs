#include "TkhdAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;
/*8.3.2.2
 Syntax
aligned(8) class TrackHeaderBox
extends FullBox(‘tkhd’, version, flags){
if (version==1) {
unsigned int(64) creation_time;
unsigned int(64) modification_time;
unsigned int(32) track_ID;
const unsigned int(32) reserved = 0;
unsigned int(64) duration;
} else { // version==0
unsigned int(32) creation_time;
unsigned int(32) modification_time;
unsigned int(32) track_ID;
const unsigned int(32) reserved = 0;
unsigned int(32) duration;
}
const unsigned int(32)[2] reserved = 0;
template int(16) layer = 0;
template int(16) alternate_group = 0;
template int(16) volume = {if track_is_audio 0x0100 else 0};
const unsigned int(16) reserved = 0;
template int(32)[9] matrix=
{ 0x00010000,0,0,0,0x00010000,0,0,0,0x40000000 };
// unity matrix
unsigned int(32) width;
unsigned int(32) height;
}
*/
TkhdAtom::TkhdAtom() 
    : Atom(TKHD, 92),
      m_versionAndFlags(0x00000003),
      m_creationTime(0xbbf81e00),
      m_id(0),
      m_duration(0),
      m_volume(0x0100),
      m_width(0),
      m_height(0)
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
TkhdAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    unsigned short reserved;

    m_atomSize = atomSize;
    atomReader.readDW(m_versionAndFlags);
    atomReader.readDW(m_creationTime);
    atomReader.readDW(m_creationTime);
    atomReader.readDW(m_id);
    atomReader.seek(4, AtomReader::SEEK_FORWARD);
    atomReader.readDW(m_duration);
    atomReader.seek(12, AtomReader::SEEK_FORWARD);
    atomReader.readW(m_volume);
    atomReader.readW(reserved); // Reserved
    for (int i(0); i < 9; i++) {
	atomReader.readDW(m_matrix[i]);
    }
    atomReader.readDW(m_width);
    atomReader.readDW(m_height);
    return true;
}

bool
TkhdAtom::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize()); //4 (fixed value of 92 -set by constructor)
    atomWriter.writeDW(getName()); //8
    atomWriter.writeDW(m_versionAndFlags); //12
    atomWriter.writeDW(m_creationTime); //16
    atomWriter.writeDW(m_creationTime); //20 mod time same as creation time.
    atomWriter.writeDW(m_id); //24 
    atomWriter.seek(4, AtomWriter::SEEK_FORWARD); //reserved 4 - 28
    atomWriter.writeDW(m_duration); //32
    atomWriter.seek(12, AtomWriter::SEEK_FORWARD); //reserved 8,layer(2),alternate group(2) - 44
    atomWriter.writeW(m_volume); //46
    atomWriter.writeW(0); // Reserved 2 - 48
    for (int i(0); i < 9; i++) { //4*9=36
	atomWriter.writeDW(m_matrix[i]); //48+32 = 84
    }
    atomWriter.writeDW(m_width); //88
    atomWriter.writeDW(m_height); //92
    return true;
}

TkhdAtom&
TkhdAtom::operator=(const TkhdAtom& o) {
    if (this != &o) {
	m_versionAndFlags = o.m_versionAndFlags;
	m_creationTime = o.m_creationTime;
	m_id = o.m_id;
	m_duration = o.m_duration;
	m_volume = o.m_volume;
	for (int i = 0; i < 9; i++) { m_matrix[i] = o.m_matrix[i]; }
	m_width = o.m_width;
	m_height = o.m_height;
    }
    return *this;
}

bool TkhdAtom::operator==(TkhdAtom& leftAtom)
{
    if (m_versionAndFlags != leftAtom.m_versionAndFlags) return false;
    if (m_creationTime != leftAtom.m_creationTime) return false;
    if (m_id != leftAtom.m_id) return false;
    if (m_duration != leftAtom.m_duration) return false;
    if (m_volume != leftAtom.m_volume) return false;
    for (int i(0); i < 9; i++) {
	if (m_matrix[i] != leftAtom.m_matrix[i]) return false;
    }
    if (m_width != leftAtom.m_width) return false;
    if (m_height != leftAtom.m_height) return false;
    return true;
}

bool TkhdAtom::operator!=(TkhdAtom& leftAtom)
{
    return !(*this == leftAtom);
}

