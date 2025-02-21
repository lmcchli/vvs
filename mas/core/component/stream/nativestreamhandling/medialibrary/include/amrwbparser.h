#ifndef AmrwbParser_h
#define AmrwbParser_h

#include <amrparser.h> //inherits.
#include <amrinfo.h>
#include <amrwbinfo.h>
#include <jni.h>
#include <mediaobject.h>

#include <boost/ptr_container/ptr_vector.hpp>
#include <base_include.h>

class AmrwbParser: public AmrParser
{
public:
    /**
     * The constructor taking a MediaObject containing MOV data
     */
    AmrwbParser(java::MediaObject* mediaObject);
	virtual ~AmrwbParser();
private:
	static const char* AMRWB_CLASSNAME;

};
#endif
