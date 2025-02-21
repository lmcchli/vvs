#include "H263SampleDescription.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

H263SampleDescription::H263SampleDescription() :
    sampleDescriptionAtom(S263, 4 //size
	 + 4 //format=s263
	 + 6 //reserved
	 + 2 //data reference index
	 + 16 //reserved
	 + 2 //width
	 + 2 //height
	 + 4 //reserved
	 + 4 //reserved
	 + 4 //reserved
	 + 2 //reserved
	 + 32 //reserved
	 + 2 //reserved
	 + 2 //reserved
	 + 4 //size
	 + 4 //type=d263
	 + 4 //vendor
	 + 1 //decoder version
	 + 1 //H263 level
	 + 1), //H263 profile
    m_vendor(0x6170706c),
    //m_index(1),
    m_width(176),
    m_height(144) {
	m_index=1;
}

void
H263SampleDescription::setWidth(unsigned char v) {
    m_width = v;
}

void
H263SampleDescription::setHeight(unsigned char v) {
    m_height = v;
}

void
H263SampleDescription::setVendor(unsigned v) {
    m_vendor = v;
}

bool
H263SampleDescription::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(6, AtomReader::SEEK_FORWARD);
    atomReader.readW(m_index);
    atomReader.seek(4 * 4, AtomReader::SEEK_FORWARD);
    atomReader.readW(m_width);
    atomReader.readW(m_height);
    atomReader.seek(4 + 4 + 4 + 2 + 32 + 2 + 2, AtomReader::SEEK_FORWARD);
    atomReader.seek(4 + 4, AtomReader::SEEK_FORWARD);
    atomReader.readDW(m_vendor);
    atomReader.seek(1 + 1 + 1, AtomReader::SEEK_FORWARD);
    return true;
}

bool
H263SampleDescription::saveGuts(AtomWriter& atomWriter)
{
    atomWriter.writeDW(getAtomSize());
    atomWriter.writeDW(getName());
    atomWriter.seek(6, AtomWriter::SEEK_FORWARD);
    atomWriter.writeW(m_index);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeW(m_width);
    atomWriter.writeW(m_height);
    atomWriter.writeDW(0x00480000);
    atomWriter.writeDW(0x00480000);
    atomWriter.writeDW(0);
    atomWriter.writeW(1);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeDW(0);
    atomWriter.writeW(24);
    atomWriter.writeW(0xffff);
    atomWriter.writeDW(4 + 4 + 4 + 1 + 1 + 1);
    atomWriter.writeDW(D263);
    atomWriter.writeDW(m_vendor);
    atomWriter.writeByte(0);
    atomWriter.writeByte(10);
    atomWriter.writeByte(0);
    return true;
}

unsigned H263SampleDescription::getAtomSize() {
	unsigned size(4 //size
	 + 4 //format=s263
	 + 6 //reserved
	 + 2 //data reference index
	 + 16 //reserved
	 + 2 //width
	 + 2 //height
	 + 4 //reserved
	 + 4 //reserved
	 + 4 //reserved
	 + 2 //reserved
	 + 32 //reserved
	 + 2 //reserved
	 + 2 //reserved
	 + 4 //size
	 + 4 //type=d263
	 + 4 //vendor
	 + 1 //decoder version
	 + 1 //H263 level
	 + 1); //H263 profile
    return size;
}

bool H263SampleDescription::operator==(H263SampleDescription& leftAtom)
{
    return m_index == leftAtom.m_index
	&& m_vendor == leftAtom.m_vendor
	&& m_width == leftAtom.m_width
	&& m_height == leftAtom.m_height;
}

bool H263SampleDescription::operator!=(H263SampleDescription& leftAtom)
{
    return !(*this == leftAtom);
}

