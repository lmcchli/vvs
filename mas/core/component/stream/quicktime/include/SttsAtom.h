#ifndef _SttsAtom_h_
#define _SttsAtom_h_

#include <Atom.h>

namespace quicktime {
    struct TimeToSampleEntry {
	unsigned sampleCount;
	unsigned sampleDuration;
    };
    /**
     * This class is the Time to Sample Atom
     * According to the QuickTime spec: the time-to-sample atoms store
     * duration information for a media's samples, providing a mapping
     * from a time in a media to the corresponding data sample.
     */
    class SttsAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	SttsAtom();

	/**
	 * This is the destructor.
	 */
	~SttsAtom();

	/**
	 * Initializes the time to sample entry array.
	 */
	void initialize(unsigned nOfSamples);

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
	 * Getter for the number of time-to-sample entries.
	 */
	unsigned getTimeToSampleCount();

	/**
	 * Getter for the number of time-to-sample array.
	 */
	TimeToSampleEntry* getTimeToSampleEntries();

	/**
	 * This is the equality operator.
	 */
	bool operator==(SttsAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(SttsAtom& leftAtom);

    private:
	/**
	 * The atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/** 
	 * The nunber of time-to-sample entries.
	 */
	unsigned m_timeToSampleCount;

	/**
	 * The time-to-sample entries array.
	 */
	TimeToSampleEntry* m_timeToSampleEntries;
    };

    inline unsigned SttsAtom::getTimeToSampleCount()
    {
	return m_timeToSampleCount;
    }

    inline TimeToSampleEntry* SttsAtom::getTimeToSampleEntries()
    {
	return m_timeToSampleEntries;
    }
};

#endif
