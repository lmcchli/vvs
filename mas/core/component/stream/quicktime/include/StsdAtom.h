#ifndef _StsdAtom_h_
#define _StsdAtom_h_

#include <Atom.h>
#include <AmrSampleDescription.h>

namespace quicktime {
    /**
     * This class is the Sample Description Atom (STSD)
	 *  contained in STBL
     * According to the QuickTime spec: the sample description atom stores
     * ISO SPEC:
	 * information that allows you to decode samples in the media.
	 * and initialization information needed for that coding.	
     **/
    class StsdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	StsdAtom();

	virtual ~StsdAtom();

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
	 * Returns the size of this atom.
	 *
	 * The returned size represent the size, on media, which is 
	 * occupied by this atom.
	 */
	unsigned getAtomSize();

	/**
	 * Setter for the one and only Sample DescripTion entry.
	 * This will be deleted by the StsdAtom destructor, so it must be dynamically
	 * allocated.
	 * NOTE:we currently only support one entry.
	 */
	void setSampleDescriptionEntry(sampleDescriptionAtom* sampleDescriptionEntry);
	/**
	 * Getter for the data format of the contained sample
	 * entry, 0 if not set.
	 */
	unsigned getDataFormat();
	
	inline sampleDescriptionAtom * getSampleDescription() {
		return m_sampleDescriptionEntry;
	}

	/**
	 * This is the equality operator.
	 */
	bool operator==(StsdAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(StsdAtom& leftAtom);

    private:
	/**
	 * The sample description entry/sub-atom (array)
	 */
	sampleDescriptionAtom* m_sampleDescriptionEntry;

	/**
	 * The atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * The  number of sample description entries.
	 * Should always be (0..1).
	 */
	unsigned m_entryCount;

    };
};

#endif
