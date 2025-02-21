package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 12:15:51 PM
 *
 <xsd:attributeGroup name="RestrictedNamelist.attrib">
     <xsd:annotation>
         <xsd:documentation>Atttibute for encoding content</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="namelist" type="RestrictedVariableNames.datatype"/>
 </xsd:attributeGroup>

 */
public interface RestrictedNameListAttributedElement
{
    public RestrictedNameListAttributedElement.List getNameList();

    public static class List
    {
        private ArrayList list = new ArrayList();

        public void add (String restrictedName)
        {
            list.add(restrictedName);
        }

        public int size()
        {
            return list.size();
        }

        public String get(int index)
        {
            return (String)list.get(index);
        }
    }

}
