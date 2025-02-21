package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 12:26:04 PM
 *
  <xsd:group name="input">
        <xsd:annotation>
            <xsd:documentation>input using adapted SRGS grammars</xsd:documentation>
        </xsd:annotation>

        <xsd:choice>
            <xsd:element name="grammar" type="mixed-grammar"/>
        </xsd:choice>

    </xsd:group>
 
 */
public interface InputGroup
        extends FieldContentElement,
                FormContentElement,
                RecordContentElement,
                TransferContentElement
{
    /// todo

    public static class Set
    {
        private ArrayList set = new ArrayList();
        public void add(InputGroup input)
        {
            if (!set.contains(input))
                set.add(input);
        }
        public int size()
        {
            return set.size();
        }
        public InputGroup get(int index)
        {
            return (InputGroup)set.get(index);
        }
    }

}
