/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.pool;

import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;

/**
 * Decorator for the <code>javax.naming.directory.DirContext</code> class
 *
 * @author ermmaha
 */
public class PooledDirContext implements DirContext {

    private DirContext context;
    private String providerUrl;
    /**
     * Time when this context was created
     */
    private long createTime = 0;

    /**
     * If you use constructor, use setContext before accessing any other
     * methods on this object.
     */
    PooledDirContext(String providerUrl) {
        this(null, providerUrl);
    }

    PooledDirContext(DirContext context, String providerUrl) {
        this.context = context;
        this.providerUrl = providerUrl;
        createTime = System.currentTimeMillis();
    }

    void setContext(DirContext context){
        this.context = context;
    }

    void release() throws NamingException {
        context.close();
    }

    long getSinceCreateTime() {
        return System.currentTimeMillis() - createTime;
    }

    public Attributes getAttributes(String name) throws NamingException {
        return context.getAttributes(name);
    }

    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
        return context.getAttributes(name, attrIds);
    }

    public Attributes getAttributes(Name name) throws NamingException {
        return context.getAttributes(name);
    }

    public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
        return context.getAttributes(name, attrIds);
    }

    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
        context.modifyAttributes(name, mod_op, attrs);
    }

    public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws NamingException {
        context.modifyAttributes(name, mod_op, attrs);
    }

    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        context.modifyAttributes(name, mods);
    }

    public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
        context.modifyAttributes(name, mods);
    }

    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
        context.bind(name, obj, attrs);
    }

    public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
        context.bind(name, obj, attrs);
    }

    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
        context.rebind(name, obj, attrs);
    }

    public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
        context.rebind(name, obj, attrs);
    }

    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        return context.createSubcontext(name, attrs);
    }

    public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
        return context.createSubcontext(name, attrs);
    }

    public DirContext getSchema(String name) throws NamingException {
        return context.getSchema(name);
    }

    public DirContext getSchema(Name name) throws NamingException {
        return context.getSchema(name);
    }

    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        return context.getSchemaClassDefinition(name);
    }

    public DirContext getSchemaClassDefinition(Name name) throws NamingException {
        return context.getSchemaClassDefinition(name);
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
        return context.search(name, matchingAttributes);
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
        return context.search(name, matchingAttributes);
    }

    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
        return context.search(name, matchingAttributes, attributesToReturn);
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
        return context.search(name, matchingAttributes, attributesToReturn);
    }

    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) throws NamingException {
        return context.search(name, filter, cons);
    }

    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons) throws NamingException {
        return context.search(name, filter, cons);
    }

    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons) throws NamingException {
        return context.search(name, filterExpr, filterArgs, cons);
    }

    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons) throws NamingException {
        return context.search(name, filterExpr, filterArgs, cons);
    }

    public Object lookup(String name) throws NamingException {
        return context.lookup(name);
    }

    public Object lookup(Name name) throws NamingException {
        return context.lookup(name);
    }

    public void bind(String name, Object obj) throws NamingException {
        context.bind(name, obj);
    }

    public void bind(Name name, Object obj) throws NamingException {
        context.bind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    public void unbind(String name) throws NamingException {
        context.unbind(name);
    }

    public void unbind(Name name) throws NamingException {
        context.unbind(name);
    }

    public void rename(String oldName, String newName) throws NamingException {
        context.rename(oldName, newName);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        context.rename(oldName, newName);
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return context.list(name);
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return context.list(name);
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return context.listBindings(name);
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return context.listBindings(name);
    }

    public void destroySubcontext(String name) throws NamingException {
        context.destroySubcontext(name);
    }

    public void destroySubcontext(Name name) throws NamingException {
        context.destroySubcontext(name);
    }

    public Context createSubcontext(String name) throws NamingException {
        return context.createSubcontext(name);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return context.createSubcontext(name);
    }

    public Object lookupLink(String name) throws NamingException {
        return context.lookupLink(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return context.lookupLink(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
        return context.getNameParser(name);
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return context.getNameParser(name);
    }

    public String composeName(String name, String prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return context.addToEnvironment(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return context.removeFromEnvironment(propName);
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return context.getEnvironment();
    }

    public void close() throws NamingException {
        DirContextPoolManager.getInstance().getDirContextPool(providerUrl).returnDirContext(this);
    }

    public String getNameInNamespace() throws NamingException {
        return context.getNameInNamespace();
    }
}
