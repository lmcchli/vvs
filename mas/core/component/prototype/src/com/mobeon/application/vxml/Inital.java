package com.mobeon.application.vxml;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:23:45 PM
 *
 <xsd:element name="initial">
     <xsd:complexType mixed="true">
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:group ref="audio"/>
             <xsd:group ref="event.handler"/>
             <xsd:element ref="link"/>
             <xsd:element ref="property"/>
             <xsd:element ref="prompt"/>
         </xsd:choice>
         <xsd:attributeGroup ref="Form-item.attribs"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Inital
        implements FormContentElement,
                   FormItemAttributedElement
{
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

    private ContentSet content = new ContentSet();

    public ContentSet getContent()
    {
        return content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public void add(InitalContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public InitalContentElement get(int index)
        {
            return (InitalContentElement)set.get(index);
        }

    }

}
