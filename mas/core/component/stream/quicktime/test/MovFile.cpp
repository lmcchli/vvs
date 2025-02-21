#include "MovFile.h"

#include <iostream>

#ifdef WIN32
#include <io.h>
#include <stdio.h>
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#endif
#include <fcntl.h>
#include <stdlib.h>

using std::cout;
using std::endl;
using std::string;

MovFile::MovFile(const char* fileName)
    : m_fileName(fileName),
      m_fd(-1)
{
}

unsigned 
MovFile::open(Mode mode)
{
    int openMode;

    close();

    switch (mode) {
    case MovFile::OPEN_AS_INPUT:
	openMode =  O_RDONLY;
	break;

    case MovFile::OPEN_AS_IO:
	openMode =  O_RDWR;
	break;

    case MovFile::OPEN_AS_OUTPUT:
	openMode =  O_WRONLY | O_CREAT | O_TRUNC;
	break;

    default:
	cout << "Illegal open mode! (" << mode << ")" << endl;
	return CLOSED;
	break;
    }
    m_fd = ::open(m_fileName.c_str(), openMode, 00644);
    if (m_fd < 0) return false;
    
    return m_mode = mode;
}

bool 
MovFile::close()
{
    ::close(m_fd);
    m_mode = CLOSED;
    return true;
}

bool
MovFile::read(char* data, unsigned size)
{
    if (isReadable())
	return ::read(m_fd, data, size) == size;
    return false;
}

bool
MovFile::readW(unsigned short& data)
{
    if (isReadable())
	return ::read(m_fd, (char*)&data, 2) == 2;
    return false;
}

bool
MovFile::readDW(unsigned& data)
{
    if (isReadable())
	return ::read(m_fd, (char*)&data, 4) == 4;
    return false;
}

bool
MovFile::readByte(unsigned char& data)
{
    if (isReadable())
	return ::read(m_fd, (char*)&data, 1) == 1;
    return false;
}

unsigned MovFile::write(const char* data, unsigned size)
{
    if (isWriteable())
	return ::write(m_fd, data, size);
    return 0;
}

unsigned MovFile::writeW(unsigned short data)
{
    if (isWriteable())
	return ::write(m_fd, (char*)&data, sizeof(short));
    return 0;
}

unsigned MovFile::writeDW(unsigned data)
{
    if (isWriteable())
	return ::write(m_fd, (char*)&data, sizeof(int));
    return 0;
}

unsigned MovFile::writeByte(unsigned char data)
{
    if (isWriteable())
	return ::write(m_fd, (char*)&data, sizeof(char));
    return 0;
}

unsigned MovFile::tell()
{
    return ::tell(m_fd);
}

unsigned MovFile::seek(unsigned offset, unsigned mode)
{
    char zero(0);
    switch (m_mode) {
    case OPEN_AS_OUTPUT:
	switch (mode) {
	case AtomWriter::SEEK_FORWARD:
	    for (int i(0); i < offset; i++)
		::write(m_fd, (unsigned char*)&zero, 1);
	    return offset;
	    break;
	    
	case AtomReader::SEEK_SET_POSITION:
	    return ::lseek(m_fd, offset, SEEK_SET);
	    break;

	default:
	    break;
	}
	break;
	
    case OPEN_AS_IO:
    case OPEN_AS_INPUT:
	switch (mode) {
	case AtomReader ::SEEK_FORWARD:
	    return ::lseek(m_fd, offset, SEEK_CUR);
	    break;

	case AtomReader ::SEEK_BACKWARD:
	    return ::lseek(m_fd, -offset, SEEK_CUR);
	    break;
	    
	case AtomReader::SEEK_SET_POSITION:
	    return ::lseek(m_fd, offset, SEEK_SET);
	    break;
	default:
	    break;
	}

    default:
	break;
	
    }
    cout << "Illegal seek!" << endl;
    return (unsigned)-1;
}

unsigned 
MovFile::size()
{
    unsigned position(::tell(m_fd));
    unsigned size(::lseek(m_fd, 0, SEEK_END));

    ::lseek(m_fd, position, SEEK_SET);
    return size;
}

bool
MovFile::copyTo(const char* fileName)
{
    string command("cp -f " + m_fileName + " " + fileName);
    m_fileName = fileName;
    close();
    return system(command.c_str()) == 0;
}

bool
MovFile::find(unsigned atomName)
{
    if (m_mode != OPEN_AS_INPUT && m_mode != OPEN_AS_IO) return false;
    unsigned fileSize(size());
    unsigned nOfBytesRead(0);
    char buffer[4];
    char* pattern((char*)&atomName);
    char ch;

    for (unsigned pos(0); pos < fileSize; pos += nOfBytesRead) {
	if (::lseek(m_fd, pos, SEEK_SET) != pos) return false;
	nOfBytesRead = 0;
	unsigned nOfMatches(0);
	for (int i(0); i < 4; i++) {
	    nOfBytesRead++;
	    if (::read(m_fd, &ch, 1) == 0) return false;
	    if (ch == pattern[i]) nOfMatches++;
	    else break;
	}
	if (nOfMatches == 4) return true;
    }
    return false;
}

