/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.greetings.SpokenNameSpecification;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingType;
import com.mobeon.common.util.executor.RetryException;
import com.mobeon.common.util.executor.TimeoutRetrier;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.HostedServiceLogger;

import javax.naming.directory.*;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Handles distribution lists
 *
 * @author mande
 */
public class DistributionListImpl implements IDistributionList {
    private static final ILogger logg = ILoggerFactory.getILogger(DistributionListImpl.class);
    private static final HostedServiceLogger log = new HostedServiceLogger(logg);

    private static final Attribute DIST_LIST_OBJECT_CLASS = new BasicAttribute("objectclass");
    static {
        DIST_LIST_OBJECT_CLASS.add("top");
        DIST_LIST_OBJECT_CLASS.add("inetlocalmailrecipient");
        DIST_LIST_OBJECT_CLASS.add("inetmailgroup");
        DIST_LIST_OBJECT_CLASS.add("groupofuniquenames");
        DIST_LIST_OBJECT_CLASS.add("distributionlist");
    }

    private static final String MGRPRFC822MAILMEMBER = "mgrprfc822mailmember";

    private BaseContext context;
    private String id;
    private String dn;
    private String mail;
    private String mailHost;
    private String rdn;
    private IProfile subscriber;

    private Set<String> members = new HashSet<String>();
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    DistributionListImpl(BaseContext context, String id, IProfile subscriber, String baseDn) throws ProfileManagerException {
        this.context = context;
        this.id = id;
        this.subscriber = subscriber;
        this.mail = subscriber.getStringAttribute("mail");
        this.mailHost = subscriber.getStringAttribute("mailhost");
        dn = createDn(baseDn);
        create();
    }

    DistributionListImpl(BaseContext context, SearchResult searchResult, IProfile subscriber) throws ProfileManagerException {
        this.context = context;
        dn = searchResult.getName();
        Attributes attributes = searchResult.getAttributes();
        try {
            id = (String)attributes.get("cn").get();
            Attribute attribute = attributes.get(MGRPRFC822MAILMEMBER);
            if (attribute != null) {
                NamingEnumeration<?> members = attribute.getAll();
                while (members.hasMore()) {
                    this.members.add((String)members.next());
                }
            }
        } catch (NamingException e) {
            throw new ProfileManagerException("Could not parse distribution list search result. " + e.getMessage(), e);
        }
        this.subscriber = subscriber;
        if (log.isDebugEnabled()) {
            log.debug("DistributionListImpl(BaseContext, SearchResult, IProfile) returns " + toString());
        }
    }

    public String getID() {
        log.info("getID() returns " + id);
        return id;
    }

    public void addMember(String member) throws ProfileManagerException {
        log.info("addMember(member=" + member + ")");
        if (members.add(member)) {
            writeMembers();
        }
        log.info("addMember(String) returns void");
    }

    public void removeMember(String member) throws ProfileManagerException {
        log.info("removeMember(member=" + member + ")");
        if (members.remove(member)) {
            writeMembers();
        }
        log.info("removeMember(String) returns void");
    }

    public String[] getMembers() {
        log.info("getMembers()");
        String[] memberArray = members.toArray(EMPTY_STRING_ARRAY);
        log.info("getMembers() returns " + Arrays.toString(memberArray));
        return memberArray;
    }

    public IMediaObject getSpokenName() throws ProfileManagerException {
        log.info("getSpokenName()");
        SpokenNameSpecification specification = new SpokenNameSpecification(
                GreetingType.DIST_LIST_SPOKEN_NAME,
                GreetingFormat.VOICE,
                id
        );
        IMediaObject greeting = subscriber.getGreeting(specification);
        log.info("getSpokenName() returns " + greeting);
        return greeting;
    }

