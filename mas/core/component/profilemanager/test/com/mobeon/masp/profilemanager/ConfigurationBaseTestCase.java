package com.mobeon.masp.profilemanager;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownParameterException;
import org.jmock.Mock;

import java.util.List;
import java.util.ArrayList;

/**
 * Documentation
 *
 * @author mande
 */
public abstract class ConfigurationBaseTestCase extends ProfileManagerMockObjectBaseTestCase {
    protected static final int READ_TIMEOUT = 3000;
    protected static final int WRITE_TIMEOUT = 4000;
    private static final int DEFAULT_TIMEOUT = 5000;
    protected static final String LIMIT_SCOPE_STRING = "true";
    protected static final String DEFAULT_LIMIT_SCOPE_STRING = "false";
    protected static final int COSCACHE_TIMEOUT = 600000;
    protected static final int DEFAULT_COSCACHE_TIMEOUT = 300000;
    private static final String SEARCHORDER_STRING = "community,cos,billing,user";
    private static final String DEFAULT_SEARCHORDER_STRING = "community,cos,user,billing";
    private static final int MAX_POOL_SIZE = 25;
    private static final int CONNECTION_LIFETIME = 300000;

    public ConfigurationBaseTestCase(String string) {
        super(string);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected IConfiguration getConfigurationProfileManagerGroupMissing() {
        Mock mockConfiguration = mock(IConfiguration.class);
        mockConfiguration.expects(once()).method("getGroup").with(eq("profilemanager")).
                will(throwException(new UnknownGroupException("profilemanager", getParentGroup())));
        return (IConfiguration)mockConfiguration.proxy();
    }

    protected IConfiguration getMockConfiguration() {
        Mock mockProfileManagerGroup = mock(IGroup.class, "mockProfileManagerGroup");
        addProfileManagerGroupAttributes(mockProfileManagerGroup);
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("userregister")).
                will(returnValue(getMockUserRegisterGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("connectionpool")).
                will(returnValue(getMockConnectionPoolGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("provisioning")).
                will(returnValue(getMockProvisioningGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("attributemap")).
                will(returnValue(getMockAttributeMapGroup()));
        Mock mockConfiguration = mock(IConfiguration.class);
        mockConfiguration.expects(once()).method("getGroup").with(eq("profilemanager")).
                will(returnValue(mockProfileManagerGroup.proxy()));
        return (IConfiguration)mockConfiguration.proxy();
    }

    protected void addProfileManagerGroupAttributes(Mock mockConfiguration) {
        mockConfiguration.expects(once()).method("getString").with(eq("limitscope"), eq(DEFAULT_LIMIT_SCOPE_STRING)).
                will(returnValue(LIMIT_SCOPE_STRING));
        mockConfiguration.expects(once()).method("getInteger").with(eq("coscachetimeout"), eq(DEFAULT_COSCACHE_TIMEOUT)).
                will(returnValue(COSCACHE_TIMEOUT));
    }

    protected IGroup getMockUserRegisterGroup() {
        Mock mockUserRegisterGroup = mock(IGroup.class, "mockUserRegisterGroup");
        mockUserRegisterGroup.expects(once()).method("getInteger").with(eq("readtimeout"), eq(DEFAULT_TIMEOUT)).
                will(returnValue(READ_TIMEOUT));
        mockUserRegisterGroup.expects(once()).method("getInteger").with(eq("writetimeout"), eq(DEFAULT_TIMEOUT)).
                will(returnValue(WRITE_TIMEOUT));
        mockUserRegisterGroup.expects(once()).method("getString").with(eq("defaultsearchbase"), eq("o=mobeon.com")).
                will(returnValue("o=mobeon.com"));
        mockUserRegisterGroup.expects(once()).method("getString").with(eq("admin"), eq("cn=Directory Manager")).
                will(returnValue("cn=Directory Manager"));
        mockUserRegisterGroup.expects(once()).method("getString").with(eq("password"), eq("emmanager")).
                will(returnValue("emmanager"));
        mockUserRegisterGroup.expects(once()).method("getInteger").with(eq("trylimit"), eq(BaseConfigTest.TRY_LIMIT)).
                will(returnValue(BaseConfigTest.TRY_LIMIT));
        mockUserRegisterGroup.expects(once()).method("getInteger").with(eq("trytimelimit"), eq(BaseConfigTest.TRY_TIME_LIMIT)).
                will(returnValue(BaseConfigTest.TRY_TIME_LIMIT));
        return (IGroup)mockUserRegisterGroup.proxy();
    }

    protected IGroup getMockConnectionPoolGroup() {
        Mock mockConnectionPoolGroup = mock(IGroup.class, "mockConnectionPoolGroup");
        mockConnectionPoolGroup.expects(once()).method("getInteger").with(eq("maxsize"), eq(MAX_POOL_SIZE)).
                will(returnValue(MAX_POOL_SIZE));
        mockConnectionPoolGroup.expects(once()).method("getInteger").with(eq("connectionlifetime"), eq(CONNECTION_LIFETIME)).
                will(returnValue(CONNECTION_LIFETIME));
        return (IGroup)mockConnectionPoolGroup.proxy();
    }

    protected IGroup getMockProvisioningGroup() {
        Mock mockProvisioningGroup = mock(IGroup.class, "mockProvisioningGroup");
        mockProvisioningGroup.expects(once()).method("getString").with(eq("password"), eq("secret")).
                will(returnValue("secret"));
        return (IGroup)mockProvisioningGroup.proxy();
    }

    private IGroup getMockAttributeMapGroup() {
        Mock mockAttributeMapGroup = mock(IGroup.class, "mockAttributeMapGroup");
        mockAttributeMapGroup.expects(once()).method("getString").
                with(eq("searchorder"), eq(DEFAULT_SEARCHORDER_STRING)).
                will(returnValue(SEARCHORDER_STRING));
        addAttributeGroups(mockAttributeMapGroup);
        return (IGroup)mockAttributeMapGroup.proxy();
    }

    private void addAttributeGroups(Mock mockAttributeMapGroup) {
        List<String> groups = new ArrayList<String>();
        addAttributeGroup("applicationName", "userRegisterName", "string", "user", null, null, "provisioningName", mockAttributeMapGroup, groups);
        addAttributeGroup("applicationName2", "userRegisterName", "string", "user", null, null, null, mockAttributeMapGroup, groups);
        addAttributeGroup("applicationName3", null, null, null, null, null, "provisioningName", mockAttributeMapGroup, groups);
        addAttributeGroupProvisioningUnknownParameterException("applicationName4", "provisioningName", mockAttributeMapGroup, groups);
        addAttributeGroupUserRegisterUnknownParameterException("applicationName5", "userRegisterName", mockAttributeMapGroup, groups);
        addAttributeGroupUnknownGroupException("unknownattributegroup", mockAttributeMapGroup, groups);
        mockAttributeMapGroup.expects(once()).method("listGroups").will(returnValue(groups));
    }

    private void addAttributeGroup(String groupName, String userRegisterName, String type, String level,
                                   String defaultValue, String searchOrder, String provisioningName,
                                   Mock mockAttributeMapGroup, List<String> groups) {

        mockAttributeMapGroup.expects(once()).method("getGroup").with(eq(groupName)).
                will(returnValue(getMockAttributeGroup(
                        groupName, userRegisterName, type, level, defaultValue, searchOrder, provisioningName
                ).proxy()));
        groups.add(groupName);
    }

    private void addAttributeGroupUnknownGroupException(String groupName, Mock mockAttributeMapGroup, List<String> groups) {
        mockAttributeMapGroup.expects(once()).method("getGroup").with(eq(groupName)).
                will(throwException(getUnknownGroupException(groupName)));
        groups.add(groupName);
    }

    private void addAttributeGroupProvisioningUnknownParameterException(String groupName, String provisioningName,
                                                                        Mock mockAttributeMapGroup, List<String> groups)
    {
        mockAttributeMapGroup.expects(once()).method("getGroup").with(eq(groupName)).
                will(returnValue(getMockAttributeGroupProvisioningUnknownParameterException(
                        groupName, provisioningName
                )));
        groups.add(groupName);
    }

    private void addAttributeGroupUserRegisterUnknownParameterException(String groupName, String userRegisterName,
                                                                        Mock mockAttributeMapGroup, List<String> groups)
    {
        mockAttributeMapGroup.expects(once()).method("getGroup").with(eq(groupName)).
                will(returnValue(getMockAttributeGroupUserRegisterUnknownParameterException(
                        groupName, userRegisterName
                )));
        groups.add(groupName);
    }

    protected Mock getMockAttributeGroup(String applicationName, String userRegisterName, String type, String level, String defaultValue, String searchOrder, String provisioningName) {
        Mock mockIGroup;
        // BaseConfig does some additional calls to the attribute group
        if (userRegisterName != null) {
            mockIGroup = getMockAttributeGroup(applicationName, userRegisterName, type, level, defaultValue, searchOrder);
            mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                    will(returnValue(userRegisterName));
        } else {
            mockIGroup = mock(IGroup.class, "mock" + capitalize(applicationName) + "Group");
            mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                    will(throwException(new UnknownParameterException(CFG_USER_REGISTER_NAME, getParentGroup())));
        }
        if (provisioningName != null) {
            mockIGroup.expects(once()).method("getName").will(returnValue(applicationName));
            mockIGroup.expects(once()).method("getString").with(eq(CFG_PROVISIONINGNAME)).
                    will(returnValue(provisioningName));
            mockIGroup.expects(once()).method("getString").with(eq(CFG_PROVISIONINGNAME)).
                    will(returnValue(provisioningName));
        } else {
            mockIGroup.expects(once()).method("getString").with(eq(CFG_PROVISIONINGNAME)).
                    will(throwException(new UnknownParameterException(CFG_PROVISIONINGNAME, getParentGroup())));
        }

        return mockIGroup;
    }

    private IGroup getMockAttributeGroupProvisioningUnknownParameterException(String groupName, String provisioningName) {
        Mock mockIGroup = mock(IGroup.class, "mockAttributeGroupUnknownParameterException");
        mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                will(throwException(new UnknownParameterException(CFG_USER_REGISTER_NAME, getParentGroup())));
        mockIGroup.expects(once()).method("getName").will(returnValue(groupName));
        mockIGroup.expects(once()).method("getName").will(returnValue(groupName));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_PROVISIONINGNAME)).
                will(throwException(getUnknownParameterException(CFG_PROVISIONINGNAME)));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_PROVISIONINGNAME)).
                will(returnValue(provisioningName));
        return (IGroup)mockIGroup.proxy();
    }

    private IGroup getMockAttributeGroupUserRegisterUnknownParameterException(String groupName, String userRegisterName) {
        Mock mockIGroup = mock(IGroup.class, "mockAttributeGroupUnknownParameterException");
        mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                will(throwException(new UnknownParameterException(CFG_USER_REGISTER_NAME, getParentGroup())));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_USER_REGISTER_NAME)).
                will(returnValue(userRegisterName));
        mockIGroup.expects(once()).method("getName").will(returnValue(groupName));
        mockIGroup.expects(once()).method("getName").will(returnValue(groupName));
        mockIGroup.expects(once()).method("getString").with(eq(CFG_PROVISIONINGNAME)).
                will(throwException(getUnknownParameterException(CFG_PROVISIONINGNAME)));
        return (IGroup)mockIGroup.proxy();
    }

    private UnknownParameterException getUnknownParameterException(String parameter) {
        return new UnknownParameterException(parameter, getParentGroup());
    }

    protected UnknownGroupException getUnknownGroupException(String group) {
        return new UnknownGroupException(group, getParentGroup());
    }
}
