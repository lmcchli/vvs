#ifndef _MdhdAtom_h_
#define _MdhdAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Media Header Atom
     * According to the QuickTime spec: the media header atom specifies
     * the characteristics of a media, including time scale and duration.
     */
    class MdhdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	MdhdAtom();

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
	 * Setter for the media atom creation time.
	 */
	void setCreationTime(unsigned creationTime);

	/**
	 * Setter for the media time scale.
	 */
	void setTimeScale(unsigned timeScale);

	unsigned getTimeScale();

	/**
	 * Setter for the media duration.
	 */
	void setDuration(unsigned duration);

	unsigned getDuration();

	/**
	 * This is the equality operator.
	 */
	bool operator==(MdhdAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(MdhdAtom& leftAtom);

    private:
	/**
	 * Atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * Atom creation time.
	 */
	unsigned m_creationTime;

	/**
	 * The media time scale.
	 */
	unsigned m_timeScale;

	/**
	 * The media duration accoording to time scale.
	 */
	unsigned m_duration;

	/**
	 * Language code is unused.
	 */
	unsigned short m_language;

	/**
	 * Specifies the media's playback quality.
	 */
	unsigned short m_quality;
    };
    
    inline void MdhdAtom::setCreationTime(unsigned creationTime)
    {
	m_creationTime = creationTime;
    }

    inline unsigned MdhdAtom::getTimeScale() {
	return m_timeScale;
    }

    inline void MdhdAtom::setTimeScale(unsigned timeScale)
    {
	m_timeScale = timeScale;
    }

    inline void MdhdAtom::setDuration(unsigned duration)
    {
	m_duration = duration;
    }

    inline unsigned MdhdAtom::getDuration()
    {
	return m_duration;
    }
};

#endif
