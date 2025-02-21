package com.mobeon.masp.execution_engine.platformaccess;

/**
 * Exception thrown when a distribution list could not be found
 *
 * @author mande
 */
public class DistributionListNotFoundException extends Exception {
    public DistributionListNotFoundException(String message) {
        super(message);
    }
}
