package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:17:59 PM
 *
 <xsd:element name="form">
     <xsd:complexType>
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:group ref="event.handler"/>
             <xsd:element ref="filled"/>
             <xsd:element ref="initial"/>
             <xsd:element ref="object"/>
             <xsd:element ref="link"/>
             <xsd:element ref="property"/>
             <xsd:element ref="record"/>
             <xsd:element ref="script"/>
             <xsd:element ref="subdialog"/>
             <xsd:element ref="transfer"/>
             <xsd:group ref="variable"/>
             <xsd:group ref="input"/>
         </xsd:choice>
         <xsd:attribute name="id" type="xsd:ID"/>
         <xsd:attributeGroup ref="GrammarScope.attrib"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Form
        implements GrammarScopeAttributedElement,
                   VXMLContentElement
{
    private GrammarScopeAttributedElement.Scope scope;


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

    private String id; // todo> other than String?



    private ContentSet content = new ContentSet();

    public ContentSet getContent()
    {
        return  content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public int size()
        {
            return set.size();
        }

        public void add(FormContentElement member)
        {
            set.add(member);
        }

        public FormContentElement get(int index)
        {
            return (FormContentElement)set.get(index);
        }
    }
}
