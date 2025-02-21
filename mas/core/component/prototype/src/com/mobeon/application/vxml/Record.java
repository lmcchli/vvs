package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:29:25 PM
 *
 <xsd:element name="record">
     <xsd:complexType mixed="true">
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:group ref="audio"/>
             <xsd:group ref="event.handler"/>
             <xsd:element ref="filled"/>
             <xsd:element ref="property"/>
             <xsd:group ref="input"/>
             <xsd:element ref="prompt"/>
         </xsd:choice>
         <xsd:attributeGroup ref="Form-item.attribs"/>
         <xsd:attribute name="type" type="ContentType.datatype"/>
         <xsd:attribute name="beep" type="Boolean.datatype" default="false"/>
         <xsd:attribute name="maxtime" type="Duration.datatype"/>
         <xsd:attribute name="modal" type="Boolean.datatype" default="true"/>
         <xsd:attribute name="finalsilence" type="Duration.datatype"/>
         <xsd:attribute name="dtmfterm" type="Boolean.datatype" default="true"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Record
        implements FormContentElement,
                   FormItemAttributedElement
{
    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public void add(RecordContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public RecordContentElement get(int index)
        {
            return (RecordContentElement)set.get(index);
        }

    }

    private String name;
    private Expression expression;
    private Cond cond;

    private List type;
    private boolean beep = false;
    private Duration maxTime;
    private boolean modal = true;
    private Duration finalSilence;
    private boolean dtmfTerm = true;
    private ContentSet content = new ContentSet();
    private FormContentElement nextSibling;


     public ContentSet getContent()
    {
        return content;
    }

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

    public List getType()
    {
        return type;
    }

    public void setType(List type)
    {
        this.type = type;
    }

    public boolean isBeep()
    {
        return beep;
    }

    public void setBeep(boolean beep)
    {
        this.beep = beep;
    }

    public Duration getMaxTime()
    {
        return maxTime;
    }

    public void setMaxTime(Duration maxTime)
    {
        this.maxTime = maxTime;
    }

    public boolean isModal()
    {
        return modal;
    }

    public void setModal(boolean modal)
    {
        this.modal = modal;
    }

    public Duration getFinalSilence()
    {
        return finalSilence;
    }

    public void setFinalSilence(Duration finalSilence)
    {
        this.finalSilence = finalSilence;
    }

    public boolean isDtmfTerm()
    {
        return dtmfTerm;
    }

    public void setDtmfTerm(boolean dtmfTerm)
    {
        this.dtmfTerm = dtmfTerm;
    }

    public FormContentElement getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(FormContentElement nextSibling) {
        this.nextSibling = nextSibling;
    }
}
