#ifndef _StcoAtom_h_
#define _StcoAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Chunk Offset Atom
     * According to the QuickTime spec: the chunk offset atom identify
     * the location of each chunk of data in the media's data stream.
     */
    class StcoAtom : public Atom {
    public:
	/**
	 * The default constructor.
	 */
	StcoAtom();

	/**
	 * The destructor.
	 */
	~StcoAtom();

	/**
	 * Initializes the chunk offset array.
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
	 * Getter for the chunk offset entry cout.
	 */
	unsigned getChunkOffsetCount();

	/**
	 * Getter for the chunk offset entries array.
	 */
	unsigned* getChunkOffsetEntries();

	/**
	 * A getter for the data reference array.
	 */
	bool operator==(StcoAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(StcoAtom& leftAtom);

    private:
	/**
	 * The atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * The number of chunk offset entries.
	 */
	unsigned m_chunkOffsetCount;

	/**
	 * The chunk offset entries array.
	 */
	unsigned* m_chunkOffsetEntries;
    };
    
};

inline unsigned 
quicktime::StcoAtom::getChunkOffsetCount()
{
    return m_chunkOffsetCount;
}

inline unsigned* 
quicktime::StcoAtom::getChunkOffsetEntries()
{
    return m_chunkOffsetEntries;
}
#endif
