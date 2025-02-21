#ifndef _StszAtom_h_
#define _StszAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Sample Size Atom
     * According to the QuickTime spec: the sample size atom contains the 
     * sample count and a table giving the size of each sample. This allows
     * the media data itself to be unframed. The total number of samples
     * in the media is always indicated in the sample count. If the default
     * size is indicated, the no table follows.
     */
    class StszAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	StszAtom();

	/**
	 * The destructor.
	 */
	~StszAtom();

	/**
	 * Initializes the sample size entry array.
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
	 * Getter for the number of sample size entries.
	 */
	unsigned getSampleSizeCount();
	
	/**
	 * Setter for the sample size count.
	 */
	void setSampleSizeCount(unsigned count);

	/**
	 * Setter for the sample size.
	 */
	void setSampleSize(unsigned sampleSize);

	/**
	 * Gets the size of a sample.
	 *@return the size of the specified sample.
	 */
	unsigned getSampleSize(unsigned index);

	/**
	 * Returns the value of the sample size field, which is either 0 or the
	 * size of all samples.
	 */
	unsigned getSampleSize();

	/**
	 * Getter for the sample size entries array.
	 */
	unsigned* getSampleSizeEntries();
	
	/**
	 * Gets the total size of all samples.
	 *@return the size in bytes of all samples.
	 */
	unsigned getTotalSize();

	void setTotalSize(unsigned tot);
	/**
	 * This is the equality operator.
	 */
	bool operator==(StszAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(StszAtom& leftAtom);

    private:
	/**
	 * Atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * The sample size.
	 * If 0 the sizes are different and stored in the table.
	 * If > 0 all the samples has this size.
	 */
	unsigned m_sizeOfSample;

	/**
	 * The sum of sizes of all samples.
	 */
	unsigned m_totalSize;

	/**
	 * The number of sample size entries.
	 */
	unsigned m_sampleSizeCount;

	/**
	 * The sample size entries array.
	 */
	unsigned* m_sampleSizeEntries;
    };
    
    inline unsigned StszAtom::getSampleSizeCount()
    {
	return m_sampleSizeCount;
    }

    inline unsigned* StszAtom::getSampleSizeEntries()
    {
	return m_sampleSizeEntries;
    }

    inline unsigned StszAtom::getSampleSize()
    {
	return m_sizeOfSample;
    }

    inline unsigned StszAtom::getTotalSize()
    {
	return m_totalSize;
    }

    inline void StszAtom::setTotalSize(unsigned tot)
    {
	m_totalSize = tot;
    }
};

#endif
