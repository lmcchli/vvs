#include "MockMediaObjectNativeAccess.h"
#include "MockByteBuffer.h"

#include <iostream>

#include <fcntl.h>
#if defined(WIN32)
#include "io.h"
#include "windows.h"
#else
#define O_BINARY 0
#include <unistd.h>
#endif
#include <base_include.h>
#include <vector>

#include <assert.h>

using base::String;
using std::vector;
using std::cout;
using std::endl;

enum {
	AppendBySize = 1,
	AppendByBuffer,
	IteratorGetter
};

MockMediaObjectNativeAccess::MockMediaObjectNativeAccess() 
	: MockObject("MediaObjectNativeAccess"),
	  m_fileSize(0L)
{
}

base::String MockMediaObjectNativeAccess::mediaFilePath(const base::String& path,const base::String& name,const base::String& extension) {
	#if defined(WIN32)
		//string fileName = path + "\\" + name + "." + extension;
		base::String fileName = name + "." + extension;
	#else
		base::String fileName = path + "/" + name + "." + extension;
	#endif
	return fileName;
}


MockMediaObjectNativeAccess::MockMediaObjectNativeAccess(const base::String& path,
                                                         const base::String& name, 					       
                                                         const base::String& extension,
                                                         int chunkSize) :
    MockObject("MediaObjectNativeAccess")
{
    base::String fileName = mediaFilePath(path,name,extension);

	char* chunk = new char[chunkSize];
    int fd = open(fileName.c_str(), O_RDONLY | O_BINARY);
    // TODO: handle file error!
	if (fd < 0) {
		cout << "********************************************************************" << endl;
		cout << "Fatal error in " << __FILE__ << " at line " << __LINE__ << endl
		     << "  Could not open '"<< fileName << "'" << endl;
		cout << "********************************************************************" << endl;
		abort();
	}
    m_fileSize = lseek(fd, 0L, SEEK_END);
    lseek(fd, 0L, SEEK_SET);
    int nOfChunks = m_fileSize/chunkSize + (m_fileSize%chunkSize ? 1 : 0);
    
    for (int i = 0; i < nOfChunks; i++) {
        int readChunkSize = read(fd, chunk, chunkSize);
        m_mockByteBuffers.push_back(new MockByteBuffer(readChunkSize, chunk));
    }
    close(fd);
    delete [] chunk;
}

MockMediaObjectNativeAccess::~MockMediaObjectNativeAccess()
{
    for (unsigned int i = 0; i < m_mockByteBuffers.size(); i++) {
        delete m_mockByteBuffers[i];
        m_mockByteBuffers[i] = (MockByteBuffer*)0;
    }
}

int MockMediaObjectNativeAccess::getMethodId(const base::String& name, const base::String& signature)
{
	if (name == "append") {
		if (signature == "(I)Ljava/nio/ByteBuffer;") {
			return AppendBySize;
		} else if (signature == "(Ljava/nio/ByteBuffer)Ljava/nio/ByteBuffer;") {
			return AppendByBuffer;
		}
	} else if (name == "iterator") {
		return IteratorGetter;
	}
	return 0;
}  

void* MockMediaObjectNativeAccess::callMethod(int methodId, va_list& args)
{
	switch (methodId) {
	case AppendBySize:		
		{
			int size = (int)va_arg(args, int);
			MockByteBuffer* buffer = new MockByteBuffer(size);
			m_mockByteBuffers.push_back(buffer);
			return (void*)buffer;
		}
		break;
	
	case AppendByBuffer:
		{
			MockByteBuffer* source = (MockByteBuffer*)va_arg(args, MockByteBuffer*);
			MockByteBuffer* buffer = new MockByteBuffer(*source);
			m_mockByteBuffers.push_back(buffer);
			return (void*)buffer;
		}
		break;
		
	case IteratorGetter:
		m_mockMediaObjectIterator.m_mockByteBuffers = m_mockByteBuffers;
		m_mockMediaObjectIterator.m_currentBuffer = 0;
		return (void*)&m_mockMediaObjectIterator;
		break;
		
	default:
		break;
	} 
	return (void*)0;
}

void MockMediaObjectNativeAccess::appendBySize(int size)
{
}

void MockMediaObjectNativeAccess::appendByBuffer(MockByteBuffer& mockByteBuffer)
{
}
