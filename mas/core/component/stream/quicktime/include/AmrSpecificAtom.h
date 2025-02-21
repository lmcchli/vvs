#ifndef _AmrSpecificAtom_h_
#define _AmrSpecificAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the specific information atom DAMR of the SAMR or SAWB
	 * AMRSampleEntry. See 6.7 of 3gpp 26 244.
	 * It contains information about how to decode the amr or amr-wb
	 * data contained in the mdat atom.
     */
    class AmrSpecificAtom : public Atom {
		
    public:
	/**Encoded bit rate kb/s. In the current application it is a known
	   constant, but could also be determined from the AMR data based on
	   FT field.*/
	static const unsigned bitRateNB = 4750;
	static const unsigned bitRateWB = 6660;

	/**Frame type bit pattern for the bit rate above: FT = 0 (See 3GPP TS
	   26.101) gives a one bit in position 0.*/
	static const unsigned short modeSetNB = 0x81FF; //all possible modes.
	static const unsigned short modeSetWB = 0x83FF; //all possible modes.

	/**Size of AMR NB frame in bytes. Since an AMR frame is 20 ms in time, this
	   value is fully determined by the bit rate. The last byte may have
	   unused bits. 1 + 4750 * 0.02 / 8 rounded up to nearest byte (header
	   byte, bit rate, frame time, bits to bytes).*/
	static const unsigned frameSizeNB = 13;
		/**Size of AMR WB frame in bytes. Since an AMR frame is 20 ms in time, this
	   value is fully determined by the bit rate. The last byte may have
	   unused bits. 1 + 6660 * 0.02 / 8 rounded up to nearest byte (header
	   byte, bit rate, frame time, bits to bytes).*/
	static const unsigned frameSizeWB = 17;

	/**Number of AMR frames in each 3gp sound sample. Valid values are
	   1-15. 1 is chosen for no particular reason.*/
	static const unsigned framesPerSample = 1;

	/** Duration of an AMR or AMR-WB fram in milliseconds. */
	static const unsigned frameMilliSec = 20;

	/**Decoder version. See 3GPP TS 126 244.*/
	static const unsigned char decoderVersion = 1;

	/**Mode change period. See 3GPP TS 126 244.*/
	static const unsigned char modeChangePeriod = 0;

	/**Codec vendor. See 3GPP TS 126 244.*/
	static const unsigned vendor = 'e'<<24 | 'r'<<16 | 'i'<<8 | 'c';

	/**
	 * This is the default constructor.
	 */
	AmrSpecificAtom();
	
	//if set to true will inizialize as default wideband, false as NB.
	AmrSpecificAtom(bool wideBand);

	inline void setFramesPerSample(unsigned char s) { m_framesPerSample = s; }
	inline void setModeChangePeriod(unsigned char m) { m_modeChangePeriod = m; }
    inline void setModeSet(unsigned short modeSet) { m_modeSet=modeSet; }
	
	inline unsigned getVendor() { return m_vendor; }
	inline unsigned char getDecoderVersion() { return m_decoderVersion;}
	inline unsigned short getModeSet() { return m_modeSet; }
	inline unsigned char getModeChangePeriod() {return m_modeChangePeriod;}
	inline unsigned char getFramesPerSample() {return m_framesPerSample;}
	
	
	/*These two functions set the default mode for the DAMR for NB or WB 
	 * By default when constructed it is set to NB
	 * restoreGuts will pull it from the file.
	 */
	inline void setDefaultmodeNB() {m_modeSet=modeSetNB;};
	inline void setDefaultmodeWB() {m_modeSet=modeSetWB;};
	

	/**
	 * Restores the contents of this atom.
	 * Only meta data is restored.
	 */
	bool restoreGuts(AtomReader& atomReader, unsigned atomSize);
	
	/**
	 * Stores this the contents of this atom.
	 * Both sample data and meta data is stored.
	 */ 
	bool saveGuts(AtomWriter& atomWriter);
	
	/**
	 * Calculates and returns the total size of the atom.
	 * The calcuation is based upon the size of this atom
	 * and all sub-atoms.
	 */
	unsigned getAtomSize();

	/**
	 * This is the equality operator.
	 */
	bool operator==(AmrSpecificAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(AmrSpecificAtom& leftAtom);

    private:

/* From 6.7	AMRSpecificBox field for AMRSampleEntry box 3gpp 26244
	 struct AMRDecSpecStruc{
	Unsigned int (32)	vendor
	Unsigned int (8)	decoder_version
	Unsigned int (16)	mode_set
	Unsigned int (8)	mode_change_period
	Unsigned int (8)	frames_per_sample
} */

	unsigned m_vendor;
	unsigned char m_decoderVersion;
    unsigned short m_modeSet;
	unsigned char m_modeChangePeriod;
    unsigned char m_framesPerSample;

    };
};

#endif
