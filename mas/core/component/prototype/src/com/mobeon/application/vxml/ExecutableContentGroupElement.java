package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 4:28:20 PM

    <xsd:group name="executable.content">
        <xsd:choice>
            <xsd:group ref="audio"/>
            <xsd:element ref="assign"/>
            <xsd:element ref="clear"/>
            <xsd:element ref="disconnect"/>
            <xsd:element ref="exit"/>
            <xsd:element ref="goto"/>
            <xsd:element ref="if"/>
            <xsd:element ref="log"/>
            <xsd:element ref="MAS_REPROMPT"/>
            <xsd:element ref="return"/>
            <xsd:element ref="script"/>
            <xsd:element ref="submit"/>
            <xsd:element ref="throw"/>
            <xsd:element ref="var"/>
            <xsd:element ref="prompt"/>
        </xsd:choice>
    </xsd:group>

 *
 */
public interface ExecutableContentGroupElement
{
    public static class Set
    {
        private ArrayList set = new ArrayList();

        public void add(ExecutableContentGroupElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public ExecutableContentGroupElement get(int index)
        {
            return (ExecutableContentGroupElement)set.get(index);
        }

        public int size()
        {
            return set.size();
        }                
    }
}
