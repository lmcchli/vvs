#ifndef _VideoTrackAtom_h_
#define _VideoTrackAtom_h_

#include <TrakAtom.h> // Inherits

#include <VideoSampleDescription.h>
#include <SampleDescription.h>

namespace quicktime {
    /**
     * This is a video track atom.
     *
     * This class provide the functionality that is nescessary for
     * creating a QuickTime file video track.
     */
    class VideoTrackAtom : public TrakAtom {
    public:
	/**
	 * This is the default constructor.
	 */
	VideoTrackAtom(sampleDescriptionAtom* videoSampleDescription = (sampleDescriptionAtom*)0);
	
	/**
	 * Initializes the size of the video "trak" (and sub atoms).
	 */
	void initialize(unsigned nOfSamples);

    private:
	/**
	 * Video sample description.
	 * Describes H.263 ...
	 */
//	VideoSampleDescription m_h263Atom;
    };
};

#endif
