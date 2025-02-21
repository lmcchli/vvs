#ifndef _MdatAtom_h_
#define _MdatAtom_h_

#include <Atom.h>
#include <AtomWriter.h>

#include <vector>

namespace quicktime {
    /**
     * This is the Movie Data Atom
     * The Movie Data Atom defines the start and size of the movie data.
     * The purpose of this class is to provide a utility for determining
     * the size of the space occupied by the movie data.
     * This class can also be used when pre calculating the data offsets
     * that are stored in the meta data ('moov').
     */
    class MdatAtom : public Atom, public AtomWriter {
    public:
	/**
	 * This is the default constructor.
	 */
	MdatAtom();

	/**
	 * The destructor.
	 */
	~MdatAtom();

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
	 * Setter for the movie data file offset.
	 */
	void setOffset(unsigned offset);

	/**
	 * Getter for the movie data size.
	 */
	unsigned getSize();

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
	bool operator==(MdatAtom& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(MdatAtom& leftAtom);

    private:
	/**
	 * The size of the movie data.
	 */
	unsigned m_size;

	/**
	 * Starting position of the movie data area.
	 */
	unsigned m_offset;
    };

    inline void MdatAtom::setOffset(unsigned offset)
    {
	m_offset = offset;
    }

    inline unsigned MdatAtom::getSize()
    {
	return m_size;
    }
};

#endif
