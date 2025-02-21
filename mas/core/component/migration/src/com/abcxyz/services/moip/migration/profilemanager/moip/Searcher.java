/*
 * Copyright (c) 2008 Abcxyz AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.executor.RetryException;
import com.mobeon.common.util.executor.TimeoutRetrier;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UserProvisioningException;

import javax.naming.*;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
/**
 * Searcher performs simple search tasks needed by
 * SegementedCoS classes ServiceCluster and ServiceInstance
 * @author qtommlu
 *
 */
public class Searcher {

  private static final HostedServiceLogger log = 
    new HostedServiceLogger(ILoggerFactory.getILogger(Searcher.class));
  private BaseContext baseContext = null;

  public Searcher(BaseContext context) {
    this.baseContext = context;
  }

  public ProfileAttributes[] retriedMultiSearch(String searchBase, String filter, int scope)
  throws ProfileManagerException {
      return retriedMultiSearch(searchBase, filter, scope, null);
  }
  
  public ProfileAttributes [] retriedMultiSearch(String searchBase, String filter, int scope, String[] attrs)
    throws ProfileManagerException {
    MultiSearchTask getCommunity = new MultiSearchTask(searchBase, filter, scope, attrs);
    TimeoutRetrier<ProfileAttributes []> timedRetrier = 
      new TimeoutRetrier<ProfileAttributes []>(
                                            getCommunity,
                                            baseContext.getConfig().getTryLimit(),
                                            baseContext.getConfig().getTryTimeLimit(),
                                            baseContext.getConfig().getReadTimeout()
                                            );
    try {
      return timedRetrier.call();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ProfileManagerException) {
        throw (ProfileManagerException) e.getCause();
      } else {
        // This should not happen, TimeoutRetrier should only throw ProfileManagerExceptions from SearchTask
        throw new ProfileManagerException("MultiSearchTask threw unexpected exception: " + e.getCause(), e.getCause());
      }
    } catch (TimeoutException e) {
      throw new HostException("MultiSearchTask has timed out", e);
    } catch (InterruptedException e) {
      throw new ProfileManagerException("MultiSearchTask was interrupted", e);
    }
  }

  public ProfileAttributes retriedSearch(String searchBase, String filter, int scope)
  throws ProfileManagerException {
      return retriedEntrySearch(searchBase, filter, scope, null);
  }
  
  public ProfileAttributes retriedEntrySearch(String searchBase, String filter, int scope, String[] attrs)
  throws ProfileManagerException {
  SearchTask getCommunity = new SearchTask(searchBase, filter, scope, attrs);
  TimeoutRetrier<ProfileAttributes> timedRetrier = 
    new TimeoutRetrier<ProfileAttributes>(
                                          getCommunity,
                                          baseContext.getConfig().getTryLimit(),
                                          baseContext.getConfig().getTryTimeLimit(),
                                          baseContext.getConfig().getReadTimeout()
                                          );
  try {
    return timedRetrier.call();
  } catch (ExecutionException e) {
    if (e.getCause() instanceof ProfileManagerException) {
      throw (ProfileManagerException) e.getCause();
    } else {
      // This should not happen, TimeoutRetrier should only throw ProfileManagerExceptions from SearchTask
      throw new ProfileManagerException("SearchTask threw unexpected exception: " + e.getCause(), e.getCause());
    }
  } catch (TimeoutException e) {
    throw new HostException("SearchTask has timed out", e);
  } catch (InterruptedException e) {
    throw new ProfileManagerException("SearchTask was interrupted", e);
  }
}

  

  public ProfileAttributes search(DirContext dirContext, 
                                   String searchBase, 
                                   String filter, 
                                   int searchScope)
    throws UserProvisioningException, HostException {
    return search(dirContext, searchBase, filter, searchScope, null);
  }

  public ProfileAttributes search(DirContext dirContext, String searchBase, String filter,
                                   int searchScope, String[] attrs)
    throws UserProvisioningException, HostException {
    SearchControls ctls = getReadSearchControl();
    ctls.setSearchScope(searchScope);
    ctls.setReturningAttributes(attrs);
    try {
      NamingEnumeration<SearchResult> results = dirContext.search(searchBase, filter, ctls);
      if (results.hasMore()) {
        SearchResult result = results.next();
        if (results.hasMore()) {
          String errMsg = getSearchErrorMessage("Search returns multiple results", searchBase, filter);
          throw new UserProvisioningException(errMsg);
        }
        return new ProfileAttributes(baseContext, result);
      } else {
        String errMsg = getSearchErrorMessage("Search returns no result", searchBase, filter);
        throw new UserProvisioningException(errMsg);
      }
    } catch (NamingException e) {
      StringBuilder errMsg = new StringBuilder();
      errMsg.append(getSearchErrorMessage("Search failed", searchBase, filter));
      errMsg.append(" url=").append(getProviderUrlFromException(e));
      throw new HostException(errMsg + ": " + e, e);
    }
  }
  
  private String getSearchErrorMessage(String message, String searchBase, String filter) {
    StringBuilder errMsg;
    errMsg = new StringBuilder(message);
    errMsg.append(". SearchBase<").append(searchBase).append("> ");
    errMsg.append("Filter<").append(filter).append("> ");
    return errMsg.toString();
  }
  
  private String getProviderUrlFromException(NamingException e) {
    if (e.getResolvedObj() != null) {
      Object resolvedObj = e.getResolvedObj();
      if (resolvedObj instanceof Context) {
        Context ldapCtx = (Context) resolvedObj;
        try {
          Hashtable env = ldapCtx.getEnvironment();
          return (String) env.get("java.naming.provider.url");
        } catch (NamingException e1) {
          log.error("Could not get ProviderUrl " + e1);
        }
      }
    }
    return "";
  }
  
  private SearchControls getReadSearchControl() {
    SearchControls readSearchControls = new SearchControls();
    readSearchControls.setTimeLimit(baseContext.getConfig().getReadTimeout());
    return readSearchControls;
  }

  
  public ProfileAttributes search(ProfileManagerImpl impl, DirContext dirContext, String searchBase, String filter, int searchScope, String[] attrs)
        throws UserProvisioningException, HostException {
    SearchControls ctls = impl.getReadSearchControl();
    ctls.setSearchScope(searchScope);
    ctls.setReturningAttributes(attrs);
    try {
        NamingEnumeration<SearchResult> results = dirContext.search(searchBase, filter, ctls);
        if (results.hasMore()) {
            SearchResult result = results.next();
            if (results.hasMore()) {
                String errMsg = impl.getSearchErrorMessage("Search returns multiple results", searchBase, filter);
                throw new UserProvisioningException(errMsg);
            }
            return new ProfileAttributes(impl.getContext(), result);
        } else {
            String errMsg = impl.getSearchErrorMessage("Search returns no result", searchBase, filter);
            throw new UserProvisioningException(errMsg);
        }
    } catch (NamingException e) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append(impl.getSearchErrorMessage("Search failed", searchBase, filter));
        errMsg.append(" url=").append(impl.getProviderUrlFromException(e));
        throw new HostException(errMsg + ": " + e, e);
    }
}

  public ProfileAttributes [] multiSearch(DirContext dirContext, String searchBase, String filter, int searchScope, String[] attrs)
  throws UserProvisioningException, HostException {
      SearchControls ctls = getReadSearchControl();
      ctls.setSearchScope(searchScope);
      ctls.setReturningAttributes(attrs);
      try {
          List<ProfileAttributes> profileAttrs = new ArrayList<ProfileAttributes>();
          NamingEnumeration<SearchResult> results = dirContext.search(searchBase, filter, ctls);
          while (results.hasMore()) {
              SearchResult result = results.next();
              if (result.getAttributes().size() == 0) {
                  log.warn(getSearchErrorMessage("MultiSearch empty result", searchBase, filter).toString());
                  continue;
              }
              profileAttrs.add(new ProfileAttributes(baseContext, result));
          }
          return profileAttrs.toArray(new ProfileAttributes[0]);
      } catch (NamingException e) {
          StringBuilder errMsg = new StringBuilder();
          errMsg.append(getSearchErrorMessage("MultiSearch failed", searchBase, filter));
          errMsg.append(" url=").append(getProviderUrlFromException(e));
          throw new HostException(errMsg + ": " + e, e);
      }
  }

  

