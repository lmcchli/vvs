#ifndef _TrefAtom_h_
#define _TrefAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Track Reference Atom
     * According to the QuickTime spec: the track reference atom
     * define relations between tracks.
     */
    class TrefAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	TrefAtom();

	/**
	 * The destructor.
	 */
	~TrefAtom();

	/**
	 * Initializes the Track Reference Type Atom (sub atom).
	 */
	void initialize(unsigned trackType, unsigned trackId);

	/**
	 * Restores the contents of this atom.
	 * Only meta track is restored.
	 */
	bool restoreGuts(AtomReader& atomReader, unsigned atomSize);
	
	/**
	 * Stores this the contents of this atom.
	 * Both sample track and meta track is stored.
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
	 * This is the equality operator.
	 */
	bool operator==(TrefAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(TrefAtom& leftAtom);

    private:
	/**
	 * Sub atom size.
	 * Track Reference Type Atom.
	 */
	unsigned m_trackReferenceSize;

	/**
	 *  Sub atom name.
	 * Track Reference Type Atom.
	 */
	unsigned m_trackReferenceType;

	/**
	 * Referenced track id entry count.
	 */
	unsigned m_trackIdEntryCount;

	/**
	 * Referenced track id entries array.
	 */
	unsigned* m_trackIdEntries;
    };
};

#endif
