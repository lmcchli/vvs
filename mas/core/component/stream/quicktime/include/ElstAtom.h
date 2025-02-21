#ifndef _ElstAtom_h_
#define _ElstAtom_h_

#include <Atom.h>

namespace quicktime {
    struct EditListEntry {
	unsigned trackDuration; // In the movies time scale
	unsigned mediaTime;     // The starting time within the media
	unsigned mediaRate;     // The relative rate to play the media
    };
    /**
     * This class is the Edit List Atom
     * According to the QuickTime spec: the edit list atom map from a time
     * in a movie to a time in a media, and ultimately to media data. This
     * information is in the form of entries in an edit list.
     */
    class ElstAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	ElstAtom();

	/**
	 * This is the destructor.
	 */
	~ElstAtom();

	/**
	 * Initializes the edit list.
	 */
	void initialize(unsigned nOfEntries);

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
	 * Returns the number of edit list entries.
	 */
	unsigned getEditListEntryCount();

	/**
	 * Returns the edit list entries (array).
	 */
	EditListEntry* getEditListEntries();
	
	/**
	 * This is the equality operator.
	 */
	bool operator==(ElstAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(ElstAtom& leftAtom);

	/**
	 * Assignment operator.
	 */
	ElstAtom& operator=(const ElstAtom& o);

    private:
	/**
	 * atom version (1 byte) and flags (3 bytes).
	 */
	unsigned m_versionAndFlags;


	/**
	 * the number of edit list entries.
	 */
	unsigned m_editListEntryCount;

	/**
	 * the edit list entries (an array).
	 */
	EditListEntry* m_editListEntries;
    };

    inline unsigned ElstAtom::getEditListEntryCount()
    {
	return m_editListEntryCount;
    }

    inline EditListEntry* ElstAtom::getEditListEntries()
    {
	return m_editListEntries;
    }
};

#endif
