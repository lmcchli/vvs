/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef STACKEXCEPTION_H_
#define STACKEXCEPTION_H_

#include "platform.h"

/**
 * Definition of supported exception types that can be thrown
 * to Java-space.
 * 
 * @author Jörgen Terner
 */
class MEDIALIB_CLASS_EXPORT StackException {
private:
    StackException(const char* name) : mName(name) {};
    
    /** Name of Java-exception class. */
    const char* mName;

public:
    /** Used when the stack does not support the operation being invoked. */
    static const StackException UNSUPPORTED_OPERATION;
    
    /** 
     * Used when the stack is in an illegal state. This could for example be
     * when join is called for an already joined outbound stream. 
     */
    static const StackException ILLEGAL_STATE;

    /**
     * Used when creating a local session fails. This might be because
     * the portnumbers where already in use.
     */
    static const StackException CREATE_LOCAL_SESSION_FAILURE;
    
    /** 
     * General exception class used when no more specific
     * exception class is necessary. This might be when the cause is
     * unknown and/or the program does not know how to recover.
     */
    static const StackException STACK_EXCEPTION;    
    
    const char* getName() const {
        return mName;
    }
};

#endif /*STACKEXCEPTION_H_*/
