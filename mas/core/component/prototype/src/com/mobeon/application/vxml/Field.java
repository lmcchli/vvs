package com.mobeon.application.vxml;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:17:19 PM
 *
 <xsd:element name="field">
     <xsd:complexType mixed="true">
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:group ref="audio"/>
             <xsd:group ref="event.handler"/>
             <xsd:element ref="filled"/>
             <xsd:element ref="link"/>
             <xsd:element ref="option"/>
             <xsd:element ref="property"/>
             <xsd:group ref="input"/>
             <xsd:element ref="prompt"/>
         </xsd:choice>
         <xsd:attributeGroup ref="Form-item.attribs"/>
         <xsd:attribute name="type" type="xsd:string"/>
         <xsd:attribute name="slot" type="xsd:NMTOKEN"/>
         <xsd:attribute name="modal" type="Boolean.datatype" default="false"/>
     </xsd:complexType>
 </xsd:element>

 */
public class Field
        implements VariableGroup,
                   FormItemAttributedElement
{
    private ContentSet content = new ContentSet();
    private FormContentElement nextSibling;

    public ContentSet getContent()
    {
        return content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public void add(FieldContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public FieldContentElement get(int index)
        {
            return (FieldContentElement)set.get(index);
        }
    }

    private String type;
    private String slot; // todo> something else than string?
    private boolean modal;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getSlot()
    {
        return slot;
    }

    public void setSlot(String slot)
    {
        this.slot = slot;
    }

    public boolean isModal()
    {
        return modal;
    }

    public void setModal(boolean modal)
    {
        this.modal = modal;
    }

    private String name;
    private Expression expression;
    private Cond cond;


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Expression getExpression()
    {
        return expression;
    }

    public void setExpression(Expression expression)
    {
        this.expression = expression;
    }

    public boolean isCond(ECMAExecutor exec)
    {
        return cond.isCond(exec);
    }

    public void setCond(Cond condition)
    {
   
        this.cond = condition;
    }

    public Cond getCond() {
        return cond;
    }

    public FormContentElement getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(FormContentElement nextSibling) {
        this.nextSibling = nextSibling;
    }
}
