#ifndef _MdiaAtom_h_
#define _MdiaAtom_h_

#include <MdhdAtom.h>
#include <HdlrAtom.h>
#include <MinfAtom.h>

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Media Atom
     * According to the QuickTime spec: the media atom describe and define 
     * a track's media type and sample data.
     */
    class MdiaAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	MdiaAtom();

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
	 * Getter for the media header atom.
	 */
	MdhdAtom& getMediaHeaderAtom();

	/**
	 * Getter for the handler reference atom.
	 */
	HdlrAtom& getHandlerReferenceAtom();

	/**
	 * Getter for the media information atom.
	 */
	MinfAtom& getMediaInformationAtom();

	/**
	 * This is the equality operator.
	 */
	bool operator==(MdiaAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(MdiaAtom& leftAtom);

    private:
	/**
	 * The media header atom.
	 */ 
	MdhdAtom m_mediaHeaderAtom;
	
	/**
	 * The handler eference atom.
	 */
	HdlrAtom m_handlerReferenceAtom;

	/**
	 * The media information atom.
	 */
	MinfAtom m_mediaInformationAtom;
    };
   
    inline MdhdAtom& MdiaAtom::getMediaHeaderAtom()
    {
	return m_mediaHeaderAtom;
    }

    inline HdlrAtom& MdiaAtom::getHandlerReferenceAtom()
    {
	return m_handlerReferenceAtom;
    }

    inline MinfAtom& MdiaAtom::getMediaInformationAtom()
    {
	return m_mediaInformationAtom;
    }

};

#endif
