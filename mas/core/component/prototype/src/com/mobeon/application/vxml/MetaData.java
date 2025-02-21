package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:25:42 PM
 *
 <xsd:element name="metadata">
     <xsd:complexType>
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:any namespace="##other" processContents="lax"/>
         </xsd:choice>
         <xsd:anyAttribute namespace="##any" processContents="strict"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class MetaData
        implements VXMLContentElement
{
    // todo
}
