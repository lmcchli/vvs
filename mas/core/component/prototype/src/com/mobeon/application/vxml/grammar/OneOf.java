package com.mobeon.application.vxml.grammar;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 8:32:13 PM
 */
public class OneOf
                implements RuleContent
{
    private ItemsSet items = new ItemsSet();

    public ItemsSet getItems()
    {
        return items;
    }

    public static class ItemsSet
    {
        public ArrayList list = new ArrayList();

        public Item get(int index)
        {
            return (Item)list.get(index);
        }

        public int size()
        {
            return list.size();
        }

        public void add(Item member)
        {
            if (!list.contains(member))
                list.add(member);
        }
    }
}
