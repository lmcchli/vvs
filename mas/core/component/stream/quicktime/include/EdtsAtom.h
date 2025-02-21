#ifndef _EdtsAtom_h_
#define _EdtsAtom_h_

#include <ElstAtom.h>

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Edit Atom.
     * According to the QuickTime spec: the edit atom define the portions 
     * of media that are to be used to build up a track for a movie.
     */
    class EdtsAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	EdtsAtom();


	/**
	 * This is the destructor.
	 */
	~EdtsAtom();

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
	 * Getter for the "elst" sub atom.
	 */
	ElstAtom& getEditListAtom();

	/**
	 * This is the equality operator.
	 */
	bool operator==(EdtsAtom& leftAtom);

	/**
	 * This is the in-equality operator.
	 */
	bool operator!=(EdtsAtom& leftAtom);

    private:
	/**
	 * This a "elst" sub atom.
	 */
	ElstAtom m_editListAtom;
    };

    inline ElstAtom& EdtsAtom::getEditListAtom()
    {
	return m_editListAtom;
    }
};

#endif
