package com.mobeon.application.vxml.datatypes;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 2:52:28 PM\

 <xsd:simpleType name="Accept.datatype">
  <xsd:annotation>
   <xsd:documentation>exact or approximate</xsd:documentation>
  </xsd:annotation>
  <xsd:restriction base="xsd:NMTOKEN">
        <xsd:enumeration value="exact"/>
        <xsd:enumeration value="approximate"/>
  </xsd:restriction>
 </xsd:simpleType>

 */
public class Accept
{
    public static final Accept EXACT = new Accept("exact");
    public static final Accept APPROXIMATE = new Accept("approximate");

    private String accept;

    private Accept(String accept)
    {
        this.accept = accept;
    }

    public String toString()
    {
        return accept;
    }
}
