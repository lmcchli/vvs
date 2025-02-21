#include "PCMSoundSampleDescription.h"

#include "AtomReader.h"
#include "AtomWriter.h"

using namespace quicktime;

/*
 * From Quicktime File Format (apple doc) 
 * It is similar in iso 14496-12 but apple quicktime is easier to read.
 * Sample description table *
An array of sample descriptions.
Sample description table
	While the exact format of the sample description varies by media type, the first four fields of every
	sample description are the same. See Chapter 4, “Media Data Atom Types,” (page 91) for details on
	various media.
Sample description size
	A 32-bit integer indicating the number of bytes in the sample description.
Data format
	A 32-bit integer indicating the format of the stored data. This depends on the media type, but
	is usually either the compression format or the media type.
Reserved 
	Six bytes that must be set to 0.
Data reference index
	A 16-bit integer that contains the index of the data reference to use to retrieve data associated
	with samples that use this sample description. Data references are stored in data reference
	atoms.



***** Sound Sample Description sssd sample entry.***** 
* 	IN OUR CASE this block is the mediaData array.
Version
	A 16-bit integer that must be set to 0 or 1 (see below).
Revision level
	A 16-bit integer that must be set to 0.
Vendor
	A 32-bit integer that must be set to 0.
Number of channels
	A 16-bit integer that indicates the number of sound channels used by the sound sample. Set
this field to 1 for monaural sounds; set it to 2 for stereo sounds.
Sample size
	A 16-bit integer that specifies the number of bits in each uncompressed sound sample. Set this
	field to 8 for 8-bit sound, and to 16 for 16-bit sound.
Compression ID
	A 16-bit integer that must be set to 0 (or, for version 1, may be -2. See below).
Packet size
	A 16-bit integer that must be set to 0.
Sample rate
	A 32-bit unsigned fixed-point number that indicates the rate at which the sound samples were
	obtained. This number should match the media’s time scale, that is, the integer portion should
	match.
** finally specific for PCM (ulaw and alaw)  **
 * 
 * ■
uLaw 2:1 and aLaw 2:1
 * 
The uLaw (mu-law) encoding scheme is used on North American and Japanese phone systems,
and is coming into use for voice data interchange, and in PBXs, voice-mail systems, and Internet
talk radio (via MIME). In uLaw encoding, 14 bits of linear sample data are reduced to 8 bits of
logarithmic data.
The aLaw encoding scheme is used in Europe and the rest of the world.
The kULawCompression and the kALawCompression formats are typically found in .au formats.

 We are only supporting version 0.
 * 
 * from iso 14496-12 basically mp4 and 3pgp:
aligned(8) abstract class SampleEntry (unsigned int(32) format)
extends Box(format){
const unsigned int(8)[6] reserved = 0;
unsigned int(16) data_reference_index;

aligned(8) class OriginalFormatBox(codingname) extends Box ('frma') {
unsigned int(32) data_format = codingname;
// format of decrypted, encoded data (in case of protection)
// or un-transformed sample entry (in case of restriction
// and complete track information)
}
  
}
 *

*/
//really mdata should be pulled out into it's component parts
//but since legacy it was like this we shall leave it for now.
PCMSoundSampleDescription::PCMSoundSampleDescription(AtomName codec) 
    : sampleDescriptionAtom(codec, 4 //atomSize
	+4 //atom name or codec aka media type.
	+6 //reserved
	+2 //index
	+20) //this is the sample rate and others - see below.
{
	m_index=1;
    m_data[0] = 0x00000000; //version 16bit (0) & Revision 16bit(0)
    m_data[1] = 0x00000000; //vendor not set.
    m_data[2] = 0x00010010; //Number of channels 16 bit (1) & sample Size(3?)
    m_data[3] = 0x00000000; //compesion id & packet size
    m_data[4] = 0x1f400000; //sample rate 32768000 or 31mbs
}

bool
PCMSoundSampleDescription::restoreGuts(AtomReader& atomReader, unsigned atomSize)
{
    m_atomSize = atomSize;
    atomReader.seek(6, AtomReader::SEEK_FORWARD); //skip reserved
    atomReader.readW(m_index); //get index
    for (int i(0); i < 5; i++) //read the inner data into the mdata array.
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

unsigned PCMSoundSampleDescription::getAtomSize() {
	
	return (4 //atomSize
	+4 //atom name or codec aka media type.
	+6 //reserved
	+2 //index
	+20); /// mdata
}

bool PCMSoundSampleDescription::operator!=(PCMSoundSampleDescription& leftAtom)
{
    return !(*this == leftAtom);
}

