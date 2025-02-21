package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Accept;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:24:55 PM

 <xsd:element name="menu">
     <xsd:complexType mixed="true">
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:group ref="audio"/>
             <xsd:element ref="choice"/>
             <xsd:group ref="event.handler"/>
             <xsd:element ref="property"/>
             <xsd:element ref="prompt"/>
         </xsd:choice>
         <xsd:attribute name="id" type="xsd:ID"/>
         <xsd:attributeGroup ref="GrammarScope.attrib"/>
         <xsd:attributeGroup ref="Accept.attrib"/>
         <xsd:attribute name="dtmf" type="Boolean.datatype" default="false"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Menu
        implements GrammarScopeAttributedElement,
                   AcceptAttributedElement,
                   VXMLContentElement
{
    private Scope scope;
    private String id;

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public boolean isDtmf()
    {
        return dtmf;
    }

    public void setDtmf(boolean dtmf)
    {
        this.dtmf = dtmf;
    }

    public Accept getAccept()
    {
        return accept;
    }

    public void setAccept(Accept accept)
    {
        this.accept = accept;
    }

    private boolean dtmf;
    private Accept accept = Accept.EXACT;



    private ContentSet content = new ContentSet();

    public ContentSet getContent()
    {
        return content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public void add(MenuContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public MenuContentElement get(int index)
        {
            return (MenuContentElement)set.get(index);
        }
    }
}
