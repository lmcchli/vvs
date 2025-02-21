/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import static com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPOptionalParamConstants.*;
import com.abcxyz.messaging.common.oam.LogAgent;

/**
 * The SMPPParamsTLV class contains the tag, length, and value for one SMPP optional parameter or vendor specific parameter. 
 * There are several ways of creating an instance of this class.
 * <p>
 * Case A - Creating an instance of this class for optional parameters which are defined in the SMPP Protocol Specification v3.4:<p>
 * 1.SMPPParamsTLV(SMPPOptionalParamsEnum optionalParamEnum, int intValue)<p>
 * 2.SMPPParamsTLV(SMPPOptionalParamsEnum optionalParamEnum, String stringValue)<p>
 * 3.SMPPParamsTLV(SMPPOptionalParamsEnum optionalParamEnum, byte[] byteValue)
 * <p>
 * Case B - Creating an instance of this class for vendor specific parameters:<p>
 * 1.SMPPParamsTLV(int tag, int minValueSize, int maxValueSize, int intValue)<p>
 * 2.SMPPParamsTLV(int tag, int minValueSize, int maxValueSize, String stringValue)<p>
 * 3.SMPPParamsTLV(int tag, int minValueSize, int maxValueSize, byte[] byteValue)
 * <p>
 * In case A, the caller is not required to pass the tag, minValueSize, or maxValueSize. 
 * Instead, the SMPPOptionalParamsEnum constant representing the SMPP optional parameter is passed and 
 * the constructor will get these values from the SMPPOptionalParamsEnum constant. 
 * <p>
 * In case B, the caller is required to pass the tag, minValueSize and maxValueSize since they are vendor specific.
 */
public class SMPPParamsTLV {

    /**Logger for this class.*/
    private static LogAgent logAgent = null;
    
    /**Validation of the TLV should need to be done only for debugging purposes during development.*/ 
    private static boolean isValidationDoneBeforeWritingTLV = false;
    
    /**The tag value of the optional parameter.**/
    private int tag = 0;
    
    /**The length of the value.*/
    private int length = 0; 
    
    /**The minimum length/size that the value can be.*/
    private int minValueSize = 0; 
    
    /**The maximum length/size that the value can be.*/
    private int maxValueSize = 0;
    
    /**Specifies whether the size of the value is fixed or if it can be of variable size.*/
    boolean isSizeVariable = false;
    
    /**The actual length/size of the value passed to the constructor.*/
    private int actualValueSize = 0;
    
    /**If the value passed to the constructor is of type Integer, it will be stored in this parameter.*/
    private int intValue = 0;
    
    /**If the value passed to the constructor is of type String, it will be stored in this parameter.*/
    private String stringValue = null;
    
    /**If the value passed to the constructor is of type byte[], it will be stored in this parameter.*/
    private byte[] stringAsByteArray = null;
    
    /**Specifies whether a value can have a range.*/
    private boolean isValueRangeAvailable = false;
    
    /**The type of the value. It can be an Integer (represented as an integer), Octet-String (represented as a String or byte[]), or C-Octet-String (represented as a String or byte[]).*/
    private int valueType = 0;
    
    /**SMPPOptionalParamsEnum for this optional parameter.*/
    private SMPPOptionalParamsEnum optionalParamEnum = null;
    

    /**
     * Sets the logger for this class.
     * The logger is set by NTF.
     * @param logger the logger to be used by this class
     */
    public static void setLogAgent(LogAgent logger){
        logAgent = logger;
    }
    
    /**
     * Sets whether validation is performed prior to writing the TLV values to the data output stream.
     * This is set by NTF according to {@link SMPPParamValues#isValidationDoneBeforeWritingTLV()}.
     * @param isValidationDone specifies whether validation is performed prior to writing the TLV values to the data output stream
     */
    public static void setIsValidationDoneBeforeWritingTLV(boolean isValidationDone){
        isValidationDoneBeforeWritingTLV = isValidationDone;
    }
    
    /**
     * Constructs an instance of SMPPParamsTLV for the given optional parameter with the given integer value. 
     * @param optionalParamEnum the SMPPOptionalParamsEnum that is associated with the given optional parameter
     * @param intValue the integer value of the optional parameter
     */
    public SMPPParamsTLV(SMPPOptionalParamsEnum optionalParamEnum, int intValue) {
        this.optionalParamEnum = optionalParamEnum;
        this.valueType = INT_TYPE;
        this.tag = optionalParamEnum.getTag();
        this.intValue = intValue;
        this.actualValueSize = getIntValueSize();
        if (!optionalParamEnum.isValueSizeVariable()) {
            this.length = optionalParamEnum.getLength();
        } else {
            this.isSizeVariable = true;
            this.minValueSize = optionalParamEnum.getMinValueSize();
            this.maxValueSize = optionalParamEnum.getMaxValueSize();
            if(this.actualValueSize<this.minValueSize){
                //E.g. If the minimum size is 2 bytes and the integer only occupies 1 byte. The value
                //of the length field (specified by the length parameter) must be 2 bytes even though the size of 
                //the value is only 1 byte. When written to the output stream, the first octet will will be padded
                //with zeroes.
                this.length = this.minValueSize;
            }else{
                this.length = this.actualValueSize;
            }
        }
        if (optionalParamEnum.isValueRangeAvailable()) {
            this.isValueRangeAvailable = true;
        } else {
            this.isValueRangeAvailable = false;
        }
    }

