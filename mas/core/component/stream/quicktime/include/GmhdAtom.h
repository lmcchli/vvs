#ifndef _GmhdAtom_h_
#define _GmhdAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Base Media Information Header Atom
     * According to the QuickTime spec: the base media information header 
     * indicates that this media information atom pertains to a base media.
     * This class also contain the Base Media Info Atom.
     * According to the QuickTime spec: the base media info atom defines
     * the media's control information, including graphics mode and
     * balance information.
     */
    class GmhdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	GmhdAtom();

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
	 * This is the equality operator.
	 */
	bool operator==(GmhdAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(GmhdAtom& leftAtom);

    private:
	/**
	 * Specifies the size of the base media info atom
	 */
	unsigned m_gminSize;

	/**
	 * "gmin" the atom name
	 */
	unsigned m_gminName;

	/**
	 * Gmin atom version and flags.
	 */
	unsigned m_versionAndFlags;
	
	/**
	 * Specifies the transfer mode.
	 */
	unsigned short m_graphicsMode;

	/**
	 * Red color operation mode.
	 */
	unsigned short m_redOpColor;

	/**
	 * Green color operation mode.
	 */
	unsigned short m_greenOpColor;

	/**
	 * Blue color operation mode.
	 */
	unsigned short m_blueOpColor;

	/**
	 * Specifies the sound balance of this media.
	 */
	unsigned short m_balance;

	/**
	 * Reserved for use by Apple.
	 */
	unsigned short m_reserved;
    };
};

#endif
