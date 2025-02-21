#ifndef _DinfAtom_h_
#define _DinfAtom_h_

#include <DrefAtom.h>

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Data Information Atom
     * According to the QuickTime spec: the data information atom contains
     * information specifying the data handler component that provides
     * access to the media data.
     */
    class DinfAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	DinfAtom();


	/**
	 * This is the destructor.
	 */
	~DinfAtom();

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
	 * Getter for the "dref" sub atom.
	 */
	DrefAtom& getDataReferenceAtom();

	/**
	 * This is the equality operator.
	 */
	bool operator==(DinfAtom& leftAtom);

	/**
	 * This is the in-equality operator.
	 */
	bool operator!=(DinfAtom& leftAtom);

    private:
	/**
	 * This a "dref" sub atom.
	 */
	DrefAtom m_dataReferenceAtom;
    };

    inline DrefAtom& DinfAtom::getDataReferenceAtom()
    {
	return m_dataReferenceAtom;
    }
};

#endif
