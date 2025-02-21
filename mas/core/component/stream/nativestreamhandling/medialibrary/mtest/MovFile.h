#ifndef _MovFile_h_
#define _MovFile_h_

#include <AtomWriter.h>
#include <AtomReader.h>

#include <base_include.h>

#ifndef WIN32
#define O_BINARY 0
#endif

/**
 * This is a class for accessing MOV files.
 *
 * The main purpose of this class is to provide file access to
 * quicktime atom test cases.
 */
class MovFile : public quicktime::AtomWriter, public quicktime::AtomReader {
 public:
    /**
     * The mode/state of this file.
     */
    enum Mode {
	CLOSED,
	OPEN_AS_IO,
	OPEN_AS_INPUT, 
	OPEN_AS_OUTPUT
    };

 public:
    /**
     * The constructor.
     *
     * Input: the file name.
     */
    MovFile(const char* fileName);

    /**
     * Opens the file i a specified mode.
     *
     * Input:
     * - mode can be either OPEN_AS_INPUT or OPEN_AS_OUTPUT.
     */
    unsigned open(Mode mode);

    /**
     * Closes the file.
     */
    bool close();

    /**
     * Reads some byets from the file.
     *
     */
    bool read(char* data, unsigned size);

    /**
     * Reads a byte from the file.
     */
    bool readByte(unsigned char& data);

	/**
     * Reads a short from the file.
     */
    bool readW(unsigned short& data);

    /**
     * Reads an unsigned from the file.
     */
    bool readDW(unsigned& data);

    /**
     * Writes some bytes to the file.
     */
    unsigned write(const char* data, unsigned size);

     /**
     * Writes a byte to the file.
     */
    unsigned writeByte(unsigned char data);

	/**
     * Writes a short to the file.
     */
    unsigned writeW(unsigned short data);

    /**
     * Writes an unsigned to the file.
     */
    unsigned writeDW(unsigned data);

    /**
     * Returns the current file position.
     */
    unsigned tell();

    /**
     * Moves the file pointer according to offset and mode.
     */
    unsigned seek(unsigned offset, unsigned mode);

    /**
     * Returns the size of the file.
     */
    unsigned size();

    /**
     * Copies the file to another.
     *
     * The current file is copied to the one of which the name is 
     * given. The current file is closed. The new file is not 
     * opened.
     */
    bool copyTo(const char* fileName);

    /**
     * Finds the first occurence of pattern.
     * 
     * Returns true if the atom name was found and false otherwise.
     * If the atom name was found the file pointer is positioned 
     * at the end of the atom name.
     * The file is traversed four bytes at a time (unsigned).
     */
    bool find(unsigned atomName);

    bool isReadable();
    bool isWriteable();
    
 private:
    /**
     * The open mode of the file.
     */
    Mode m_mode;

    /**
     * The file name.
     */
    base::String m_fileName;

    /**
     * The file handle.
     */
    int m_fd;
    
};

inline bool
MovFile::isWriteable()
{
    return m_mode == OPEN_AS_OUTPUT || m_mode == OPEN_AS_IO;
}


inline bool
MovFile::isReadable()
{
    return m_mode == OPEN_AS_INPUT || m_mode == OPEN_AS_IO;
}

#endif
