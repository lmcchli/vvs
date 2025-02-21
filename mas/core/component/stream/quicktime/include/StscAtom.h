#ifndef _StscAtom_h_
#define _StscAtom_h_

#include <Atom.h>

namespace quicktime {
    struct SampleToChunkEntry {
		unsigned firstChunk;
		unsigned samplesPerChunk;
		unsigned sampleDescriptionIndex;
    };
    /**
     * This class is the Track Header Atom
     * According to the QuickTime spec: the track header atom specifies
     * the characterostics of a single track within a movie.
     */
    class StscAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	StscAtom();
	~StscAtom();

	void initialize(unsigned noOfEntries);

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
	 *
	 */
	unsigned getAtomSize();

	/**
	 * Get the number of samples in the specified chunk.
	 *@param wantedChunk the chunk to look for.
	 *@return the number of samples in the specified chunk.
	 */
	unsigned getSamplesPerChunk(unsigned wantedChunk);

	/*
	 * Gets the number of chunk to sample entries
	 * in the table.
	 */
	unsigned getEntryCount();

	/**
	 *
	 */
	SampleToChunkEntry* getSampleToChunkEntries();

	bool operator==(StscAtom& leftAtom);
	bool operator!=(StscAtom& leftAtom);

	/**
	 * Find the SampleToChunkEntry this chunk is described by
	 *@return the index of the entry describing the specified chunk.
	 */
	unsigned getEntryForChunk(unsigned wantedChunk);

    private:
	unsigned m_versionAndFlags;
	SampleToChunkEntry* m_sampleToChunkEntries;
	unsigned m_entriesCount; //how many in table SampleToChunkEntry*
    };
};

#endif
