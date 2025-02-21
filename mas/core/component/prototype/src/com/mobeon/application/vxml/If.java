package com.mobeon.application.vxml;

import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:20:45 PM
 *
    <xsd:element name="if">
        <xsd:complexType mixed="true">
            <xsd:sequence>
                <xsd:group ref="executable.content" minOccurs="0" maxOccurs="unbounded"/>
                <xsd:sequence minOccurs="0" maxOccurs="unbounded">
                    <xsd:element ref="elseif"/>
                    <xsd:group ref="executable.content" minOccurs="0" maxOccurs="unbounded"/>
                </xsd:sequence>
                <xsd:sequence minOccurs="0" maxOccurs="1">
                    <xsd:element ref="else"/>
                    <xsd:group ref="executable.content" minOccurs="0" maxOccurs="unbounded"/>
                </xsd:sequence>
            </xsd:sequence>
            <xsd:attributeGroup ref="If.attribs"/>
        </xsd:complexType>
    </xsd:element>

 */
public class If
        implements ExecutableContentGroupElement,
                   IfAttributedElement
{
    private Cond  condition;



    private ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();

    public Cond getCondition()
    {
        return condition;
    }

    public void setCondition(Cond condition)
    {
        this.condition = condition;
    }

    private ElseIfSet elseIf = new ElseIfSet();
    private Else _else;
    private ExecutableContentGroupElement.Set elseExecutableContent = new ExecutableContentGroupElement.Set();

    public Set getExecutableContent()
    {
        return executableContent;
    }

    public ElseIfSet getElseIf()
    {
        return elseIf;
    }

    public Else getElse()
    {
        return _else;
    }

    public Set getElseExecutableContent()
    {
        return elseExecutableContent;
    }
   
    public void setElse(Else _else)
    {
        this._else = _else;
    }

    public class ElseIfSet
    {
        private ArrayList set = new ArrayList();

        public void add(Entry member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public Entry get(int index)
        {
            return (Entry)set.get(index);
        }

        public class Entry
        {
            public ElseIf getElseif()
            {
                return elseif;
            }

            public void setElseif(ElseIf elseif)
            {
                this.elseif = elseif;
            }

            public Set getExecutableContent()
            {
                return executableContent;
            }

            private ElseIf elseif;
            private ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();
        }
    }
}
