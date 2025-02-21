package com.mobeon.application.vxml.datatypes;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 4:49:12 PM
 *
 <xsd:simpleType name="Method.datatype">
 <xsd:annotation>
  <xsd:documentation>get or post</xsd:documentation>
 </xsd:annotation>
            <xsd:restriction base="xsd:NMTOKEN">
                <xsd:enumeration value="get"/>
                <xsd:enumeration value="post"/>
            </xsd:restriction>
        </xsd:simpleType>

 */
public class Method
{
    public static final Method GET = new Method("get");
    public static final Method POST = new Method("post");

    private String type;
    private Method(String type)
    {
        this.type = type;
    }
    public String toString()
    {
        return type;
    }
}
