package com.mobeon.application.vxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 2:55:23 AM
 *
 * todo: refactor. this should in fact be a inner class of the xmlbeans com.mobeon.recompiler.vxml export class.
 * This class makes sure that early bound NextAttributedElements get late bound to a NextElement instance.
 */
public class LateBinder
{
    public boolean validateIntegrity()
    {
        boolean ret = true;
        Iterator it = unresolvedPointers.values().iterator();
        while (it.hasNext())
        {
            ret = false;
            System.err.println("Still not found: "+ (String)it.next());
        }
        return ret;
    }


    private HashMap unresolvedPointers = new HashMap();
    private HashMap resolvedURIs = new HashMap();

    /**
     * Call this method to report a fully resolved NextAttributedElement
     * @param absoluteURI URIValidator of the instance NextElement
     * @param instance The fully resolved NextElement
     */
    public void reportURI(String absoluteURI, NextElement instance)
    {
        // update early bound pointers.
        Iterator it = unresolvedPointers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry)it.next();
            if (entry.getValue().equals(absoluteURI))
            {
                ((NextAttributedElement)entry.getKey()).setNext(instance);
                it.remove();
            }
        }

        resolvedURIs.put(absoluteURI, instance);
    }


    /**
     * Call this method as soon the URIValidator is set in a NextAttributedElement.
     *
     * @param reference
     * @param absoluteURI URIValidator of a NextElement
     */
    public NextAttributedElement reportPointer(NextAttributedElement reference, String absoluteURI)
    {
        NextElement instance = (NextElement)resolvedURIs.get(absoluteURI);
        if (instance != null)
            reference.setNext(instance);
        else
            unresolvedPointers.put(reference, absoluteURI);

        return reference;
    }
}
