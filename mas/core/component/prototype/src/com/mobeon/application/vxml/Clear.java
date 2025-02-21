package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:14:52 PM
 *
 *     <xsd:element name="clear">
        <xsd:complexType>
            <xsd:attributeGroup ref="Namelist.attrib"/>
        </xsd:complexType>
    </xsd:element>

 *
 *
 */
public class Clear
        implements ExecutableContentGroupElement,
                   NameListAttributedElement
{
    private NameListAttributedElement.List nameList = new NameListAttributedElement.List();

    public NameListAttributedElement.List getNameList()
    {
        return nameList;
    }
}
