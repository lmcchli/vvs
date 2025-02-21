package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.BaseContext;
import com.mobeon.masp.profilemanager.ProfileManagerException;

public class GreetingManagerImplNoSQL extends GreetingManagerImpl {
    
    private static final ILogger log = ILoggerFactory.getILogger(GreetingManagerImplNoSQL.class);
    
    private String telephone = null;

    public GreetingManagerImplNoSQL(BaseContext context, String userId, String telephone, String folder) {
        super(context, userId, folder);
        this.telephone = telephone;
    }

    @Override
    public IMediaObject getGreeting(GreetingSpecification specification) throws ProfileManagerException {
        if (log.isDebugEnabled()) {
            log.debug("getGreeting() specification type=" + specification.getType() + ", specification format=" + specification.getFormat());
            log.debug("getGreeting() userId=" + userId + ", telephone=" + telephone);
        }
        String str = folder;
        if (str.startsWith("tel:")) {
            str = str.substring(4);
        }
        if (str.startsWith("+")) {
            str = str.substring(1);
        }
        IGreetingStore store = getStore(userId, telephone, str);
        IGreeting greeting = store.search(specification);
        if (log.isDebugEnabled()) {
            log.debug("getGreeting() greeting=" + greeting.getName());
        }
        return parseMessage(greeting, specification);
    }

}
