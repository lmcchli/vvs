#ifndef _MvhdAtom_h_
#define _MvhdAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Movie Header Atom
     * According to the QuickTime spec: the data contained in this atom
     * defines chracteristics of the entire QuickTime movie, such as time
     * scale and duration.
     */
    class MvhdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	MvhdAtom();

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
	 * This is a setter for the Movie Duration.
	 * The movie duration is defined by the duration of the
	 * track witch have the largest duration.
	 * 
	 * @param duration the movie duration in milli seconds.
	 */
	void setDuration(unsigned duration);
	
	/**
	 * This is a setter for the Movie Duration.
	 * The movie duration is defined by the duration of the
	 * track witch have the largest duration.
	 *
	 * @return the movie duraion in milli seconds.
	 */
	unsigned getDuration();

	unsigned getTimeScale();

	/**
	 * This is the equality operator.
	 */
	bool operator==(MvhdAtom& leftAtom);

	/**
	 * This is the in-equality operator.
	 */
	bool operator!=(MvhdAtom& leftAtom);

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
	 * The movie time scale.
	 */
	unsigned m_movieTimeScale;

	/**
	 * The duration of the movie.
	 */
	unsigned m_movieDuration;

	/**
	 * Fixed point number specifying at which rate to play this movie.
	 * Default: 1.0
	 */
	unsigned m_preferredRate;

	/**
	 * Fixed point number specifying how loud to play the movie sound.
	 * Default: 1.0
	 */
	unsigned short m_preferredVolume;

	/**
	 * The display matrix. Should be the unit matrix as default.
	 */
	unsigned m_matrix[9];

	/**
	 * The id to use for the track added next.
	 */
	unsigned m_nextTrackId;
    };

    inline void MvhdAtom::setDuration(unsigned duration)
    {
	m_movieDuration = duration;
    }
    
    inline unsigned MvhdAtom::getDuration()
    {
	return m_movieDuration;
    }

    inline unsigned MvhdAtom::getTimeScale()
    {
	return m_movieTimeScale;
    }
};

#endif
