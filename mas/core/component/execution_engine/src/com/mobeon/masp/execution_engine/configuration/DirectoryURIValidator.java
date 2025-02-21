package com.mobeon.masp.execution_engine.configuration;

/**
 * @author Mikael Andersson
 */
public class DirectoryURIValidator extends FileURIValidator {
    public boolean requireDir() { return true; }
}
