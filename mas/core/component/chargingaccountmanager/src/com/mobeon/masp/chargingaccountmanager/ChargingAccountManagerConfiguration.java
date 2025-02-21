package com.mobeon.masp.chargingaccountmanager;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownParameterException;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that contains configuration variables for the Charging Account Manager.
 * <p/>
 * Date: 2007-nov-28
 *
 * @author emahagl
 */
public class ChargingAccountManagerConfiguration {
    private static final String CHARGINGACCOUNTMANAGER_GROUP = "chargingaccountmanager";
    private static final String ELEMENT_GROUP = "element";
    private static final String ELEMENTGROUP_GROUP = "elementgroup";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String PARENT_ATTR = "parent";
    private static final String STRUCTTYPE_ATTR = "structtype";
    private static final String MEMBER_ATTR = "member";

    private IConfiguration configuration;

    private static List<AirNode> airNodeList = new ArrayList<AirNode>();
    private static List<Element> elementList = new ArrayList<Element>();
    private static List<ElementGroup> elementGroupList = new ArrayList<ElementGroup>();

    private static ChargingAccountManagerConfiguration instance = new ChargingAccountManagerConfiguration();

    private ChargingAccountManagerConfiguration() {
    }

    /**
     * Retrieves the singleton instance of this class
     *
     * @return the singleton instance
     */
    static ChargingAccountManagerConfiguration getInstance() {
        return instance;
    }

    /**
     * Sets the configuration. This method should only be called once when the Provisoning Manager is initiated.
     *
     * @param config The configuration instance.
     * @throws IllegalArgumentException If <code>config</code> is <code>null</code>.
     */
    void setConfiguration(IConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Parameter config is null");
        }
        configuration = config;
    }

    /**
     * Reads configuration parameters.
     *
     * @throws com.mobeon.common.configuration.ConfigurationException
     *          if configuration could not be read.
     */
    void update() throws ConfigurationException {
        IGroup chargingGroup = configuration.getGroup(CHARGINGACCOUNTMANAGER_GROUP);

        loadAirNodeList(chargingGroup);
        loadElementList(chargingGroup);
        loadElementGroupList(chargingGroup);
    }

    /**
     * Retrieves the list of all configured <code>AirNode</code>
     *
     * @return a list of <code>AirNode</code>
     */
    public List<AirNode> getAirNodeList() {
        return airNodeList;
    }

    /**
     * Gets an Element specified by name.
     *
     * @param name
     * @return the Element, null if not found
     */
    public Element getElement(String name) {
        for (int i = 0; i < elementList.size(); i++) {
            Element e = elementList.get(i);
            if (e.getName().equals(name)) return e;
        }
        return null;
    }

    /**
     * Returns an <code>ElementGroup</code> from a name that is a member in the group.
     *
     * @param name
     * @return ElementGroup null if not found.
     */
    public ElementGroup getElementGroup(String name) {
        for (int i = 0; i < elementGroupList.size(); i++) {
            ElementGroup group = elementGroupList.get(i);
            Element[] elements = group.getMemberElements();
            for (int j = 0; j < elements.length; j++) {
                if (elements[j].getName().equals(name)) return group;
            }
        }
        return null;
    }

    private void loadAirNodeList(IGroup chargingGroup) throws ConfigurationException {
        IGroup airnodesGroup = chargingGroup.getGroup("airnodes");
        List<IGroup> groups = airnodesGroup.getGroups("node");
        for (IGroup nodeGroup : groups) {
            String host = nodeGroup.getString("host");
            int port = nodeGroup.getInteger("port");
            String userName = null;
            try {
                userName = nodeGroup.getString("username");
            } catch (UnknownParameterException e) {
                // ignore
            }
            String password = null;
            try {
                password = nodeGroup.getString("password");
            } catch (UnknownParameterException e) {
                // ignore
            }
            airNodeList.add(new AirNode(host, port, userName, password));
        }
    }

    private void loadElementList(IGroup chargingGroup) throws ConfigurationException {
        List<IGroup> groups = chargingGroup.getGroups(ELEMENT_GROUP);
        for (IGroup attrGroup : groups) {
            String name = attrGroup.getString(NAME_ATTR);
            String type = attrGroup.getString(TYPE_ATTR);
            Element element = new Element(name, type);
            elementList.add(element);
        }
    }

    private void loadElementGroupList(IGroup chargingGroup) throws ConfigurationException {
        List<IGroup> groups = chargingGroup.getGroups(ELEMENTGROUP_GROUP);
        for (IGroup elementGroup : groups) {

            ElementGroup eGroup = makeElementGroup(elementGroup);
            if (eGroup != null) elementGroupList.add(eGroup);
        }
    }

    private ElementGroup makeElementGroup(IGroup elementGroup) throws ConfigurationException {
        String parent = elementGroup.getString(PARENT_ATTR);

        String structtype = elementGroup.getString(STRUCTTYPE_ATTR);
        List<IGroup> memberGroups = elementGroup.getGroups(MEMBER_ATTR);
        Element[] members = new Element[memberGroups.size()];
        int i = 0;
        for (IGroup memberGroup : memberGroups) {
            String name = memberGroup.getString(NAME_ATTR);
            Element member = getElement(name);
            if (member == null) throw new ConfigurationException("Invalid member element " + name);
            members[i++] = member;
        }
        return new ElementGroup(parent, members, structtype);
    }
}
