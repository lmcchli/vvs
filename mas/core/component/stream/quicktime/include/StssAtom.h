#ifndef _StssAtom_h_
#define _StssAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Sync Sample Atom
     * According to the QuickTime spec: The sync sample atom identifies
     * the key frames in the media.
     */
    class StssAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	StssAtom();

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
	bool operator==(StssAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(StssAtom& leftAtom);

    private:
	/**
	 * The number uf sync sample entries.
	 */
	unsigned m_syncSampleEntryCount;

	/**
	 * The sync sample entry array.
	 */
	unsigned* m_syncSampleEntries;
    };
    
};

#endif
