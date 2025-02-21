package com.mobeon.masp.execution_engine.ccxml.runtime;

/**
 * @author David Looberger
 */
public interface IdGenerator<T> {

    /**
     * Create and return a new Id
     * @return the newly created Id
     */
    public Id<T> generateId();

}
