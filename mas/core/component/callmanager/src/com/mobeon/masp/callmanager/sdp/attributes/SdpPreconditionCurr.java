package com.mobeon.masp.callmanager.sdp.attributes;

import java.util.concurrent.atomic.AtomicReference;

import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.SdpFactory;

/**
 * This class represents the content of a curr SDP attribute.
 * As defined in RFC3312: Integration of Resource Management and SIP.
 * <p>
 *       current-status     =  "a=curr:" precondition-type
 *                              SP status-type SP direction-tag
 *                             
 * <p>
 * This class is immutable.
 */
public class SdpPreconditionCurr extends SdpPrecondition {
        
    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();
    
    public SdpPreconditionCurr(String preconditionType, StatusType statusType, DirectionTag directionTag) {
        super(preconditionType, null, statusType, directionTag);
    }

  
    /**
     * Encodes the curr into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link Attribute} is returned with "curr" as name and
     * format specific parameters as value.
     * @param   sdpFactory SdpFactory
     * @return  An precondition curr attribute.
     * @throws  SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_PRECONDITION_CURRENT, toString());
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = getPreconditionType() + " " + getStatusType() + " " + getDirectionTag();
            stringRepresentation.set(representation);
        }

        return representation;
    }


    /**
     * 
     * 
     * @param desiredStatus SdpPreconditionDes
     * @return true if the current status reaches or surpasses the threshold set by the desired status. 
     *         This does not take into account the strength-tag of the desired status.
     * @throws IllegalArgumentException if the status type of the desiredStatus does not match this object status type
     */
    public boolean isPreconditionReached(SdpPreconditionDes desiredStatus) throws IllegalArgumentException {
        if(getStatusType() != desiredStatus.getStatusType()) {
            throw new IllegalArgumentException("SDP Precondition status type do not match.");
        }
        
        DirectionTag directionTag = getDirectionTag();
        if(directionTag == DirectionTag.SENDRECV || 
           directionTag == desiredStatus.getDirectionTag() || 
           desiredStatus.getDirectionTag() == DirectionTag.NONE) {
            return true;
        } 
        
        return false;
    }
}
