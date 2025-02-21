package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Accept;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:27:37 PM
 *
 <xsd:element name="option">
     <xsd:complexType mixed="true">
         <xsd:attributeGroup ref="Accept.attrib"/>
         <xsd:attribute name="dtmf" type="DTMFSequence.datatype"/>
         <xsd:attribute name="value" type="xsd:string"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Option
        implements FieldContentElement,
                   FormContentElement,
                   AcceptAttributedElement
{
    private String dtmf;
    private String value;

    private Accept accept = Accept.EXACT;

    public String getDtmf()
    {
        return dtmf;
    }

    public void setDtmf(String dtmf)
    {
        this.dtmf = dtmf;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Accept getAccept()
    {
        return accept;
    }

    public void setAccept(Accept accept)
    {
        this.accept = accept;
    }

}