    public void setSpokenName(IMediaObject spokenName) throws ProfileManagerException {
        log.info("setSpokenName(spokenName=" + spokenName + ")");
        SpokenNameSpecification specification = new SpokenNameSpecification(
                GreetingType.DIST_LIST_SPOKEN_NAME,
                GreetingFormat.VOICE,
                id
        );
        subscriber.setGreeting(specification, spokenName);
        log.info("setSpokenName(IMediaObject) returns void");
    }

    public String toString() {
        StringBuilder str = new StringBuilder("DistributionList(");
        str.append(id).append(")");
        if (members.size() > 0) {
            str.append(members);
        }
        return str.toString();
    }

    private void create() throws ProfileManagerException {
        CreateTask createTask = new CreateTask(dn, getSubContextAttributes());
        retryTask(createTask);
    }

    private void writeMembers() throws ProfileManagerException {
        ModificationItem[] mods = new ModificationItem[1];
        BasicAttribute attr = new BasicAttribute(MGRPRFC822MAILMEMBER);
        for (String member : members) {
            attr.add(member);
        }
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
        ModifyTask modifyTask = new ModifyTask(getContext(), dn, mods);
        retryTask(modifyTask);
    }

    private Attributes getSubContextAttributes() {
        Attributes attributes = new BasicAttributes();
        attributes.put(DIST_LIST_OBJECT_CLASS);
        attributes.put(new BasicAttribute("cn", id));
        attributes.put(new BasicAttribute("mailhost", mailHost));
        attributes.put(new BasicAttribute("dlname", rdn));
        attributes.put(new BasicAttribute("description", id));
        return attributes;
    }

    private void retryTask(Callable<Object> task) throws ProfileManagerException {
        TimeoutRetrier<Object> timedRetrier = new TimeoutRetrier<Object>(
                task,
                getContext().getConfig().getTryLimit(),
                getContext().getConfig().getTryTimeLimit(),
                getContext().getConfig().getWriteTimeout()
        );
        try {
            timedRetrier.call();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ProfileManagerException) {
                throw (ProfileManagerException)e.getCause();
            } else {
                // This should not happen, TimeoutRetrier should only throw ProfileManagerExceptions from tasks
                throw new ProfileManagerException("Task threw unexpected exception: " + e.getMessage(), e.getCause());
            }
        } catch (TimeoutException e) {
            throw new HostException("Task has timed out", e);
        } catch (InterruptedException e) {
            throw new HostException("Task was interrupted", e);
        }
    }

    private BaseContext getContext() {
        return context;
    }

    private String createDn(String baseDn) {
        StringBuilder dn = new StringBuilder("mail=");
        dn.append(id).append(mail);
        rdn = dn.toString();
        dn.append(",").append(baseDn);
        log.debug("createDn(String) returns " + dn);
        return dn.toString();
    }

    private class CreateTask implements Callable<Object> {
        private String dn;
        private Attributes attrs;

        public CreateTask(String dn, Attributes attrs) {
            this.dn = dn;
            this.attrs = attrs;
        }

        public Object call() throws Exception {
            LdapServiceInstanceDecorator serviceInstance = getContext().getServiceInstance(Direction.WRITE);
            DirContext dirContext = null;
            boolean release = false;
            try {
                dirContext = getContext().getDirContext(serviceInstance, Direction.WRITE);
                if (log.isDebugEnabled()) {
                    log.debug("createSubcontext(dn=" + dn + ", attrs=" + attrs + ")");
                }
                dirContext.createSubcontext(dn, attrs);
                log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
                return null;
            } catch (CommunicationException e) {
                log.notAvailable(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort(), e.toString());
                getContext().getServiceLocator().reportServiceError(serviceInstance.getDecoratedServiceInstance());
                release = true;
                throw new RetryException(new HostException("Could not create distribution list: " + e, e));
            } catch (NamingException e) {
                throw new HostException("Could not create distribution list. " + e, e);
            } finally {
                getContext().returnDirContext(dirContext, release);
            }
        }
    }
}
