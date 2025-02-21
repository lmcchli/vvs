package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 3:34:27 PM
 *
 	<xsd:complexType name="speak" mixed="true">
	    <xsd:group ref="speak.class"/>
	    <xsd:attributeGroup ref="speak.attribs"/>
	</xsd:complexType>

 */
public class Speak 
{
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    // todo
}
