/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.util.executor.RetryException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;

import javax.naming.directory.ModificationItem;
import javax.naming.directory.DirContext;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import java.util.concurrent.Callable;

/**
 * A retryable JNDI modification task submittable to a TimedRetrier
 *
 * @author mande
 */
class ModifyTask implements Callable<Object> {
    private static final ILogger logg = ILoggerFactory.getILogger(ModifyTask.class);
    private static final HostedServiceLogger log = new HostedServiceLogger(logg);

    private String name;
    private ModificationItem[] mods;
    private BaseContext context;

    public ModifyTask(BaseContext context, String name, ModificationItem[] mods) {
        this.context = context;
        this.name = name;
        this.mods = mods;
    }

    public Object call() throws ProfileManagerException, RetryException {
        LdapServiceInstanceDecorator serviceInstance = getContext().getServiceInstance(Direction.WRITE);
        DirContext dirContext = null;
        boolean release = false;
        try {
            dirContext = getContext().getDirContext(serviceInstance, Direction.WRITE);
            modifyAttributes(dirContext);
            log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
            return null;
        } catch (HostException e) {
            if (e.getCause() instanceof CommunicationException) {
                log.notAvailable(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort(), e.toString());
                getContext().getServiceLocator().reportServiceError(serviceInstance.getDecoratedServiceInstance());
                release = true;
                throw new RetryException(e);
            } else {
                throw e;
            }
        } finally {
            getContext().returnDirContext(dirContext, release);
        }
    }

    private void modifyAttributes(DirContext dirContext) throws HostException {
        try {
            dirContext.modifyAttributes(name, mods);
        } catch (NamingException e) {
            throw new HostException("Write failed: " + e, e);
        }
    }

    private BaseContext getContext() {
        return context;
    }
}
