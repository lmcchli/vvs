#ifndef _AtomWriter_h_
#define _AtomWriter_h_

/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

namespace quicktime {
    /**
     * Reader of QuickTime atoms.
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
    class AtomWriter {
    public:
	enum {
	    SEEK_FORWARD /*,
	    SEEK_BACKWARD,
	    SEEK_SET_POSITION */
	};

	enum {
	    QT,
	    MPEG4
	};

	AtomWriter();

	/**
	 * Sets the current position in the media.
	 */
	virtual unsigned seek(unsigned position, unsigned mode) = 0;

	/**
	 * Returns the current position in the media.
	 */
	virtual unsigned tell() = 0;

	virtual unsigned writeByte(unsigned char byte) = 0;

	/**
	 * Writes short (two bytes).
	 */
	virtual unsigned writeW(unsigned short word) = 0;

	/**
	 * Writes int (four bytes).
	 */
	virtual unsigned writeDW(unsigned doubleWord) = 0;

	/**
	 * Writes a chunk of data.
	 */
	virtual unsigned write(const char* data, unsigned size) = 0;

	virtual void setQuicktimeVariant(int variant);

	virtual unsigned writeString(char* str, int maxlen);

	virtual ~AtomWriter() {};
    protected:
	virtual unsigned writePascalString(char* str, int maxlen);

	virtual unsigned writeCString(char* str, int maxlen);

	int m_variant;
    };
};

#endif
