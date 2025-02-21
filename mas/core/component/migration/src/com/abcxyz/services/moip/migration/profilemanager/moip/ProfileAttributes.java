package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import java.util.*;

/**
 * Documentation
 *
 * @author mande
 */
public class ProfileAttributes {
    private static final ILogger LOG = ILoggerFactory.getILogger(ProfileAttributes.class);
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private Map<String, ProfileAttribute> data = new HashMap<String, ProfileAttribute>();
    private String distinguishedName;
    private BaseContext context;

    public ProfileAttributes(BaseContext context) {
        this.context = context;
    }

    public ProfileAttributes(BaseContext context, SearchResult searchResult) throws NamingException {
        this(context);
        parseSearchResult(searchResult);
    }

    public BaseContext getContext() {
        return context;
    }

    /**
     * Returns the distinguished name for the entry representing the ProfileAttributes
     *
     * @return a distinguished name or null if entry does not represent a UserRegister entry
     */
    public String getDistinguishedName() {
        return distinguishedName;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public Set<Map.Entry<String, ProfileAttribute>> entrySet() {
        return data.entrySet();
    }

    public ProfileAttribute get(String key) {
        return data.get(key);
    }

    public ProfileAttribute put(String key, ProfileAttribute value) {
        return data.put(key, value);
    }

    public ProfileAttribute remove(String key) {
        return data.remove(key);
    }

    /**
     * Parse search results
     *
     * @param searchResult the search result to parse
     * @throws NamingException
     */
    private void parseSearchResult(SearchResult searchResult) throws NamingException {
        // Todo: is this needed?
        // If no result is submitted, just return.
        if (searchResult == null) {
            if (LOG.isDebugEnabled()) LOG.debug("SearchResult is null");
            return;
        }
        // Todo: What happens when searchResult.isRelative is false?
        distinguishedName = searchResult.getNameInNamespace();
        NamingEnumeration<? extends Attribute> attributes = searchResult.getAttributes().getAll();
        while (attributes.hasMore()) {
            Attribute attribute = attributes.next();
            addUserRegisterAttribute(attribute);
        }
    }

    private void addUserRegisterAttribute(Attribute attribute) throws NamingException {
        // Since LDAP attributes are case insensitive: use lowercase
        String userRegisterName = attribute.getID().toLowerCase();
        // Only handle "known" attributes
        if (getContext().getConfig().getUserRegisterAttributeMap().containsKey(userRegisterName)) { // Todo: put this in context instead?
            if (LOG.isDebugEnabled()) LOG.debug(userRegisterName + " is in datamodel and will be added");
            List<String> valueList = new ArrayList<String>();
            NamingEnumeration<?> values = attribute.getAll();
            while (values.hasMore()) {
                Object value = values.next();
                if (value instanceof String) {
                    valueList.add((String) value);
                }
            }
            ProfileAttribute profileAttribute = new ProfileAttribute(valueList.toArray(EMPTY_STRING_ARRAY));
            data.put(userRegisterName, profileAttribute);
        } else {
            if (LOG.isDebugEnabled()) LOG.debug(userRegisterName + " is not in datamodel and will not be added");
        }
    }
}
