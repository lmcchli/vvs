#ifndef _PCMSoundSampleDescription_h_
#define _PCMSoundSampleDescription_h_

#include <Atom.h>
#include <SampleDescription.h>

namespace quicktime {
    /**
     * This class is the Sound Sample Description ('ulaw')
     * According to the QuickTime spec: the sound sample description
     * contains information that defines how to interpret the sound data.
     */
    class PCMSoundSampleDescription : public sampleDescriptionAtom {
    public:
	/**
	 * This is the default constructor.
	 */
	PCMSoundSampleDescription(AtomName codec);

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
	 * This is the equality operator.
	 */
	bool operator==(PCMSoundSampleDescription& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(PCMSoundSampleDescription& leftAtom);
	
	/**
	 * Calculates and returns the total size of the atom.
	 * The calcuation is based upon the size of this atom
	 * and all sub-atoms.
	 */
	unsigned getAtomSize();
	
	//*get the innerdata
	inline unsigned * getmData() {return m_data;}

    private:

	/**
	 * The sound sample description ...
	 */
	unsigned m_data[5];
    };
};

#endif
