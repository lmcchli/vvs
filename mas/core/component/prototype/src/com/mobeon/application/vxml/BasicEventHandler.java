package com.mobeon.application.vxml;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.util.Cond;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:45:04 PM
 *
 <xsd:complexType name="basic.event.handler" mixed="true">
     <xsd:group ref="executable.content" minOccurs="0" maxOccurs="unbounded"/>
     <xsd:attributeGroup ref="EventHandler.attribs"/>
 </xsd:complexType>

 */
public abstract class BasicEventHandler
        implements EventHandlerAttributedElement
{
    private ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();

    public ExecutableContentGroupElement.Set getExecutableContent()
    {
        return executableContent;
    }

    private int count;
    private Cond cond;




    

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public boolean isCond(ECMAExecutor exec)
    {
        return cond.isCond(exec);
    }

    public void setCond(Cond condition)
    {
        this.cond = condition;
    }
}
