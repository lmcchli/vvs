#include "VideoSampleDescription.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>
using std::cout;
using std::endl;

using namespace quicktime;

VideoSampleDescription::VideoSampleDescription() 
    : sampleDescriptionAtom(H263, 4+4+6+6+12+4+12+2+32+4),
      //m_index(1),
      m_version(2),
      m_revision(1),
      m_vendor(quicktime::APPL),
      m_temporalQuality(512),
      m_spatialQuality(1024),
      m_width(176),
      m_height(144),
      m_horizontalResolution(0x00480000),
      m_vericalResolution(0x00480000),
      m_dataSize(0),
      m_frameCount(1),
      m_colorDepth(24),
      m_colorTableId(0xffff)
{
	m_index=1;
    m_data[0] = 0x05482e32;
    m_data[1] = 0x36330000;
    m_data[2] = 0x00000000;
    m_data[3] = 0x00000000;
    m_data[4] = 0x00000000;
    m_data[5] = 0x00000000;
    m_data[6] = 0x00000000;
    m_data[7] = 0x00000000;
}

bool
VideoSampleDescription::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(6, AtomReader::SEEK_FORWARD);
    atomReader.readW(m_index);
    atomReader.readW(m_version);
    atomReader.readW(m_revision);
    atomReader.readDW(m_vendor);
    atomReader.readDW(m_temporalQuality);
    atomReader.readDW(m_spatialQuality);
    atomReader.readW(m_width);
    atomReader.readW(m_height);
    atomReader.readDW(m_horizontalResolution);
    atomReader.readDW(m_vericalResolution);
    atomReader.readDW(m_dataSize);
    atomReader.readW(m_frameCount);
    for (int i(0); i < 8; i++) {
	atomReader.readDW(m_data[i]);
    }
    atomReader.readW(m_colorDepth);    
    atomReader.readW(m_colorTableId);
    return true;
}

bool
VideoSampleDescription::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.seek(6, AtomWriter::SEEK_FORWARD);
    atomWriter.writeW(m_index);
    atomWriter.writeW(m_version);
    atomWriter.writeW(m_revision);
    atomWriter.writeDW(m_vendor);
    atomWriter.writeDW(m_temporalQuality);
    atomWriter.writeDW(m_spatialQuality);
    atomWriter.writeW(m_width);
    atomWriter.writeW(m_height);
    atomWriter.writeDW(m_horizontalResolution);
    atomWriter.writeDW(m_vericalResolution);
    atomWriter.writeDW(m_dataSize);
    atomWriter.writeW(m_frameCount);
    for (int i(0); i < 8; i++) {
	atomWriter.writeDW(m_data[i]);
    }
    atomWriter.writeW(m_colorDepth);    
    atomWriter.writeW(m_colorTableId);
    return true;
}

bool VideoSampleDescription::operator==(VideoSampleDescription& leftAtom)
{
    if (m_index != leftAtom.m_index) return false;
    if (m_version != leftAtom.m_version) return false;
    if (m_revision != leftAtom.m_revision) return false;
    if (m_vendor != leftAtom.m_vendor) return false;
    if (m_temporalQuality != leftAtom.m_temporalQuality) return false;
    if (m_spatialQuality != leftAtom.m_spatialQuality) return false;
    if (m_width != leftAtom.m_width) return false;
    if (m_height != leftAtom.m_height) return false;
    if (m_horizontalResolution != 
	leftAtom.m_horizontalResolution) return false;
    if (m_vericalResolution != leftAtom.m_vericalResolution) return false;
    if (m_dataSize != leftAtom.m_dataSize) return false;
    if (m_frameCount != leftAtom.m_frameCount) return false;
    for (int i(0); i < 8; i++) {
	if (m_data[i] != leftAtom.m_data[i]) return false;
    }
    if (m_colorDepth != leftAtom.m_colorDepth) return false;
    if (m_colorTableId != leftAtom.m_colorTableId) return false;
    return true;
}

bool VideoSampleDescription::operator!=(VideoSampleDescription& leftAtom)
{
    return !(*this == leftAtom);
}