    /**
     * Constructs an instance of SMPPParamsTLV for the given optional parameter with the given string value.
     * @param optionalParamEnum the SMPPOptionalParamsEnum that is associated with the given optional parameter
     * @param stringValue the string value of the optional parameter
     */
    public SMPPParamsTLV(SMPPOptionalParamsEnum optionalParamEnum, String stringValue) {
        this.optionalParamEnum = optionalParamEnum;
        this.valueType = STRING_TYPE;
        this.tag = optionalParamEnum.getTag();
        this.stringValue = stringValue;
        if (stringValue != null) {
            this.actualValueSize = getStringValueSize();
            this.stringAsByteArray = convertStringToByteArray();
        }
        if (!optionalParamEnum.isValueSizeVariable()) {
            this.length = optionalParamEnum.getLength();
        } else {
            this.isSizeVariable = true;
            this.length = actualValueSize;
            this.minValueSize = optionalParamEnum.getMinValueSize();
            this.maxValueSize = optionalParamEnum.getMaxValueSize();
        }
    }

    /**
     * Constructs an instance of SMPPParamsTLV for the given optional parameter with the given byte array value.
     * @param optionalParamEnum the SMPPOptionalParamsEnum that is associated with the given optional parameter
     * @param byteValue the byte array representation of the value.
     */
    public SMPPParamsTLV(SMPPOptionalParamsEnum optionalParamEnum, byte[] byteValue) {
        this.optionalParamEnum = optionalParamEnum;
        this.valueType = OTHER;
        this.tag = optionalParamEnum.getTag();
        this.stringAsByteArray = byteValue;
        if (byteValue != null) {
            this.actualValueSize = byteValue.length;
        }
        if (!optionalParamEnum.isValueSizeVariable()) {
            this.length = optionalParamEnum.getLength();
        } else {
            this.isSizeVariable = true;
            this.length = actualValueSize;
            this.minValueSize = optionalParamEnum.getMinValueSize();
            this.maxValueSize = optionalParamEnum.getMaxValueSize();
        }
    }

    /**
     * Constructs an instance of SMPPParamsTLV for a vendor specific parameter with the given integer value.
     * @param tag the tag value of the parameter
     * @param minValueSize the minimum size of the vendor specific parameter value
     * @param maxValueSize the maximum size of the vendor specific parameter value
     * @param intValue the integer value of the vendor specific parameter
     */
    public SMPPParamsTLV(int tag, int minValueSize, int maxValueSize, int intValue) {
        this.valueType = INT_TYPE;
        this.tag = tag;
        this.intValue = intValue;
        this.actualValueSize = getIntValueSize();
        this.minValueSize = minValueSize;
        this.maxValueSize = maxValueSize;
        if (minValueSize == maxValueSize) {
            this.length = minValueSize;
        } else {
            //E.g. if the minimum size is 2 bytes and the integer only occupies 1 byte. The value
            //of the length field (specified by the length parameter) must be 2 bytes eventhough the size of 
            //the value is only 1 byte. When written to the output stream, the first octet will will be padded
            //with zeroes.
            if(this.actualValueSize<this.minValueSize){
                this.length = minValueSize;
            }else{
                this.length = actualValueSize;
            }
            this.isSizeVariable = true;
        }
    }

    /**
     * Constructs an instance of SMPPParamsTLV for a vendor specific with the given string value.
     * @param tag the tag value of the parameter
     * @param minValueSize the minimum size of the vendor specific parameter value
     * @param maxValueSize the maximum size of the vendor specific parameter value
     * @param stringValue the string value of the vendor specific parameter
     */
    public SMPPParamsTLV(int tag, int minValueSize, int maxValueSize, String stringValue) {
        this.valueType = STRING_TYPE;
        this.tag = tag;
        this.stringValue = stringValue;
        if (stringValue != null) {
            this.actualValueSize = getStringValueSize();
            this.stringAsByteArray = convertStringToByteArray();
        }
        this.minValueSize = minValueSize;
        this.maxValueSize = maxValueSize;
        if (minValueSize == maxValueSize) {
            this.length = minValueSize;
        } else {
            this.length = actualValueSize;
            this.isSizeVariable = true;
        }
    }

