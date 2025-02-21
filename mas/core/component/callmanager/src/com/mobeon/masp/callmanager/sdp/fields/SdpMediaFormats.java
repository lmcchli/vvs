/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A container for the media formats present in the Media field of a
 * Media Description.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpMediaFormats {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(SdpMediaFormats.class);

    private final Vector<Integer> formats;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpMediaFormats(Vector<Integer> formats) {
        this.formats = formats;
    }

    public Vector<Integer> getFormats() {
        return formats;
    }

    /**
     * @param   format
     * @return  Returns true if the given <param>format</param> is among the
     *          media format list. Otherwise false is returned.
     */
    public boolean isFormatSupported(int format) {
        return formats.contains(format);    
    }


    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = getFormats().toString();
            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * Parses a vector of strings of formats and converts it into an
     * {@link SdpMediaFormats}.
     * Null is returned if the <param>formats</param> vector is null or empty.
     * <p>
     * If the <param>formats</param> vector contains an entry that cannot be
     * formatted as an Integer, an {@link SdpNotSupportedException} is
     * thrown with {@link SipWarning.INCOMPATIBLE_MEDIA_FORMAT}.
     *
     * @param   formats     The media formats.
     * @return  An {@link SdpMediaType}.
     *          Null is returned if <param>formats</param> is null or empty.
     */
    public static SdpMediaFormats parseMediaFormats(Vector<String> formats)
            throws SdpNotSupportedException {
        SdpMediaFormats sdpMediaFormats = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing media formats: " + formats);

        if ((formats != null) && (!formats.isEmpty())) {

            Vector<Integer> integerFormats = new Vector<Integer>();

            for (String formatStr : formats) {
                Integer formatInt;
                try {
                    formatInt = Integer.valueOf(formatStr);
                } catch (NumberFormatException e) {
                    String message =
                            "Could not parse format from Media formats <" +
                                    formats + "> received in remote SDP. " +
                                    "The call will not be setup.";
                    LOG.warn(message, e);
                    throw new SdpNotSupportedException(
                            SipWarning.INCOMPATIBLE_MEDIA_FORMAT, message);
                }

                integerFormats.add(formatInt);
            }

            sdpMediaFormats = new SdpMediaFormats(integerFormats);
        }

        return sdpMediaFormats;
    }

}
