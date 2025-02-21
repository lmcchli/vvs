/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * The interface to provide access to the factory used to instantiate NotifierLogger objects.
 */
public interface INotifierLoggerFactory {

    /**
     * Gets the NotifierLogger for the given className
     * @param theClass The class to instantiate a logger for
     * @return the NotifierLogger for the given Class
     */
    public INotifierLogger getLogger(Class<?> theClass);
    
    /**
     * Gets the NotifierLogger for the given className
     * @param className The class name to instantiate a logger for
     * @return the NotifierLogger for the given className
     */
    public INotifierLogger getLogger(String className);
    
}
