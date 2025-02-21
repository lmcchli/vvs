package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:32:22 PM
 *
 <xsd:element name="transfer">
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
         <xsd:attribute name="dest" type="URIValidator.datatype"/>
         <xsd:attribute name="destexpr" type="Script.datatype"/>
         <xsd:attribute name="bridge" type="Boolean.datatype" default="false"/>
         <xsd:attribute name="connecttimeout" type="Duration.datatype"/>
         <xsd:attribute name="maxtime" type="Duration.datatype"/>
         <xsd:attribute name="transferaudio" type="URIValidator.datatype"/>
         <xsd:attribute name="aai" type="xsd:string"/>
         <xsd:attribute name="aaiexpr" type="Script.datatype"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Transfer
        implements FormContentElement,
                   FormItemAttributedElement
{
    private ContentSet content = new ContentSet();
    public ContentSet getContent()
    {
        return content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();
        public void add(TransferContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }
        public int size()
        {
            return set.size();
        }
        public TransferContentElement get(int index)
        {
            return (TransferContentElement)set.get(index);
        }
    }

    private String destination;
    private String destinationExpression;
    private boolean bridge;
    private Duration connectTimeOut;
    private Duration maxTime;
    private String transferAudio;
    private String aai;
    private String aaiExpression;

    private String name;
    private Expression expression;
    private Cond cond;

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getDestinationExpression()
    {
        return destinationExpression;
    }

    public void setDestinationExpression(String destinationExpression)
    {
        this.destinationExpression = destinationExpression;
    }

    public boolean isBridge()
    {
        return bridge;
    }

    public void setBridge(boolean bridge)
    {
        this.bridge = bridge;
    }

    public Duration getConnectTimeOut()
    {
        return connectTimeOut;
    }

    public void setConnectTimeOut(Duration connectTimeOut)
    {
        this.connectTimeOut = connectTimeOut;
    }

    public Duration getMaxTime()
    {
        return maxTime;
    }

    public void setMaxTime(Duration maxTime)
    {
        this.maxTime = maxTime;
    }

    public String getTransferAudio()
    {
        return transferAudio;
    }

    public void setTransferAudio(String transferAudio)
    {
        this.transferAudio = transferAudio;
    }

    public String getAai()
    {
        return aai;
    }

    public void setAai(String aai)
    {
        this.aai = aai;
    }

    public String getAaiExpression()
    {
        return aaiExpression;
    }

    public void setAaiExpression(String aaiExpression)
    {
        this.aaiExpression = aaiExpression;
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
}
