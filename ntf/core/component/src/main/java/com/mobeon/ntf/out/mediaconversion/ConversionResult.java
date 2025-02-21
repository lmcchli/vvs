/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2010
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 7-Apr-2010
*/
package com.mobeon.ntf.out.mediaconversion;
import java.io.InputStream;

/**
 * ConversionResult holds the result of conversion, since it has two components,
 * the converted data and the length in milliseconds.
 */
public class ConversionResult {

    /**Result code: Conversion succeeded. */
    public static final int OK = 0;
    /**Result code: Conversion failed, cause unspecified. */
    public static final int OTHER_ERROR = 1;
    /**Result code: Conversion failed because the data was invalid. */
    public static final int INVALID_INPUT_DATA = 2;
    /**Result code: Conversion failed because the format is not supported. */
    public static final int UNSUPPORTED_INPUT_DATA = 3;
    /**Result code: Conversion failed due to lack of system resources, such as
     * disk or memory. */
    public static final int RESOURCE_PROBLEM = 4;

    public static final String OK_TEXT = "OK";

    /**
     * Constructor for failed conversion.
     *@param resultCode the machine version of the failure cause.
     *@param resultText the human readable error cause.
     */
    public ConversionResult(int resultCode,
                            String resultText) {
        this.resultCode = resultCode;
        this.resultText = resultText;
    }

    /**
     * Constructor for successful conversion.
     *@param contentType the type of the converted data.
     *@param is the data after conversion.
     *@param length the length of the data in milliseconds.
     */
    public ConversionResult(String contentType, InputStream is, int length, int conversionTime) {
        this.contentType = contentType;
        this.is = is;
        this.length = length;
        this.conversionTime = conversionTime;
    }

    private String contentType = null;
    private InputStream is = null;
    private int length = 0;
    private int conversionTime = 0;
    private int resultCode = 0;
    private String resultText = OK_TEXT;

    /**
     * Get the inputStream .
     *@return the inputStream
     */
    public InputStream getInputStream() { return is; }

    /**
     * Get the length .
     *@return the length
     */
    public int getLength() { return length; }

    /**
     * Get the contentType .
     *@return the contentType
     */
    public String getContentType() { return contentType; }

    /**
     * Get the resultCode .
     *@return the resultCode
     */
    public int getResultCode() { return resultCode; }

    /**
     * Get the resultText .
     *@return the resultText
     */
    public String getResultText() { return resultText; }

    /**
     * Get the conversionTime .
     *@return the conversionTime
     */
    public int getConversionTime() { return conversionTime; }
}
