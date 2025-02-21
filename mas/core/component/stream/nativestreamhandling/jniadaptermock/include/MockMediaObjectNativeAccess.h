#ifndef MockMediaObjectNativeAccess_H_
#define MockMediaObjectNativeAccess_H_

#include <MockObject.h>
#include <MockMediaObjectIterator.h>

class MockByteBuffer;

#include <base_include.h>

class MockMediaObjectNativeAccess : public MockObject
{
public:
	MockMediaObjectNativeAccess();
	MockMediaObjectNativeAccess(const base::String& path,
		const base::String& name, 
		const base::String& extension,
		int chunkSize);
	virtual ~MockMediaObjectNativeAccess();
	int getMethodId(const base::String& name, const base::String& signature);  
	virtual void* callMethod(int methodId, va_list& args);
	
	void appendBySize(int size);
	void appendByBuffer(MockByteBuffer& mockByteBuffer);
	static base::String mediaFilePath(
		const base::String& path,
		const base::String& name, 
		const base::String& extension
		);
public:
	MockMediaObjectIterator m_mockMediaObjectIterator;
	std::vector<MockByteBuffer*> m_mockByteBuffers;
	long m_fileSize;
};

#endif /*MockMediaObjectNativeAccess_H_*/
