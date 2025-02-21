#ifndef _StblAtom_h_
#define _StblAtom_h_

#include <Atom.h>

#include <StsdAtom.h>
#include <SttsAtom.h>
#include <StssAtom.h>
#include <StscAtom.h>
#include <StszAtom.h>
#include <StcoAtom.h>


namespace quicktime {
    class StblAtom;

    /**
     * This class is the Sample Table Atom
     * According to the QuickTime spec: the sample table atom contains
     * all the time and data indexing of the media samples in a track.
     * Using tables, it is possible to locate samples in time, determine 
     * their type, and determine their size, container, and offset into 
     * that container.
     */
    class StblAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	StblAtom();

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
	 * Getter for the Sample Description Atom.
	 */
	StsdAtom& getSampleDescriptionAtom();

	/**
	 * Getter for the Time to Sample Atom.
	 */
	SttsAtom& getTimeToSampleAtom();

	/**
	 * Getter for the Sample Size Atom.
	 */
	StszAtom& getSampleSizeAtom();

	/** 
	 * Getter for the Chunk Offset Atom.
	 */
	StcoAtom& getChunkOffsetAtom();

	/**
	 * Getter for the Sample to Chunk Atom.
	 */
	StscAtom& getSampleToChunkAtom();

	/**
	 * This is the equality operator.
	 */
	bool operator==(StblAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(StblAtom& leftAtom);

    private:
	/**
	 * The sample description atom.
	 */
	StsdAtom m_sampleDescriptionAtom;

	/**
	 * The time to sample atom.
	 */
	SttsAtom m_timeToSampleAtom;

	/**
	 * The syncronize sample atom.
	 */
	StssAtom m_syncSampleAtom;

	/**
	 * The sample to chunk atom.
	 */
	StscAtom m_sampleToChunkAtom;

	/**
	 * The sample to size atom.
	 */
	StszAtom m_sampleSizeAtom;

	/**
	 * The chunk offset atom.
	 */
	StcoAtom m_chunkOffsetAtom;
    };

    inline StsdAtom& StblAtom::getSampleDescriptionAtom()
    {
	return m_sampleDescriptionAtom;
    }

    inline SttsAtom& StblAtom::getTimeToSampleAtom()
    {
	return m_timeToSampleAtom;
    }

    inline StszAtom& StblAtom::getSampleSizeAtom()
    {
	return m_sampleSizeAtom;
    }

    inline StscAtom& StblAtom::getSampleToChunkAtom()
    {
	return m_sampleToChunkAtom;
    }

    inline StcoAtom& StblAtom::getChunkOffsetAtom()
    {
	return m_chunkOffsetAtom;
    }
};

#endif
