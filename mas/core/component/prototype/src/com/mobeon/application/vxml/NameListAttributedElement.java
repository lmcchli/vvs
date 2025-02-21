package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 10:01:44 PM
 *
 *  <xsd:attributeGroup name="Namelist.attrib">
        <xsd:annotation>
            <xsd:documentation>Atttibute for encoding content</xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="namelist" type="VariableNames.datatype"/>
    </xsd:attributeGroup>
 */
public interface NameListAttributedElement
{
    public NameListAttributedElement.List getNameList();

    public static class List
    {
        private ArrayList list = new ArrayList();

        public void add(String name)
        {
            list.add(name);
        }

        public int size()
        {
            return list.size();
        }

        public String get(int index)
        {
            return (String)list.get(index);
        }

        public ArrayList getList() {
            return list;
        }
    }
}
