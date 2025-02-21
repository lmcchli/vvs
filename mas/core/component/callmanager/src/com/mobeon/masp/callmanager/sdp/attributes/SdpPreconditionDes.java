package com.mobeon.masp.callmanager.sdp.attributes;

import java.util.concurrent.atomic.AtomicReference;

import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.SdpFactory;

/**
 * This class represents the content of a des SDP attribute.
 * As defined in RFC3312: Integration of Resource Management and SIP.
 * <p>
 *       desired-status     =  "a=des:" precondition-type
 *                             SP strength-tag SP status-type
 *                             SP direction-tag
 *                             
 * <p>
 * This class is immutable. 
 */
public class SdpPreconditionDes extends SdpPrecondition {

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();
    
    public SdpPreconditionDes(String preconditionType, StrengthTag strengthTag, StatusType statusType, DirectionTag directionTag) {
        super(preconditionType, strengthTag, statusType, directionTag);
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
                SdpConstants.ATTRIBUTE_PRECONDITION_DESIRED, toString());
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = getPreconditionType() + " " + getStrengthTag() + " " + getStatusType() + " " + getDirectionTag();
            stringRepresentation.set(representation);
        }

        return representation;
    }
}
