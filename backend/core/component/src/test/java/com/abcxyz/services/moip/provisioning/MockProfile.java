package com.abcxyz.services.moip.provisioning;


import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.oam.LogAgent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class MockProfile implements Profile
{
    private final LogAgent _logAgent;

    private final Map<String, List<String>> _attributes = new HashMap<String, List<String>>();

    private final List<URI> _identities = new ArrayList<URI>();


    public MockProfile(
            final LogAgent logAgent,
            final URI identity)
    {
        _logAgent = logAgent;
        _identities.add(identity);
    }


    public MockProfile(
            final LogAgent logAgent,
            final URI[] identities)
    {
        _logAgent = logAgent;
        //noinspection ManualArrayToCollectionCopy
        for (URI identity : identities)
        {
            _identities.add(identity);
        }
    }


    public URI[] getIdentities()
    {
        return _identities.toArray(new URI[_identities.size()]);
    }


    public URI[] getIdentities(final String scheme)
    {
        return _identities.toArray(new URI[_identities.size()]);
    }


    public boolean hasIdentity(final URI uri)
    {
        for (final URI identity : _identities)
        {
            if (identity.equals(uri)) return true;
        }
        return false;
    }


    public void removeAllIdentities()
    {
        _identities.clear();
    }

    /**
     * Removes the identity passed in as argument. This string is expected to parse as a URI.
     * @param anIdentityToRemove
     * @return true if the identity was removed, false if it was not removed because could not be found in identity list
     */
    public boolean removeSpecificIdentity(String anIdentityToRemove) throws URISyntaxException{
    	return removeSpecificIdentity(new URI(anIdentityToRemove)); 
    }

    /**
     * Removes the identity passed in as argument, a URI.
     * @param anIdentityToRemove
     * @return true if the identity was removed, false if it was not removed because could not be found in identity list
     */
    public boolean removeSpecificIdentity(URI uri){
    	
        for (final URI identity : _identities){
            if (identity.equals(uri)){
            	removeIdentity(uri);
            	return true;
            }
        }
    	return false;
    }
    
    
    public KeyValues[] getAttributes()
    {
        final KeyValues[] ret = new KeyValues[_attributes.size()];
        int i = 0;
        for (final Map.Entry<String, List<String>> entry : _attributes.entrySet())
        {
            final List<String> values = entry.getValue();
            ret[i++] = new KeyValues(entry.getKey(), values.toArray(new String[values.size()]));
        }
        return ret;
    }


    /**
     * Return the first value for an attribute. 
     * @param name of key to search
     * @return value as string, or null if not found in map
     */
    public String getAttributeValue(final String key)
    {
    	String value = null;
    	
    	List <String> values =_attributes.get(key);
    	if (values != null) {
    		value = values.get(0);
    	}
    	return value;
    }


    public List<String> getAttributeValues(final String key)
    {
        return _attributes.get(key);
    }


    public void addIdentity(final String uriString)
    {
        try
        {
            addIdentity(new URI(uriString));
        }
        catch (URISyntaxException e)
        {
            _logAgent.error("addIdentity", e);
        }
    }


    public void addIdentity(final URI uri)
    {
        _identities.add(uri);
    }


    public void addAttributeValue(final String attributeName,
                                  final String attributeValue)
    {
        List<String> attributeValues = _attributes.get(attributeName);
        if (attributeValues == null)
        {
            attributeValues = new ArrayList<String>();
            _attributes.put(attributeName, attributeValues);
        }
        attributeValues.add(attributeValue);
    }

    public void addAttributeNoValue(final String attributeName)
    {
        List<String> attributeValues = _attributes.get(attributeName);
        if (attributeValues == null)
        {
            attributeValues = new ArrayList<String>();
            _attributes.put(attributeName, attributeValues);
        }
    }

    public boolean hasAttribute(final String attributeName)
    {
        return _attributes.containsKey(attributeName);
    }


    public void removeAttribute(final String attributeName)
    {
        _attributes.remove(attributeName);
    }


    public void removeIdentity(final String identity)
    {
    	try{
    		removeIdentity(new URI(identity));
    	}
        catch (URISyntaxException e)
        {
            _logAgent.error("removeIdentity", e);
        }
   }
    
    public void removeIdentity(URI identity)
    {
        _identities.remove(identity);   
    }


    public int attributeSize(final String attributeName)
    {
        throw new RuntimeException("not expected");
    }


    public int attributesSize()
    {
        return _attributes.size();
    }


    public int identitySize()
    {
        return _identities.size();
    }


    public int size()
    {
        return attributesSize();
    }


    public void addExtraAttributesValue(final String attributeName,
                                        final String attributeValue)
    {
        throw new RuntimeException("not expected");
    }


    public String getExtraAttributesValue(final String attributeName)
    {
        throw new RuntimeException("not expected");
    }


    public Iterator<String> identitySchemeIterator()
    {
        throw new RuntimeException("not expected");
    }


    public Iterator<String> attributeIterator()
    {
    	Set attributeValues = _attributes.keySet();
    	
    	return attributeValues.iterator();
    }

    public Set<String> attributeKeySet() {
        return _attributes.keySet();
    }


    public void removeAllAttributes(){
    	_attributes.clear();       
    }

    @Override
    public String toLdifString() {
        return null;
    }

    @Override
    public void apply(List<Modification> aModificationList) {
    }

}

