package com.mobeon.masp.logging;

import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.LogManager;

/**
 * @author David Looberger
 */
public class RepositorySelectorImpl implements RepositorySelector {

    private static final RepositorySelectorImpl selector = new RepositorySelectorImpl();

    static {
        LogManager.setRepositorySelector(selector, null);
    }

    final HierarchyExtender repository = new HierarchyExtender(Log4JLogger.getRootLogger());

    public static RepositorySelector getRepositiorySelector() {
        return selector;
    }

    public static HierarchyExtender getInstance() {
        return selector.repository;
    }

    public LoggerRepository getLoggerRepository() {
        return repository;
    }
}
