package com.mobeon.masp.logging;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.RepositorySelector;

/**
 * @author David Looberger
 */
public class HierarchyExtender extends Hierarchy {
    private static final LoggerFactory defaultFactory = new Log4JLoggerFactory();
    /**
     * Create a new logger hierarchy.
     *
     * @param root The root of the new hierarchy.
     */
    public HierarchyExtender(Logger root) {
        super(root);
    }

    public Logger getLogger(String name) {
        return getLogger(name, defaultFactory);
    }

    public Logger getDefaultLogger(String name) {
        return super.getLogger(name);
    }
}