    /**
     * Constructs an instance of SMPPParamsTLV for a vendor specific parameter with the given byte array value.
     * @param tag the tag value of the parameter
     * @param minValueSize the minimum size of the vendor specific parameter value
     * @param maxValueSize the maximum size of the vendor specific parameter value
     * @param byteValue the byte array representation of the vendor specific parameter value
     */
    public SMPPParamsTLV(int tag, int minValueSize, int maxValueSize, byte[] byteValue) {
        this.valueType = STRING_TYPE;
        this.tag = tag;
        this.stringAsByteArray = byteValue;
        if (byteValue != null) {
            this.actualValueSize = byteValue.length;
        }
        this.minValueSize = minValueSize;
        this.maxValueSize = maxValueSize;
        if (minValueSize == maxValueSize) {
            this.length = minValueSize;
        } else {
            this.length = actualValueSize;
            this.isSizeVariable = true;
        }
    }

    /**
     * Retrieves the SMPPOptionalParamsEnum constant associated with this optional parameter.
     * @return the SMPPOptionalParamsEnum constant associated with this optional parameter; null if this parameter is vendor-specific
     */
    public SMPPOptionalParamsEnum getOptionalParamEnum(){
        return this.optionalParamEnum;
    }
    
    /**
     * Retrieves the tag value of this parameter.
     * @return the tag value of this parameter
     */
    public int getTag() {
        return this.tag;
    }

    /**
     * Retrieves the length/size of the value container for this parameter.
     * <p>
     * The value container size can be greater than the size of the actual value for this parameter. 
     * @return the length/size of the value container for this parameter.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Retrieves the integer value for this parameter.
     * @return the integer value for this parameter if this SMPPParamsTLV was instantiated with an integer value; null otherwise
     */
    public int getIntValue() {
        return this.intValue;
    }

    /**
     * Retrieves the string value for this parameter as byte array.
     * @return the string value for this parameter as byte array if this SMPPParamsTLV was instantiated with a string or byte array value; null otherwise
     */
    public byte[] getStringAsByteArray() {
        return this.stringAsByteArray;
    }

    
    /**
     * Retrieves the type of value contained in this parameter.
     * @return the type of value contained in this parameter (integer or string)
     */
    public int getValueType() {
        return this.valueType;
    }

    /**
     * Retrieves the size of the actual value contained in this parameter.
     * @return the size of the actual value contained in this parameter
     */
    public int getValSize() {
        return this.actualValueSize;
    }

    /**
     * Determines whether the value in this parameter is valid.
     * Validation is performed only if the isValidationDoneBeforeWritingTLV variable is set to true.
     * <p>
     * Potential reasons why a value may be invalid are:<p>
     * 1. It is of the incorrect type (for example, an integer is used where a string is expected or vice versa).<p>
     * 2. There is a defined range for the value contained in this parameter and the given value is not within this range.<p>
     * 3. The size of the value is less than the minimum allowed size or greater than the maximum allowed size.<p>
     * @return true if this parameter is valid or if validation was not performed because isValidationDoneBeforeWritingTLV is set to false
     * @throws SMPPException if this parameter is not valid
     */
    public boolean isValid() throws SMPPException {
        if(!isValidationDoneBeforeWritingTLV){
            logAgent.debug("SMPPParamsTLV.isValid():Validation will not be performed since it is turned off.");
            return true;
        }
        
        if (this.optionalParamEnum != null) {
            logAgent.debug("SMPPParamsTLV.isValid():Validating type.");
            if (!validateType()) {
                return false;
            }
            logAgent.debug("SMPPParamsTLV.isValid():Validating range.");
            if (!validateRange()) {
                return false;
            }
        }
        
        logAgent.debug("SMPPParamsTLV.isValid():Validating Size.");
        if (!validateSize()) {
            return false;
        }

        return true;
    }
    
    /**
     * Converts the the string value in this parameter to a byte array.
     * @return the byte array representation of the string value in this parameter
     */
    private byte[] convertStringToByteArray() {
        return this.stringValue.getBytes();
    }

    /**
     * Retrieves the size of the string value in this parameter.
     * @return the size of the string value in this parameter
     */
    private int getStringValueSize() {
        int count = this.stringValue.length();

        return count;
    }

    /**
     * Retrieves the size of the integer value (no of bytes) in this parameter.
     * @return the size of the integer value (no of bytes) in this parameter
     */
    private int getIntValueSize() {
        int tmpIntValue = this.intValue;

        if (tmpIntValue == 0) {
            return 1;
        }

        int count = 0;

        while (tmpIntValue > 0) {
            count++;
            tmpIntValue >>= 1;
        }

        if (count % 8 == 0) {
            return count / 8;
        } else {
            return (count / 8) + 1;
        }
    }

