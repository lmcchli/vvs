package com.mobeon.application.vxml;

import java.util.ArrayList;
import java.util.List;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:14:04 PM
 *
 *     <xsd:element name="catch">
        <xsd:complexType>
            <xsd:complexContent mixed="true">
                <xsd:extension base="basic.event.handler">
                   <xsd:attribute name="event" type="EventNames.datatype"/>
                </xsd:extension>
            </xsd:complexContent>
        </xsd:complexType>
    </xsd:element>

 *
 */
public class Catch
        extends BasicEventHandler
        implements EventHandlerGroup, VXMLContentElement
{
    private List events = new ArrayList();

    public List getEvents()
    {
        return events;
    }

    public void setEvents(List event)
    {
        this.events = event;
    }
}
