package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 4:51:20 PM

 <xsd:attributeGroup name="Enctype.attrib">
     <xsd:annotation>
         <xsd:documentation>Atttibute for content encoding</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="enctype" type="ContentType.datatype"/>
 </xsd:attributeGroup>

 <xsd:simpleType name="ContentType.datatype">
        <xsd:annotation>
            <xsd:documentation>Content type [RFC2045]</xsd:documentation>
        </xsd:annotation>
        <xsd:list itemType="xsd:token"/>
    </xsd:simpleType>

 */
public interface EncodeTypeAttributedElement
{
    // todo is this a list rather than a string?

    public String getEncodingType();
    public void setEncodingType(String encodingType);
}
