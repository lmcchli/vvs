#ifndef _MoovAtom_h_
#define _MoovAtom_h_

#include <MvhdAtom.h>
#include <Atom.h>
#include <TrakAtom.h>

namespace quicktime {

    /**
     * This class is the Movie Atom
     * According to the QuickTime spec: the Movie Atom contains meta data 
     * about the movie (number and type of tracks, location of sample data, 
     * and such).
     */
    class MoovAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	MoovAtom();

	/**
	 * This is the destructor.
	 */
	~MoovAtom();

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
	 * Getter for the movie header atom.
	 */
	MvhdAtom& getMovieHeaderAtom();

	/**
	 * Getter for the audio track atom.
	 */
	TrakAtom* getAudioTrackAtom();

	/**
	 * Setter for the audio track atom.
	 */
	void setAudioTrackAtom(TrakAtom* trackAtom);

	/**
	 * Getter for the video track atom.
	 */
	TrakAtom* getVideoTrackAtom();

	/**
	 * Setter for the video track atom.
	 */
	void setVideoTrackAtom(TrakAtom* trackAtom);

	/**
	 * Getter for the hint track atom.
     * getVideoHint is true for video hint track
     * and false for audio hint track
	 */
	TrakAtom* getHintTrackAtom();
	TrakAtom* getAudioHintTrackAtom();

	/**
	 * Setter for the hint track atom.
	 */
	void setHintTrackAtom(TrakAtom* trackAtom);
	void setAudioHintTrackAtom(TrakAtom* trackAtom);
	
	/**
	 * This is the equality operator.
	 */
	bool operator==(MoovAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(MoovAtom& leftAtom);

    private:
	/**
	 * The movie header atom.
	 */
	MvhdAtom m_movieHeaderAtom;

	/**
	 * The audio track atom.
	 */
	TrakAtom* m_audioTrackAtom;

	/**
	 * The video track atom.
	 */
	TrakAtom* m_videoTrackAtom;

	/**
	 * The hint track atom.
	 */
	TrakAtom* m_audioHintTrackAtom;
	TrakAtom* m_videoHintTrackAtom;
    };

    inline MvhdAtom& MoovAtom::getMovieHeaderAtom()
    {
	return m_movieHeaderAtom;
    }

    inline TrakAtom* MoovAtom::getAudioTrackAtom()
    {
	return m_audioTrackAtom;
    }

    inline void MoovAtom::setAudioTrackAtom(TrakAtom* trackAtom)
    {
		if(m_audioTrackAtom != NULL ) {
			delete m_audioTrackAtom;
		}
		m_audioTrackAtom = trackAtom;
    }

    inline TrakAtom* MoovAtom::getVideoTrackAtom()
    {
	return m_videoTrackAtom;
    }

    inline void MoovAtom::setVideoTrackAtom(TrakAtom* trackAtom)
    {
		if(m_videoTrackAtom != 0)
			delete m_videoTrackAtom;
		m_videoTrackAtom = trackAtom;
    }

    inline TrakAtom* MoovAtom::getHintTrackAtom()
    {
	return m_videoHintTrackAtom;
    }
 
    inline TrakAtom* MoovAtom::getAudioHintTrackAtom()
    {
	return m_audioHintTrackAtom;
    }

    inline void MoovAtom::setHintTrackAtom(TrakAtom* trackAtom)
    {
      if (m_videoHintTrackAtom != 0)
			delete m_videoHintTrackAtom;
      m_videoHintTrackAtom = trackAtom;
    }

    inline void MoovAtom::setAudioHintTrackAtom(TrakAtom* trackAtom)
    {
      if (m_audioHintTrackAtom != 0)
			delete m_audioHintTrackAtom;
      m_audioHintTrackAtom = trackAtom;
    }
};

#endif
