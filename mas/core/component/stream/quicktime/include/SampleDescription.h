#ifndef _SampleDescription_h_
#define _SampleDescription_h_

#include <Atom.h>
#include <AtomWriter.h>
#include <AtomReader.h>

#include <vector>

namespace quicktime {
    /**
     * This is a sample Description atom, contained in a stsd Atom
     * Strictly speaking it is not an atom or box but it looks like one
	 * This contains a generic copy of the sample-description if not 
	 * directly decoded by the parser.  You can extend it to be an
	 * actual more specific sample such as an amrSampleDescription.
	 * see 8.5.2.3 Semantics of ISO/IEC 14496-12
	 * 
	 * This just keeps the index and the name otherwise known as
	 * codec.  The rest of the data is kept as an array of inner_data
	 * which cna be fetched using the base class methods.
	 * 
	 * To be more specific use the specific sample types like
	 * PCMSampleDescription, AmrSampleDescription etc..
     */
    class sampleDescriptionAtom : public Atom, public AtomWriter {
    public:
	/**
	 * This is the default constructor.
	 */
	sampleDescriptionAtom(AtomName codec);
	
	/**
	 * The destructor.
	 */
	~sampleDescriptionAtom();

	/**
	 * Sets the current position in the media.
	 * This is a simulated operation.
	 */
	unsigned seek(unsigned offset, unsigned mode);

	/**
	 * Returns the current file pointer position.
	 * This is a simulated file access interface. In reality
	 * what is returned is the current size of the simulated 
	 * write.
	 */
	unsigned tell();

	/**
	 * Writes char (one byte).
	 * This is a simulated operation.
	 */
	unsigned writeByte(unsigned char byte);

	/**
	 * Writes short (two bytes).
	 * This is a simulated operation.
	 */
	unsigned writeW(unsigned short word);

	/**
	 * Writes int (four bytes).
	 * This is a simulated operation.
	 */
	unsigned writeDW(unsigned doubleWord);

	/**
	 * Writes a chunk of data.
	 * This is a simulated operation.
	 */
	unsigned write(const char* data, unsigned size);


	/**
	 * Getter for the  atom size.
	 */
	inline unsigned getSize()  { return m_size; }

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
	 * Calculates and returns the total size of the atom.
	 * The calcuation is based upon the size of this atom
	 * and all sub-atoms.
	 */
	unsigned getAtomSize();
		
	inline unsigned short getIndex() {return m_index;}
	inline void setIndex(unsigned short index) {m_index=index;}

	/**
	 * This is the equality operator.
	 */
	bool operator==(sampleDescriptionAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(sampleDescriptionAtom& leftAtom);
	
	protected:
	/**
	 * constructor for decendents
	 */
	 sampleDescriptionAtom(AtomName codec,unsigned size);
	 
	 	/**
	 * The size of the atom.
	 */
	unsigned m_size;

	/**
	 * Data Reference Index, ussually 1 as only 1 sample in table typically.
	 */
	unsigned short m_index;

    private:
    };

};

#endif
