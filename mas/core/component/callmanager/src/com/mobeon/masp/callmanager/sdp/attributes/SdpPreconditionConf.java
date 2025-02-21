package com.mobeon.masp.callmanager.sdp.attributes;

import java.util.concurrent.atomic.AtomicReference;

import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.SdpFactory;

/**
 * This class represents the content of a conf SDP attribute.
 * As defined in RFC3312: Integration of Resource Management and SIP.
 * <p>
 *       confirm-status     =  "a=conf:" precondition-type
 *                             SP status-type SP direction-tag
 *                             
 * <p>
 * This class is immutable. 
 */
public class SdpPreconditionConf extends SdpPrecondition {

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();
    
    public SdpPreconditionConf(String preconditionType, StatusType statusType, DirectionTag directionTag) {
        super(preconditionType, null, statusType, directionTag);
    }

    /**
     * Encodes the des into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link Attribute} is returned with "des" as name and
     * format specific parameters as value.
     * @param   sdpFactory SdpFactory
     * @return  An precondition des attribute.
     * @throws  SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_PRECONDITION_CONFIRM, toString());
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = getPreconditionType() + " " + getStatusType() + " " + getDirectionTag();
            stringRepresentation.set(representation);
        }

        return representation;
    }
}
