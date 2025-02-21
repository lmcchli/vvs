#ifndef _AtomReader_h_
#define _AtomReader_h_

#include "AtomName.h"
#include <string>

/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

namespace quicktime {
    /**
     * Reader of QuickTime atoms.
     *
     * This class defines an interface for the I/O functionality
     * that is required for restoring an atom from media.
     *
     * This class is pure virtual.
     *
     * You can select a variant of quicktime file to control how atoms parse and
     * and store data. There are currently two variants:<ul>
     *<li><b>QT</b> uses variable-length strings in pascal format, i.e. a length
     * byte followed by characters.
     *<li><b>MPEG4</b> uses variable-length strings in C format,
     * i.e. null-terminated.
     *</ul>Other variants and differences may be added as the need arises.
     */
    class AtomReader {
    public:
	enum {
	    SEEK_FORWARD,
	    SEEK_BACKWARD,
	    SEEK_SET_POSITION
	};

	enum {
	    QT,
	    MPEG4
	};

	AtomReader();

	/**
	 * Sets the current position in the media.
	 */
	virtual unsigned seek(unsigned position, unsigned mode) = 0;

	/**
	 * Returns the current position in the media.
	 */
	virtual unsigned tell() = 0;

	virtual bool readByte(unsigned char& byte) = 0;

	/**
	 * Reads short (two bytes).
	 */
	virtual bool readW(unsigned short& word) = 0;

	/**
	 * Reads int (four bytes).
	 */
	virtual bool readDW(unsigned& doubleWord) = 0;

	/**
	 * Reads a number of bytes into a character array.
	 *@param buf a pointer to a character array of sufficient size.
	 *@param len the number of bytes to read.
	 */
	virtual bool read(unsigned char* buf, int len);

	/**
	 * Set the kind of quicktime specification to follow when reading.
	 */
	virtual void setQuicktimeVariant(int variant);

	/**
	 * Reads a (variable length) string according to the format specified in
	 * by the variant.
	 *@param maxlen Safety limit for the size of a string.
	 *@return a null-terminated string read from the atom source. The string
	 * must be deleted by the caller.
	 */
	virtual char* readString(int maxlen, bool skipTail=true);
	
	//Given an AtomName will populate the given string with the decoded name.
	static void getAtomNameAsString(AtomName name, std::string &stringName);

	virtual ~AtomReader(){};
    protected:
	char* readPascalString(int maxlen, bool skipTail);
	char* readCString(int maxlen);

	int m_variant;
    };
};



#endif
