/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.configuration.moip;

import com.abcxyz.services.moip.migration.configuration.moip.GroupImpl;
import com.abcxyz.services.moip.migration.configuration.moip.Utilities;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.ParameterTypeException;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.apache.commons.collections.MultiHashMap;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;

/**
 * Implements the IGroup interface.
 */
@SuppressWarnings({"EmptyCatchBlock"})
final class GroupImpl implements IGroup {
    private static final ILogger logger = ILoggerFactory.getILogger(IGroup.class);

    private final GroupImpl m_parent;
    private String m_name;
    private String m_text;
    private final MultiHashMap m_subGroups = new MultiHashMap();
    private final Map<String, String> m_parameters = new TreeMap<String, String>();

    public GroupImpl(Document doc) {
        m_parent = null;
        addConfiguration(doc);
    }

    private GroupImpl(GroupImpl parent) {
        m_parent = parent;
    }

    public void addConfiguration(Document doc) {
        Element root = doc.getRootElement();

        for (Object o : root.elements()) {
            Element element = (Element) o;
            GroupImpl group = new GroupImpl(this);
            group.addElements(element);
            m_subGroups.put(element.getName(), group);
        }
    }

    private void addElements(Element root) {
        m_name = root.getName();
        m_text = root.getTextTrim();
        for (Object o : root.attributes()) {
            Attribute attr = (Attribute) o;
            m_parameters.put(attr.getName(), attr.getValue());
        }

        for (Object o : root.elements()) {
            Element element = (Element) o;
            GroupImpl group = new GroupImpl(this);
            group.addElements(element);
            m_subGroups.put(element.getName(), group);
        }
    }

    public String getName() {
        return m_name;
    }

    private String _getFullName() {
        if (m_parent != null) {
            return m_parent._getFullName() + getName() + ".";
        }
        return "";
    }

    public String getFullName() {
        if (m_parent != null) {
            return m_parent._getFullName() + getName();
        }
        return "";
    }

    private String combineFullName(String child) {
        return _getFullName() + child;
    }

    public IGroup getGroup(String name) throws GroupCardinalityException, UnknownGroupException {
        if (name.length() == 0) {
            return this;
        }

        List<IGroup> groups = getGroups(Utilities.dotSplit(name));
        if (groups.size() > 1) {
            if (logger.isInfoEnabled())
                logger.info("Multiple groups with the same name found when expecting a single group: " + combineFullName(name));
            throw new GroupCardinalityException(combineFullName(name));
        }
        return groups.get(0);
    }

    public List<IGroup> getGroups(String name) throws UnknownGroupException {
        if (name.length() == 0) {
            List<IGroup> list = new LinkedList<IGroup>();
            list.add(this);
            return list;
        }

        return getGroups(Utilities.dotSplit(name));
    }

    public List<String> listGroups() {
        return new LinkedList<String>(m_subGroups.keySet());
    }

    public List<String> listParameters() {
        return new LinkedList<String>(m_parameters.keySet());
    }

    private List<IGroup> getGroups(LinkedList<String> nameList) throws UnknownGroupException {
        String key = nameList.poll();
        Collection<IGroup> subGroups = m_subGroups.getCollection(key);
        if (subGroups != null) {
            List<IGroup> retVal = new LinkedList<IGroup>();
            if (nameList.size() > 0) {
                for (IGroup group : subGroups) {
                    LinkedList<String> tmp = (LinkedList<String>) nameList.clone();
                    retVal.addAll(((GroupImpl) group).getGroups(tmp));
                }
            } else {
                retVal.addAll(subGroups);
            }
            return retVal;
        }
        if (logger.isInfoEnabled()) logger.info("Unable to find configuration group: " + combineFullName(key));
        throw new UnknownGroupException(key, this);
    }

    public int getInteger(String name) throws UnknownParameterException, ParameterTypeException {
        if (m_parameters.containsKey(name)) {
            String s = m_parameters.get(name);
            try {
                return new Integer(s);
            } catch (NumberFormatException e) {
                throw new ParameterTypeException("int");
            }
        }
        throw new UnknownParameterException(name, this);
    }

    public int getInteger(String name, int defaultValue) throws ParameterTypeException {
        try {
            return getInteger(name);
        } catch (UnknownParameterException e) {
            if (logger.isInfoEnabled())
                logger.info("Unable to find configuration parameter: " + e.getParameterName() + " using default: " + defaultValue);
        }

        return defaultValue;
    }

    public double getFloat(String name) throws UnknownParameterException, ParameterTypeException {
        if (m_parameters.containsKey(name)) {
            String s = m_parameters.get(name);
            try {
                return new Double(s);
            } catch (NumberFormatException e) {
                throw new ParameterTypeException("float");
            }
        }
        throw new UnknownParameterException(name, this);
    }

    public double getFloat(String name, double defaultValue) throws ParameterTypeException {
        try {
            return getFloat(name);
        } catch (UnknownParameterException e) {
            if (logger.isInfoEnabled())
                logger.info("Unable to find configuration parameter: " + e.getParameterName() + " using default: " + defaultValue);
        }
        return defaultValue;
    }

    public String getString(String name) throws UnknownParameterException {
        if (m_parameters.containsKey(name)) {
            return m_parameters.get(name);
        }
        throw new UnknownParameterException(name, this);
    }

    public String getString(String name, String defaultValue) {
        try {
            return getString(name);
        } catch (UnknownParameterException e) {
            if (logger.isInfoEnabled())
                logger.info("Unable to find configuration parameter: " + e.getParameterName() + " using default: " + defaultValue);
        }
        return defaultValue;
    }

    public boolean getBoolean(String name) throws UnknownParameterException, ParameterTypeException {
        if (m_parameters.containsKey(name)) {
            String value = m_parameters.get(name).toLowerCase();
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")) {
                return true;
            }
            if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")) {
                return false;
            }
            throw new ParameterTypeException("boolean");
        }
        throw new UnknownParameterException(name, this);
    }

    public boolean getBoolean(String name, boolean defaultValue) throws ParameterTypeException {
        try {
            return getBoolean(name);
        } catch (UnknownParameterException e) {
            if (logger.isInfoEnabled())
                logger.info("Unable to find configuration parameter: " + e.getParameterName() + " using default: " + defaultValue);
        }
        return defaultValue;
    }

    public String getText() {
        return m_text;
    }

    public String toString() {
        return "Group: " + getFullName();
    }

	@Override
	public ArrayList<String> getList(String listName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getTable(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableParameter(String tableName, String tableItemKey,
			String paramName) {
		// TODO Auto-generated method stub
		return null;
	}
}
