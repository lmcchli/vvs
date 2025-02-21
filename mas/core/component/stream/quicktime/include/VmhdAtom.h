#ifndef _VmhdAtom_h_
#define _VmhdAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Video Media Information Atom
     * According to the QuickTime spec: video media information header
     * atoms define specific color and graphics mode information.
     */
    class VmhdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	VmhdAtom();

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
	bool operator==(VmhdAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(VmhdAtom& leftAtom);

    private:
	/**
	 * The atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * The transfer mode (information for QuickDraw).
	 */
	unsigned short m_graphicsMode;

	/**
	 * Specifiy red color for the transfer mode operation above.
	 */
	unsigned short m_redOpColor;

	/**
	 * Specifiy green color for the transfer mode operation above.
	 */
	unsigned short m_greenOpColor;

	/**
	 * Specifiy blue color for the transfer mode operation above.
	 */
	unsigned short m_blueOpColor;
    };
};

#endif
