#ifndef _MEDIAVALIDATOR_H_
#define _MEDIAVALIDATOR_H_

class MediaParser;
//#include <mediaparser.h>

class MediaValidator
{
public:
	virtual void validateMediaProperties(MediaParser *mediaParser) = 0;
	virtual ~MediaValidator() {};
};

#endif