/* Nested class search task */
  public class SearchTask implements Callable<ProfileAttributes> {
    String searchBase;
    String filter;
    int scope;
    String[] attrs;
    
    public SearchTask(String searchBase, String filter, int scope, String[] attrs) {
      this.searchBase = searchBase;
      this.filter = filter;
      this.scope = scope;
      this.attrs = attrs;
    }
    
    public ProfileAttributes call() throws ProfileManagerException, RetryException {
      LdapServiceInstanceDecorator serviceInstance = baseContext.getServiceInstance(Direction.READ);
      DirContext dirContext = null;
      boolean release = false;
      try {
        dirContext = baseContext.getDirContext(serviceInstance, Direction.READ);
        ProfileAttributes result = search(dirContext, searchBase, filter, scope, attrs);
        log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
        return result;
      } catch (HostException e) {
        if (e.getCause() instanceof CommunicationException) {
          log.notAvailable(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort(), e.toString());
          baseContext.getServiceLocator().reportServiceError(serviceInstance.getDecoratedServiceInstance());
          release = true;
          throw new RetryException(e);
        } else {
          throw e;
        }
      } finally {
        baseContext.returnDirContext(dirContext, release);
      }
    }
  }
  /* Nested class multi-search task */
  public class MultiSearchTask implements Callable<ProfileAttributes []> {
    String searchBase;
    String filter;
    int scope;
    String[] attrs;
    
    public MultiSearchTask(String searchBase, String filter, int scope, String[] attrs) {
      this.searchBase = searchBase;
      this.filter = filter;
      this.scope = scope;
      this.attrs = attrs;
    }
    
    public ProfileAttributes [] multiCall() throws ProfileManagerException, RetryException {
    return null; /*ProfileAttributes []*/
    }
    
    public ProfileAttributes [] call() throws ProfileManagerException, RetryException {
      LdapServiceInstanceDecorator serviceInstance = baseContext.getServiceInstance(Direction.READ);
      DirContext dirContext = null;
      boolean release = false;
      try {
        dirContext = baseContext.getDirContext(serviceInstance, Direction.READ);
        ProfileAttributes [] result = multiSearch(dirContext, searchBase, filter, scope, attrs);
        log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
        return result;
      } catch (HostException e) {
        if (e.getCause() instanceof CommunicationException) {
          log.notAvailable(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort(), e.toString());
          baseContext.getServiceLocator().reportServiceError(serviceInstance.getDecoratedServiceInstance());
          release = true;
          throw new RetryException(e);
        } else {
          throw e;
        }
      } finally {
        baseContext.returnDirContext(dirContext, release);
      }
    }
  }
}
