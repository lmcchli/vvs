package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:25:19 PM
 *
 <xsd:element name="meta">
     <xsd:complexType>
         <xsd:attribute name="name" type="xsd:NMTOKEN"/>
         <xsd:attribute name="content" type="xsd:string" use="required"/>
         <xsd:attribute name="http-equiv" type="xsd:NMTOKEN"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Meta
        implements VXMLContentElement
{
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getHttpEquivelent()
    {
        return httpEquivelent;
    }

    public void setHttpEquivelent(String httpEquivelent)
    {
        this.httpEquivelent = httpEquivelent;
    }

    private String name;
    private String content;
    private String httpEquivelent;


}
