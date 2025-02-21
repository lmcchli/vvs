package com.mobeon.masp.profilemanager;

import com.mobeon.masp.profilemanager.greetings.GreetingMockObjectBaseTestCase;

import javax.naming.directory.*;

/**
 * Documentation
 *
 * @author mande
 */
public abstract class DistributionListMockObjectBaseTestCase extends GreetingMockObjectBaseTestCase {
    protected static final String DIST_LIST_ID = "1";
    protected static final String MAIL_HOST = "mailhost";
    protected static final String MAIL = "mande1@lab.mobeon.com";
    protected static final String BASE_DN = "uniqueidentifier=um35,ou=c6,o=mobeon.com";
    private static final String DIST_LIST_RDN = "mail=" + DIST_LIST_ID + MAIL;
    protected static final String DIST_LIST_DN = DIST_LIST_RDN + "," + BASE_DN;
    protected static final String MGRPRFC822MAILMEMBER = "mgrprfc822mailmember";

    public DistributionListMockObjectBaseTestCase(String string) {
        super(string);
    }

    protected Attributes getCreateSubcontextAttributesComponent(String id) {
        BasicAttributes attributes = new BasicAttributes();
        BasicAttribute basicAttribute = new BasicAttribute("objectclass");
        basicAttribute.add("top");
        basicAttribute.add("inetlocalmailrecipient");
        basicAttribute.add("inetmailgroup");
        basicAttribute.add("groupofuniquenames");
        basicAttribute.add("distributionlist");
        attributes.put(basicAttribute);
        attributes.put(new BasicAttribute("cn", id));
        attributes.put(new BasicAttribute("mailhost", MAIL_HOST));
        attributes.put(new BasicAttribute("dlname", "mail=" + id + MAIL));
        attributes.put(new BasicAttribute("description", id));
        return attributes;
    }

    protected SearchResult getDistributionListSearchResult(String id, String... members) {
        Attributes attrs = getCreateSubcontextAttributesComponent(id);
        // Attribute should not exist if no members
        if (members.length > 0) {
            Attribute attr = new BasicAttribute(MGRPRFC822MAILMEMBER);
            for (String member : members) {
                attr.add(member);
            }
            attrs.put(attr);
        }
        return new SearchResult("mail=" + id + MAIL + "," + BASE_DN, null, attrs);
    }
}
