package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 4:46:13 PM
 * 
 <xsd:attributeGroup name="Submit.attribs">
        <xsd:annotation>
            <xsd:documentation>Atttibutes for submit element (5.3.8)</xsd:documentation>
        </xsd:annotation>
        <xsd:attributeGroup ref="Method.attrib"/>
        <xsd:attributeGroup ref="Enctype.attrib"/>
        <xsd:attributeGroup ref="Namelist.attrib"/>
    </xsd:attributeGroup>
 */
public interface SubmitAttributedElement
        extends MethodAttributedElement,
                EncodeTypeAttributedElement,
                NameListAttributedElement
{
}
