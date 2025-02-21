/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * Contains the configured restricted outbound headers.
 * <p>
 * The restricted outbound headers can be created by parsing the configuration.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Nyfeldt
 */
public class RestrictedOutboundHeaders {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(RestrictedOutboundHeaders.class);

    public enum RestrictedHeader {
        P_ASSERTED_IDENTITY,
        REMOTE_PARTY_ID
    }

    // Configuration related constants
    private static final String REMOTE_PARTY_ID = "remote-party-id";
    private static final String P_ASSERTED_IDENTITY = "p-asserted-identity";

    private final Set<RestrictedHeader> restrictedHeaders =
            new HashSet<RestrictedHeader>();

    /**
     * Use this method to find out if a specific header is restricted.
     * @param   header    The header to control.
     * @return  Returns true if the given header is restricted, false otherwise.
     */
    public boolean isRestricted(RestrictedHeader header) {
        return restrictedHeaders.contains(header);
    }

    /**
     * @return Returns the amount of restricted headers.
     */
    public int getAmountOfRestrictedHeaders() {
        return restrictedHeaders.size();
    }

    /**
     * Parses the configuration for restricted outbound headers and returns a
     * {@link RestrictedOutboundHeaders}.
     * @param configGroup
     * @return a {@link RestrictedOutboundHeaders} containing all restricted
     * headers that should not be included in an outdial INVITE.
     */
    public static RestrictedOutboundHeaders parseRestrictedOutboundHeaders(
			IGroup configGroup) {

		RestrictedOutboundHeaders restrictedHeaders = new RestrictedOutboundHeaders();

		try {
			if (configGroup.getBoolean(ConfigConstants.RESTRICTED_OUTBOUND_HEADERS_REMOTE_PARTY_ID)) {
				restrictedHeaders.addRestrictedHeader(RestrictedHeader.REMOTE_PARTY_ID);
			}
			if (configGroup.getBoolean(ConfigConstants.RESTRICTED_OUTBOUND_HEADERS_P_ASSERTED_IDENTITY)) {
				restrictedHeaders.addRestrictedHeader(RestrictedHeader.P_ASSERTED_IDENTITY);
			}
		} catch (Exception e) {
			if (LOG.isDebugEnabled())
				LOG.debug("Restricted outbound headers could not be retrieved "
						+ "from configuration.");
		}

		return restrictedHeaders;
	}

    /**
     * Returns a default set of restricted outbound headers.
     * The default set is empty, i.e. no headers are restricted by default.
     * @return A {@link RestrictedOutboundHeaders} instance is returned
     * containing the default set of restricted headers.
     */
    public static RestrictedOutboundHeaders getDefaultRestrictedOutboundHeaders() {
        return new RestrictedOutboundHeaders();
    }


    //=========================== Private methods =========================

    /**
     * Adds a header to the set of restricted header.
     * @param header    Contains the restricted header.
     */
    private void addRestrictedHeader(RestrictedHeader header) {
        restrictedHeaders.add(header);
    }

}
