#ifndef _DrefAtom_h_
#define _DrefAtom_h_

#include <Atom.h>

namespace quicktime {
    struct DataReferenceEntry {
	unsigned refSize; 
	unsigned refType;
	unsigned versionAndFlags;
    };
    /**
     * This class is the Data Reference Atom
     * According to the QuickTime spec: data reference atoms contain
     * tabular data that instructs the data handler component how to 
     * access the media's data.
     */
    class DrefAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	DrefAtom();

	/**
	 * The destructor.
	 */
	~DrefAtom();


	/**
	 * Initializes the size of the atom.
	 */
	void initialize(unsigned nOfReferences);

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
	 * Getter for the data reference entry count.
	 */
	unsigned getDataReferenceEntryCount();

	/**
	 * A getter for the data reference array.
	 */
	DataReferenceEntry* getDataReferenceEntries();

	/**
	 * This is the equality operator.
	 */
	bool operator==(DrefAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(DrefAtom& leftAtom);

    private:
	/**
	 * version (1 byte) and flags (3 bytes).
	 */
	unsigned m_versionAndFlags;

	/**
	 * the number of data reference entries.
	 */
	unsigned m_dataReferenceEntryCount;

	/**
	 * the data reference entries (an array).
	 */
	DataReferenceEntry* m_dataReferenceEntries;
    };

    inline unsigned DrefAtom::getDataReferenceEntryCount()
    {
	return m_dataReferenceEntryCount;
    }

    inline DataReferenceEntry* DrefAtom::getDataReferenceEntries()
    {
	return m_dataReferenceEntries;
    }
};

#endif
