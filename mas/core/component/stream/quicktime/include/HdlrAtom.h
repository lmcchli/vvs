#ifndef _HdlrAtom_h_
#define _HdlrAtom_h_

#include <Atom.h>

namespace quicktime {
    /**
     * This class is the Handler Reference Atom
     * According to the QuickTime spec: the handler reference spacifies the
     * the media handler component that is to be used to interpret the
     * media's data.
     */
    class HdlrAtom : public Atom {
    public:
	/**
	 * This is the default constructor.
	 */
	HdlrAtom();

	virtual ~HdlrAtom();

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
	 * Getter for the type of the handler
	 */
	unsigned getComponentType();

	/**
	 * Setter for the type of the handler
	 */
	void setComponentType(unsigned componentType);

	/**
	 * Getter for the type of the media handler or data handler.
	 */
	unsigned getComponentSubType();

	/**
	 * Setter for the type of the media handler or data handler.
	 */
	void setComponentSubType(unsigned componentSubType);

	/**
	 * This is the equality operator.
	 */
	bool operator==(HdlrAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(HdlrAtom& leftAtom);

    private:
	/**
	 * Atom version and flags.
	 */
	unsigned m_versionAndFlags;

	/**
	 * The type of the handler: 'mhlr' or 'dhlr'.
	 */
	unsigned m_componentType;

	/**
	 * The type of the media handler or data handler.
	 * For media handler (e.g.): 'vide' or 'soun'
	 * For data handler (e.g.): 'alis'
	 */
	unsigned m_componentSubType;

	/**
	 * Reserved: 0.
	 */
	unsigned m_componentManufacturer;

	/**
	 * Reserved: 0.
	 */
	unsigned m_componentFlags;

	/**
	 * Reserved: 0.
	 */
	unsigned m_componentFlagsMask;

	/**
	 * This is a c string. Depending on the quicktime variant, it will be
	 * read and written as a Mac pascal-style counted string (length byte +
	 * characters), or as a null-terminated c string.
	 */
        char* m_componentName;
    };
    
    inline unsigned HdlrAtom::getComponentType()
    {
	return m_componentType;
    }

    inline void HdlrAtom::setComponentType(unsigned componentType)
    {
	m_componentType = componentType;
    }

    inline unsigned HdlrAtom::getComponentSubType()
    {
	return m_componentSubType;
    }

    inline void HdlrAtom::setComponentSubType(unsigned componentSubType)
    {
	m_componentSubType = componentSubType;
    }

};

#endif
