#ifndef _AudioTrackAtom_h_
#define _AudioTrackAtom_h_

#include <TrakAtom.h> // Inherits

#include <SampleDescription.h>

namespace quicktime {
    /**
     * This is an Audio Track Atom.
     *
     * This class provide the functionality that is nescessary for
     * creating a QuickTime file audio track.
     */
    class AudioTrackAtom : public TrakAtom {
    public:
	/**
	 * This is the default constructor.
	 * 
     * @param sdEntry a Sample Description Entry (which will be inserted into the 
	 *        STSD atom.
     * 
	 */
	AudioTrackAtom(sampleDescriptionAtom* sdEntry = (sampleDescriptionAtom*)0);

	/**
	 * Initializes the size of the audio "trak" (and sub atoms).
	 */
	void initialize(unsigned nOfSamples);

	/**
	 * Setter for the sample size (entire audio data).
     * 
     * This method updates the "stsz" with sample size and count. Please
     * note that this implies that no sample size list is used and that
     * the sample size is assumed to be homogeneous (same for all samples).
     * By defalt uLaw/PCMU is only one sample devided over multiple 
     * chunks.
	 */
	void setSampleSize(unsigned sampleSize);
	void setSampleSize(unsigned sampleSize, unsigned sampleCount);
	
	/**
	 * Setter for the sample time.
	 *
	 * The "stts" (Time to Sample atom) is updated with the
	 * sample time (in samples, entire amount of samples) which
	 * is the same as the sample size.
	 */
	void setSampleTime(unsigned sampleTime);
	void setSampleTime(unsigned sampleTime, unsigned sampleCount);

	/**
	 * Setter for the sample chunk size.
	 *
	 * The audio is chunked in RTP friendly chunks where all
	 * chunks have the same size. This method update the "stcs" 
	 * (Sample to Chunk Size) which is the same for all chunks.
	 */
	void setSampleToChunk(unsigned chunkSize);

    private:
    };
};

#endif
