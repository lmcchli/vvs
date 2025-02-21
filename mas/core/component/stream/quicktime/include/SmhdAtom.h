#ifndef _SmhdAtom_h_
#define _SmhdAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Sound Media Information Header Atom
     * According to the QuickTime spec: the sound media information atom
     * stores the sound media's control information, such as balance.
     */
    class SmhdAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	SmhdAtom();

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
	bool operator==(SmhdAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(SmhdAtom& leftAtom);

    private:
	/**
	 * The atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * The sound balance.
	 * Default: 0.
	 */
	unsigned short m_balance;

	/**
	 * Reserved for use by Apple.
	 * Default: 0.
	 */
	unsigned short m_reserved;
    };
};

#endif
