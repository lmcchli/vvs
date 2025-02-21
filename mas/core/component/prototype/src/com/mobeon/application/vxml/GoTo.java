package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:18:23 PM

 <xsd:element name="goto">
     <xsd:complexType>
         <xsd:attributeGroup ref="Cache.attribs"/>
         <xsd:attributeGroup ref="Next.attribs"/>
         <xsd:attribute name="fetchaudio" type="URIValidator.datatype"/>
         <xsd:attribute name="expritem" type="Script.datatype"/>
         <xsd:attribute name="nextitem" type="RestrictedVariableName.datatype"/>
     </xsd:complexType>
 </xsd:element>

 */
public class GoTo
        implements ExecutableContentGroupElement,
                   CacheAttributedElement
{
    private String fetchHint;
    private Duration fetchTimeOut;

    private int maxAge;
    private int maxStale;
    private String fetchAudio;
    private Expression expr;


    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getFetchHint()
    {
        return fetchHint;
    }

    public void setFetchHint(String fetchHint)
    {
        this.fetchHint = fetchHint;
    }

    public Duration getFetchTimeOut()
    {
        return fetchTimeOut;
    }

    public void setFetchTimeOut(Duration fetchTimeOut)
    {
        this.fetchTimeOut = fetchTimeOut;
    }

    public int getMaxAge()
    {
        return maxAge;
    }

    public void setMaxAge(int maxAge)
    {
        this.maxAge = maxAge;
    }

    public int getMaxStale()
    {
        return maxStale;
    }

    public void setMaxStale(int maxStale)
    {
        this.maxStale = maxStale;
    }

    public String getFetchAudio()
    {
        return fetchAudio;
    }

    public void setFetchAudio(String fetchAudio)
    {
        this.fetchAudio = fetchAudio;
    }


    private String next;

    public String getNext()
    {
        return next;
    }

    public void setNext(String next)
    {
        this.next = next;
    }

    /*
    private NextElement next;
    private FormItemAttributedElement nextItem;
    private ECMAScriptInvoker expressionItem;

    public NextElement getNext()
    {
        return next;
    }

    public void setNext(NextElement next)
    {
        this.next = next;
    }

        public FormItemAttributedElement getNextItem()
    {
        return nextItem;
    }

    public void setNextItem(FormItemAttributedElement nextItem)
    {
        this.nextItem = nextItem;
    }

    public ECMAScriptInvoker getExpressionItem()
    {
        return expressionItem;
    }

    public void setExpressionItem(ECMAScriptInvoker expressionItem)
    {
        this.expressionItem = expressionItem;
    }

    */


}
