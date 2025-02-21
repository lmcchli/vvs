/**
COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.

THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
TO ANY OTHER PERSON OR ENTITY.
TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
*/
package com.mobeon.common.smscom.udh;

/**
 * This class represent the Information Element of the Special SMS Message Indication
 * @author esebpar
 * @since MiO 2.0
 * @see 3GPP TS 23.040 v6.7.0 (9.2.3.24.2 Special SMS Message Indication)
 */
public class SpecialSMSMessageIndication extends IE{

    /**
     * Constructor of the Special SMS Message Indication
     * @param messageType The message type of this SMS indication (voice, fax, email, extended)
     * @param extendedMessageType An extended message type if the message type is extended. (None, video)
     * @param store If the sms shall be store or discarded
     * @param profileID The profile ID see 3GPP TS 23.097
     * @param nbOfMessage The number of new message of type.
     */
    public SpecialSMSMessageIndication(int messageType, int extendedMessageType, boolean store, int profileID, int nbOfMessage){
        //The IEI of the Special SMS Message Indication is 0x01 
        iei = 0x01;
        //The length is 2
        data = new byte[2];
        
        //The data, see 3GPP TS 23.040 v6.7.0 (9.2.3.24.2 Special SMS Message Indication) to understand
        data[0] = (byte) (store ? 0x80 : 0x00);
        data[0] |= (byte) (messageType & 0x03);
        if((messageType & 0x03) == 0x03){
            data[0] |= (byte) ((extendedMessageType << 2) & 0x1c);
        }
        
        data[0] |= (byte) ((profileID << 5) & 0x60);
        
        if(nbOfMessage > 255){
            nbOfMessage = 255;
        }
        
        data[1] = (byte) (nbOfMessage & 0xff);
    }
    
    /**
     * Constructor of the Special SMS Message Indication
     * @param messageType Message type to use, shall be VOICE, FAX and EMAIL
     * @param store If the sms shall be store or discarded
     * @param nbOfMessage The number of new message of type.
     */
    public SpecialSMSMessageIndication(MessageType messageType, boolean store, int nbOfMessage){
        this(messageType.getMessageTypeValue(), ExtendedMessageType.NONE.getExtendedMessageTypeValue(), store, ProfileID.PROFILE_ID_1.getProfileIDValue(), nbOfMessage);
    }
    
    /**
     * Constructor of the Special SMS Message Indication
     * @param messageType Message type to use, shall be VOICE, FAX and EMAIL
     * @param store If the sms shall be store or discarded
     * @param profileID The profile ID see 3GPP TS 23.097
     * @param nbOfMessage The number of new message of type.
     */
    public SpecialSMSMessageIndication(MessageType messageType, boolean store, ProfileID profileID, int nbOfMessage){
        this(messageType.getMessageTypeValue(), ExtendedMessageType.NONE.getExtendedMessageTypeValue(), store, profileID.getProfileIDValue(), nbOfMessage);
    }
    
    /**
     * Constructor of the Special SMS Message Indication
     * @param messageType Message type to use
     * @param extendedMessageType An extended message type if the message type is extended. (None, video)
     * @param store If the sms shall be store or discarded
     * @param profileID The profile ID see 3GPP TS 23.097
     * @param nbOfMessage The number of new message of type.
     */
    public SpecialSMSMessageIndication(MessageType messageType, ExtendedMessageType extendedMessageType, boolean store, ProfileID profileID, int nbOfMessage){
        this(messageType.getMessageTypeValue(), extendedMessageType.getExtendedMessageTypeValue(), store, profileID.getProfileIDValue(), nbOfMessage);
    }
    
    /**
     * Constructor of the Special SMS Message Indication
     * @param messageType Message type to use
     * @param extendedMessageType An extended message type if the message type is extended. (None, video)
     * @param store If the sms shall be store or discarded
     * @param nbOfMessage The number of new message of type.
     */
    public SpecialSMSMessageIndication(MessageType messageType, ExtendedMessageType extendedMessageType, boolean store, int nbOfMessage){
        this(messageType.getMessageTypeValue(), extendedMessageType.getExtendedMessageTypeValue(), store, ProfileID.PROFILE_ID_1.getProfileIDValue(), nbOfMessage);
    }
    
    /**
     * The Special SMS Message Indication is repeatable
     */
    @Override
    public boolean isRepeatable(){
        return true;
    }
    
    /**
     * This enum are the possible value of the Message Type.
     * @author esebpar
     * @since MiO 2.0
     *
     */
    public enum MessageType{
        VOICE(0x00),
        FAX(0x01),
        EMAIL(0x02),
        EXTENDED(0x03);
        
        private int messageTypeValue = 0x00;
        
        private MessageType(int messageTypeValue){
            this.messageTypeValue = messageTypeValue;
        }

        public int getMessageTypeValue() {
            return messageTypeValue;
        }
    }
    
    /**
     * This enum are the possible value of the Extended Message Type.
     * The Extended Message Type shall only be use when the Message Type is of type extended.
     * @author esebpar
     * @since MiO 2.0
     *
     */
    public enum ExtendedMessageType{
        NONE(0x00),
        VIDEO(0x01);
        
        private int extendedMessageTypeValue = 0x00;
        
        private ExtendedMessageType(int extendedMessageTypeValue){
            this.extendedMessageTypeValue = extendedMessageTypeValue;
        }

        public int getExtendedMessageTypeValue() {
            return extendedMessageTypeValue;
        }
    }
    
    /**
     * The Profile ID to use for the SMS Message Indication
     * @see 3GPP TS 23.097
     * @author esebpar
     * @since MiO 2.0
     *
     */
    public enum ProfileID{
        PROFILE_ID_1(0x00),
        PROFILE_ID_2(0x01),
        PROFILE_ID_3(0x02),
        PROFILE_ID_4(0x03);
        
        private int profileIDValue = 0x00;
        
        private ProfileID(int profileIDValue){
            this.profileIDValue = profileIDValue;
        }

        public int getProfileIDValue() {
            return profileIDValue;
        }
    }
}
