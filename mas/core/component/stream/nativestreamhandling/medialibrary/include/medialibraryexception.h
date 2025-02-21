/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIALIBRARYERROR_H_
#define MEDIALIBRARYERROR_H_

#include <cc++/exception.h>
#include <base_include.h>

/**
 * Exception thrown by the MediaLibrary module.
 * @author Mats Egland
 */
class MediaLibraryException: public ost::Exception
{

public:
    /**
     * Constructs a MediaLibraryException with the given message.
     * @param s Descriptive message
     */
    explicit MediaLibraryException(const base::String &s) :
            Exception(s)
    {
    }
};

#endif /*MEDIALIBRARYERROR_H_*/
