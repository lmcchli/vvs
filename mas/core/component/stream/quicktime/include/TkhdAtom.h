#ifndef _TkhdAtom_h_
#define _TkhdAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Track Header Atom
     * According to the QuickTime spec: the track header atom specifies
     * the characteristics of a single track within a movie.
     */
    class TkhdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	TkhdAtom();

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
	 * Setter for the version and flags.
	 */
	void setFlags(unsigned versionAndFlags);

	/**
	 * Setter for the track id.
	 */
	void setId(unsigned id);

	/**
	 * Setter for the track duration (movie time scale).
	 */
	void setDuration(unsigned duration);

	unsigned getDuration();

	/**
	 * Setter for the pixel width.
	 */
	void setWidth(unsigned width);

	/**
	 * Stter for the pixel height.
	 */
	void setHeight(unsigned height);

	/**
	 * Assignment operator.
	 */
	TkhdAtom& operator=(const TkhdAtom& o);

	/**
	 * This is the equality operator.
	 */
	bool operator==(TkhdAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(TkhdAtom& leftAtom);

    private:
	/**
	 * Atom version and flags.
	 * Default: 0x000f.
	 */
	unsigned m_versionAndFlags;

	/**
	 * Track header creation time.
	 */
	unsigned m_creationTime;

	/**
	 * Track id (>0).
	 */
	unsigned m_id;
	
	/**
	 * Track duration in movie's time scale.
	 */
	unsigned m_duration;

	/**
	 * Preffered sound volume.
	 * Default: (fiexed point) 1.0.
	 */
	unsigned short m_volume;

	/**
	 * Matrix structure.
	 * Default: unit matrix.
	 */
	unsigned m_matrix[9];

	/**
	 * Fixed point number specifying the track's pixel width.
	 */
	unsigned m_width;

	/**
	 * Fixed point number specifying the track's pixel height.
	 */
	unsigned m_height;
    };

    inline void TkhdAtom::setId(unsigned id)
    {
	m_id = id;
    }
    
    inline void TkhdAtom::setFlags(unsigned versionAndFlags)
    {
	m_versionAndFlags = versionAndFlags;
    }

    inline void TkhdAtom::setDuration(unsigned duration)
    {
	m_duration = duration;
    }

    inline unsigned TkhdAtom::getDuration()
    {
	return m_duration;
    }

    inline void TkhdAtom::setWidth(unsigned width)
    {
	m_width = width;
    }

    inline void TkhdAtom::setHeight(unsigned height)
    {
	m_height = height;
    }

};

#endif
