#ifndef _AmrTrackAtom_h_
#define _AmrTrackAtom_h_

#include <AudioTrackAtom.h> // Inherits
#include <SampleDescription.h>
#include <AmrSampleDescription.h>

namespace quicktime {
    class AmrSampleDescription;

    /**
     * This is an Audio Track Atom.
     *
     * This class provide the functionality that is nescessary for
     * creating a QuickTime file audio track.
     */
    class AmrTrackAtom : public AudioTrackAtom {
    public:
	/**
	 * This is the default constructor for amr-nb
	 * 
     * @param sdEntry a Sample Description Entry (which will be inserted into the 
	 *        STSD atom.
     * 
	 */
	AmrTrackAtom(AmrSampleDescription* sampleDescription);
	
	/**
	 * Initializes the size of the audio "trak" (and sub atoms).
	 */
	void initialize(unsigned nOfFrames);


	/**
	 * Setter for the sample chunk size.
	 *
	 * The audio is chunked in RTP friendly chunks where all
	 * chunks have the same size. This method update the "stcs" 
	 * (Sample to Chunk Size) which is the same for all chunks.
	 */
	void setSampleToChunk(unsigned index, unsigned chunkSize);

    private:
    };
};

#endif
