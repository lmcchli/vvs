package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 10:06:28 PM
 */
public interface InputElement
{
     public static class Set
    {
        public ArrayList list = new ArrayList();

        public InputElement get(int index)
        {
            return (InputElement)list.get(index);
        }

        public int size()
        {
            return list.size();
        }

        public void add(InputElement member)
        {
            if (!list.contains(member))
                list.add(member);
        }
    }
}
