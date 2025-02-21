package com.mobeon.masp.execution_engine.externaldocument;

/**
 * Wrapper object to be used in the cache.
 *
 * @author ermmaha
 */
public class Resource {

    private long age;
    private Object resource;

    /**
     * Constructor.
     *
     * @param resource
     * @param age
     */
    Resource(Object resource, long age) {
        this.resource = resource;
        this.age = age;
    }

    /**
     * Retrieves age on the resource
     *
     * @return the age
     */
    long getAge() {
        return age;
    }

    /**
     * Retrieves the object
     *
     * @return the object
     */
    Object getResource() {
        return resource;
    }
}
