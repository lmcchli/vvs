package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:28:57 PM
 *
 <xsd:element name="property">
     <xsd:complexType>
         <xsd:attribute name="name" type="xsd:NMTOKEN" use="required"/>
         <xsd:attribute name="value" type="xsd:string" use="required"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Property
        implements FieldContentElement,
                   FormContentElement,
                   InitalContentElement,
                   MenuContentElement,
                   ObjectContentElement,
                   RecordContentElement,
                   SubDialogContentElement,
                   TransferContentElement,
                   VXMLContentElement
{
    private String name;
    private String value;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
