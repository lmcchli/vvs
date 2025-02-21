package com.mobeon.application.vxml.grammar;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 8:32:26 PM
 */
public class Item
        implements RuleContent,
                   ItemContent
{
    private String bread; // todo: im not sure how to represent data.. so a string for now will do.

    public String getBread()
    {
        return bread;
    }

    public void setBread(String bread)
    {
        this.bread = bread;
    }

    private int[] repeat = new int[]{0, 1};
    private int[] probability = new int[]{0, 1};
    private int[] weights = new int[]{0, 1};

    public int[] getRepeat()
    {
        return repeat;
    }

    public int[] getProbability()
    {
        return probability;
    }

    public int[] getWeights()
    {
        return weights;
    }

    private Set content = new Set();

    public Set getContent()
    {
        return content;
    }

    public static class Set
    {
        public ArrayList list = new ArrayList();

        public ItemContent get(int index)
        {
            return (ItemContent)list.get(index);
        }

        public int size()
        {
            return list.size();
        }

        public void add(ItemContent member)
        {
            if (!list.contains(member))
                list.add(member);
        }
    }

}
