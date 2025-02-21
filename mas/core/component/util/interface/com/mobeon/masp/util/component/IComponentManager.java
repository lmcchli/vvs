/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.util.component;

/**
 * The component manager is acting as an object factory, creating instances of
 * requested components. The manager reads a configuration at start-up, where a
 * binding between a logical name and an actual implementation is made.
 * A specific component may be registered in the configuration as being singleton or
 * template, singleton meaning that the same instance of the requested component
 * will be re-used for each request, template meaning that a new instance will be
 * created for each request.
 * <p>
 * The interfaces that can be created and returned by the IComponentManager must
 * contain a default constructor (i.e a constructor that accepts no parameters) and
 * get/set methods for all public members.
 *
 * @author David Looberger
 */
public interface IComponentManager {

    /**
     * Create a the specified bean/object. Depending on the configuration read by the
     * component manager, the returned object will be a singleton or a template.
     * The componentSpecification is an identity that can be found in the configuration.
     * In generall the componentSpecification should be the interface name.
     * An Exception is thrown if the request can not be fullfilled.
     * <p>
     * @param componentSpecification
     * @return  the component
     */
    public Object create(String componentSpecification);

    /**
     * Create a component according to the componentSpecification. Depending on the configuration read by the
     * component manager, the returned object will be a singleton or a template.
     * The componentSpecification is an identity that can be found in the configuration.
     * In generall the componentSpecification should be the interface name.
     * The Class parameter state the required type
     * (typically interface) the created component must fulfill. An Exception is thrown if the request can not be
     * fullfilled.
     * <p>
     * @param componentSpecification
     * @param clazz A type that the requested component must fulfill.
     * @return the requested component
     */
    public Object create(String componentSpecification, Class clazz);
}
