#ifndef _HintTrackAtom_h_
#define _HintTrackAtom_h_

#include <TrakAtom.h> // Inherits

#include <HintSampleDescription.h>

namespace quicktime {
    /**
     * This is a hint track atom.
     *
     * This class provide the functionality that is nescessary for
     * creating a QuickTime file hint track.
     */
    class HintTrackAtom : public TrakAtom {
    public:
	/**
	 * This is the default constructor.
	 */
	HintTrackAtom();

	/**
	 * Initializes the size of the hint "trak" (and sub atoms).
	 */
	void initialize(unsigned nOfSamples);

    private:
	/**
	 * Hint sample description.
	 * Describes RTP ...
	 */
	HintSampleDescription m_rtpAtom;
    };
};

#endif
