package com.mobeon.application.vxml.grammar;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 8:32:03 PM
 */
public class Rule
{
    public Set content = new Set();

    public Set getContent()
    {
        return content;
    }

    public void setContent(Set content)
    {
        this.content = content;
    }

    /**
     * The id attribute specifies the rulename of the grammar.
     * Rulenames must be unique within the file itself, and may not contain hyphens, colons, or periods.
     */
    private String id;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * The scope attribute denotes whether or not the XML grammar is a private (local) rule,
     * or if it is a public rule used for an external rule reference.
     */
    private boolean _public;

    public boolean isPublic()
    {
        return _public;
    }
    
    public void setPublic(boolean _public)
    {
        this._public = _public;
    }

    public static class Set
    {
        public ArrayList list = new ArrayList();

        public RuleContent get(int index)
        {
            return (RuleContent)list.get(index);
        }

        public int size()
        {
            return list.size();
        }

        public void add(RuleContent member)
        {
            if (!list.contains(member))
                list.add(member);
        }
    }
}
