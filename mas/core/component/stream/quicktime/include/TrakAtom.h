#ifndef _TrakAtom_h_
#define _TrakAtom_h_

#include <TkhdAtom.h>
#include <EdtsAtom.h>
#include <MdiaAtom.h>
#include <TrefAtom.h>

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Track Atom
     * According to the QuickTime spec: the track atom define a single
     * track of a movie.
     */
    class TrakAtom : public Atom {
    public:
        /**
         * This is the default constructor.
         */
        TrakAtom();

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
         * Getter for the track header atom.
         */
        TkhdAtom& getTrackHeaderAtom();

        /**
         * Getter fpr the edit atom.
         */
        EdtsAtom& getEditAtom();

        /**
         * Getter for the media atom.
         */
        MdiaAtom& getMediaAtom();

        /**
         * Getter for the sample table atom.
         */
        StblAtom& getSampleTableAtom();

        /**
         * Returns the component sub type from the handler reference atom
         * in the media atom.
         */
        unsigned getSubType();


        /**
         * Sets the component sub type from the handler reference atom
         * in the media atom.
         */
        void setSubType(unsigned componentSubType);

        /**
         * Sets the track id in the track header.
         */
        void setTrackId(unsigned id);

        /**
         * Sets the sample time in the time-to-sample atom.
         * Input:
         *   - sampleTime, the sample time
         *   - index, the sample time index
         */
        void setSampleTime(unsigned sampleTime, unsigned index);

        /**
         * Sets the sample chunk size in the sample-to-chunk atom.
         * Input:
         *   - sampleSize, the sample size
         *   - index, the sample size index
         */
        void setSampleSize(unsigned chunkSize, unsigned index=0);

        /**
         * Sets the chunk offset in the chunk offset atom.
         * Input:
         *   - offset, the sample chunk offset
         *   - index, the sample chunk offset index
         */
        void setChunkOffset(unsigned offset, unsigned index);

        /**
         * Gets the chunk offset from the chunk offset atom.
         * Input:
         *   - index, the sample chunk offset index
         */
        unsigned getChunkOffset(unsigned index);

        /**
         * Sets the start time offset of the track.
         * The start time offset is relative to the origo of the media.
         */
        void setStartTimeOffset(unsigned offset);

        /**
         * Sets the start time offset of the track.
         * The start time offset is relative to the origo of the media.
         */
        unsigned getStartTimeOffset();

        /**
         * Returns the data format (codec)
         */
        unsigned getDataFormat();

        /**
         * Getter for the media information atom.
         */
        MinfAtom& getMediaInformationAtom();


	unsigned getFrameTime(int index);

	/**
	 *
	 */
	unsigned getChunkCount();


	/**
	 * CHUNK ITERATION - restart the internal chunk iterator so it
	 * references the first available chunk.
	 */
	void firstChunk();

	/**
	 * CHUNK ITERATION - check if the internal chunk iterator is at the
	 * end. 
	 *@return true if there are more chunks to be gotten.
	 */
	bool hasMoreChunks();

	/**
	 * CHUNK ITERATION - advance the internal chunk iterator so it
	 * references the next available chunk
	 */
	void nextChunk();

	/**
	 * CHUNK ITERATION - get the offset of the chunk referenced by the
	 * internal chunk iterator.
	 *@return the offset of the current chunk.
	 */
	unsigned getChunkOffset();

	/**
	 * CHUNK ITERATION - set the offset of the chunk referenced by the
	 * internal chunk iterator.
	 *@param o the offset.
	 */
	void setChunkOffset(unsigned o);

	/**
	 * CHUNK ITERATION - get the size of the chunk referenced by the
	 * internal chunk iterator. 
	 *@return the size of the current chunk;
	 */
	unsigned getChunkSize();


	/**
	 * SAMPLE ITERATION - restart the internal sample iterator so it
	 * references the first available sample.
	 */
	void firstSample();

	/**
	 * SAMPLE ITERATION - check if the internal sample iterator is at the
	 * end. 
	 *@return true if there are more samples to be gotten.
	 */
	bool hasMoreSamples();

	/**
	 * SAMPLE ITERATION - advance the internal sample iterator so it
	 * references the next available sample
	 */
	void nextSample();

	/**
	 * SAMPLE ITERATION - get the offset of the sample referenced by the
	 * internal sample iterator.
	 *@return the offset of the current sample.
	 */
	unsigned getSampleOffset();

	/**
	 * SAMPLE ITERATION - get the size of the sample referenced by the
	 * internal sample iterator. 
	 *@return the size of the current sample;
	 */
	unsigned getSampleSize();
	
	/**
	 * SAMPLE ITERATION - get the chunk index (starting from 0) of the
	 * current sample.
	 *@return the chunk index of the current sample;
	 */
	unsigned getChunkOfSample();

	/**
	 * SAMPLE ITERATION - get the duration of the current sample.
	 *@return the duration of the current sample;
	 */
	unsigned getSampleDuration();

	/**
	 * SAMPLE ITERATION - get the index of the current sample.
	 *@return the index of the current sample;
	 */
	unsigned getSampleIndex();

	/**
	 * Get the total size of the media in this track
	 *@return the sum of sizes of all media samples in this track.
	 */
	unsigned getMediaSize();

	/**
	 * Get the number of samples in the specified chunk.
	 *@param wantedChunk the chunk to look for.
	 *@return the number of samples in the specified chunk.
	 */
        unsigned getSamplesPerChunk(unsigned index);

	/**
	 * Get the number of chunk offset entries.
	 *@return the number of chunk offset entries in the chunk offset atom.
	 */
        int getChunkOffsetCount();
        
	/**
	 * Get the size a sample.
	 *@param sampleIx the index of the sample
	 *@return size of the specified sample.
	 */
	unsigned getSampleSize(unsigned sampleIx);

	/**
	 * Check if this track is OK.
	 *@return true if this track was prsed OK.
	 */
	bool check();

        /**
         * This is the equality operator.
         */
        bool operator==(TrakAtom& leftAtom);

        /**
         * This is the equality operator.
         */
        bool operator!=(TrakAtom& leftAtom);

    protected:
        /**
         * The track header atom.
         */
        TkhdAtom m_trackHeaderAtom;

        /**
         * The edit atom.
         */
        EdtsAtom m_editAtom;

        /**
         * The track reference atom.
         */
        TrefAtom m_trackReferenceAtom;

        /**
         * The media atom.
         */
        MdiaAtom m_mediaAtom;

        HdlrAtom& m_handlerReferenceAtom;

        /**
         * A reference to the sample description atom.
         * sub atom to the sample table which is sub sub atom to
         * the media atom.
         */
        StsdAtom& m_sampleDescriptionAtom;

        /**
         * A reference to the time-to-sample atom.
         * sub atom to the sample table which is sub sub atom to
         * the media atom.
         */
        SttsAtom& m_timeToSampleAtom;

        /**
         * A reference to the sample-to-chunk atom.
         * sub atom to the sample table which is sub sub atom to
         * the media atom.
         */
        StscAtom& m_sampleToChunkAtom;

        /**
         * A reference to the sample-to-size atom.
         * sub atom to the sample table which is sub sub atom to
         * the media atom.
         */
        StszAtom& m_sampleSizeAtom;

        /**
         * A reference to the chunk offset atom.
         * sub atom to the sample table which is sub sub atom to
         * the media atom.
         */
        StcoAtom& m_chunkOffsetAtom;

	/**
	 * CHUNK ITERATION - internal state.
	 */
	unsigned m_chunkItIndex;
	unsigned m_chunkItStscEntryIndex;
	unsigned m_chunkItSampleIndex;

	/**
	 * SAMPLE ITERATION - internal state.
	 */
	unsigned m_sampItIndex; 
	unsigned m_sampItChunkIndex;
	unsigned m_sampItStscEntryIndex;
	unsigned m_sampleInChunk;
	unsigned m_sampItOffset;

    };

    inline unsigned TrakAtom::getSubType() {
        return m_mediaAtom.getHandlerReferenceAtom().getComponentSubType();
    }

    inline void TrakAtom::setSubType(unsigned componentSubType) {
        m_mediaAtom.getHandlerReferenceAtom()
            .setComponentSubType(componentSubType);
    }

    inline TkhdAtom& TrakAtom::getTrackHeaderAtom() {
        return m_trackHeaderAtom;
    }

    inline EdtsAtom& TrakAtom::getEditAtom() {
        return m_editAtom;
    }

    inline MdiaAtom& TrakAtom::getMediaAtom() {
        return m_mediaAtom;
    }

    inline MinfAtom& TrakAtom::getMediaInformationAtom() {
        return m_mediaAtom
            .getMediaInformationAtom();
    }

    inline StblAtom& TrakAtom::getSampleTableAtom() {
        return m_mediaAtom
            .getMediaInformationAtom()
            .getSampleTableAtom();
    }
};

#endif