    /**
     * Validates that the value in this parameter is of the correct type.
     * @return true if the value in this parameter is of the correct type
     * @throws SMPPException if the parameter is not of the correct type
     */
    private boolean validateType() throws SMPPException {
        if (this.optionalParamEnum.getValueType() == INT_TYPE) {
            if (this.valueType != INT_TYPE) {
                throw (new SMPPException("The value for the optional parameter " + this.optionalParamEnum.toString()
                        + " is not of the correct type. It should be implemented as a integer."));
            }
        } else if (this.optionalParamEnum.getValueType() == C_OCTET_STRING_TYPE) {
            if (this.valueType != STRING_TYPE) {
                throw (new SMPPException("The value for the optional parameter " + this.optionalParamEnum.toString()
                        + " is not of the correct type. It should be implemented as a string."));
            }
        } else {
            if (this.valueType != OTHER) {
                throw (new SMPPException("The value for the optional parameter " + this.optionalParamEnum.toString()
                        + " is not of the correct type. It should be implemented as a byte array."));
            }
        }

        return true;
    }

    /**
     * Validates that the value in this parameter is within the defined value range for this parameter.
     * @return true if the value in this parameter is within the defined value range for this parameter
     * @throws SMPPException the value in this parameter is not within the defined value range for this parameter
     */
    private boolean validateRange() throws SMPPException {
        if (this.optionalParamEnum.getValueType() == INT_TYPE) {
            if (this.isValueRangeAvailable) {
                boolean outsideRange = true;
                int[][] valueRange = this.optionalParamEnum.getValueRange();
                String stringRange = "";

                for (int[] range : valueRange) {
                    if (this.intValue < range[0] || this.intValue > range[1]) {
                        stringRange += "(" + range[0] + "," + range[1] + ")";
                    } else {
                        outsideRange = false;
                        break;
                    }
                }

                if (outsideRange) {
                    throw (new SMPPException("The value (" + this.intValue + ") for the optional parameter "
                            + this.optionalParamEnum.toString() + " is outside the allowed range(s) [" + stringRange + "]."));
                }
            }
        } else {
            if (stringAsByteArray == null) {
                throw (new SMPPException(
                        "The value for the optional parameter "
                                + this.optionalParamEnum.toString()
                                + " is null/empty. Therefore, it will not be written. If a null value is required, use 0 or '\0' instead of null."));
            }
        }

        return true;
    }

    /**
     * Validates that the size of the value in this parameter.
     * @return true if the value in this parameter is of the correct size.
     * @throws SMPPException if the value in this parameter is not of the correct size.
     */
    private boolean validateSize() throws SMPPException {
        String paramTag = "";

        if (this.optionalParamEnum != null) {
            paramTag = this.optionalParamEnum.toString();
        } else {
            paramTag = "" + this.tag;
        }

        if (this.valueType == INT_TYPE) {
            if (this.actualValueSize > 4) {
                throw (new SMPPException("The size (" + this.actualValueSize + " byte(s)) of the value for the parameter "
                        + paramTag + " is greater than the maximum allowed size for an integer type which is 4 bytes."));
            }

            if (this.minValueSize > 4) {
                throw (new SMPPException("The minimum size (" + this.minValueSize + " byte(s)) of the value for the parameter "
                        + paramTag + " is greater than the maximum allowed size for an integer type which is 4 bytes."));
            }

            if (this.length > 4) {
                throw (new SMPPException("The minimum size (" + this.length + " byte(s)) of the value for the parameter "
                        + paramTag + " is greater than the maximum allowed size for an integer type which is 4 bytes."));
            }
        }

        if (!this.isSizeVariable) {
            if (this.actualValueSize < this.length) {
                if (this.valueType == STRING_TYPE || this.valueType == OTHER) {
                    throw (new SMPPException("The size (" + this.actualValueSize + " byte(s)) of the value for the parameter "
                            + paramTag + " is smaller than the permitted size of (" + this.length + " byte(s))."));
                }
            } else if (this.actualValueSize > this.length) {
                throw (new SMPPException("The size (" + this.actualValueSize + " byte(s)) of the value for the parameter "
                        + paramTag + " is greater than the permitted size of (" + this.length + " byte(s))."));
            }
        } else {
            if (this.actualValueSize < this.minValueSize) {
                if (this.valueType == STRING_TYPE || this.valueType == OTHER) {
                    throw (new SMPPException("The size (" + this.actualValueSize + " byte(s)) of the value for the parameter "
                            + paramTag + " is smaller than the minimum size of (" + this.minValueSize + " byte(s))."));
                }
            } else if (this.actualValueSize > this.maxValueSize) {
                throw (new SMPPException("The size (" + this.actualValueSize + " byte(s)) of the value for the parameter "
                        + paramTag + " is greater than the maximum allowed size of (" + this.maxValueSize + " byte(s))."));
            }
        }

        return true;
    }
}
