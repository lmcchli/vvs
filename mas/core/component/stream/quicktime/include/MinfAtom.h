#ifndef _MinfAtom_h_
#define _MinfAtom_h_

#include <HdlrAtom.h>
#include <DinfAtom.h>
#include <StblAtom.h>

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Media Information Atom
     * According to the QuickTime spec: the media information atom
     * store handler-specific information for a track's media data.
     */
    class MinfAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	MinfAtom();

	/**
	 * This is a destructor.
	 */
	~MinfAtom();

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
	 * Setter for the Media Information Header Atom.
	 * Input: atom the header ('vmhd', 'smhd' or 'gmhd').
	 */
	void setMediaInformationHeaderAtom(Atom* atom);

	/**
	 * Getter for the handler reference atom.
	 */
	HdlrAtom& getHandlerReferenceAtom();

	/**
	 * Getter for the data information atom.
	 */
	DinfAtom& getDataInformationAtom();

	/**
	 * Getter for the sample table atom.
	 */
	StblAtom& getSampleTableAtom();

	/**
	 * This is the equality operator.
	 */
	bool operator==(MinfAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(MinfAtom& leftAtom);

    private:
	/**
	 * The media information atom: ('vmhd', 'smhd' or 'gmhd').
	 */
	Atom* m_mediaInformationHeaderAtom;

	/**
	 * The handler refernece atom.
	 */
	HdlrAtom m_handlerReferenceAtom;

	/**
	 * The data information atom.
	 */
	DinfAtom m_dataInformationAtom;

	/**
	 * The sample table atom.
	 */
	StblAtom m_sampleTableAtom;
    };

    inline void MinfAtom::setMediaInformationHeaderAtom(Atom* atom)
    {
		MOV_DEBUG("MinfAtom::setMediaInformationHeaderAtom" << atom);
		if ( m_mediaInformationHeaderAtom != 0 ) {
			MOV_DEBUG("MinfAtom::setMediaInformationHeaderAtom - deleting prevously set value." << atom);
			delete m_mediaInformationHeaderAtom;
		}
		m_mediaInformationHeaderAtom = atom;
			
    }

    inline HdlrAtom& MinfAtom::getHandlerReferenceAtom()
    {
	return m_handlerReferenceAtom;
    }

    inline DinfAtom& MinfAtom::getDataInformationAtom()
    {
	return m_dataInformationAtom;
    }

    inline StblAtom& MinfAtom::getSampleTableAtom()
    {
	return m_sampleTableAtom;
    }

};

#endif
