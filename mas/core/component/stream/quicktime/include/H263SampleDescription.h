#ifndef _H263SampleDescription_h_
#define _H263SampleDescription_h_

#include <SampleDescription.h>

namespace quicktime {
    /**
     * This class is the sample description for H263 video tracks in a 3gp
     * file. 
     */ 
    class H263SampleDescription : public sampleDescriptionAtom {
    public:
	void setHeight(unsigned char v);
	void setWidth(unsigned char v);
	void setVendor(unsigned v);

	/**
	 * This is the default constructor.
	 */
	H263SampleDescription();

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
	bool operator==(H263SampleDescription& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(H263SampleDescription& leftAtom);
	
	/**
	 * Calculates and returns the total size of the atom.
	 * The calcuation is based upon the size of this atom
	 * and all sub-atoms.
	 */
	unsigned getAtomSize();

    private:
	unsigned m_vendor;
	
	unsigned short m_width;

	unsigned short m_height; 

    };
};

#endif
