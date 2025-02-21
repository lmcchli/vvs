package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;

public class GreetingStoreImplNoSQL extends GreetingStoreImpl {

    private static final ILogger log = ILoggerFactory.getILogger(GreetingStoreImplNoSQL.class);
    
    private String telephone = null;
    
    public GreetingStoreImplNoSQL(String msa, String telephone) {
        super(msa, null);
        this.telephone = telephone;
    }

    @Override
    public IGreeting search(GreetingSpecification specification) throws GreetingNotFoundException {
        
        String fileName = buildFileName(specification);
        log.debug("search() for Greeting with msa=" + msa.toString() + ", telephone=" + telephone + ", filename=" + fileName);
        
        IGreeting greeting;
        try {
            greeting = new GreetingNoSQL(msa, telephone, fileName);
        } catch (Exception e) {
            throw new GreetingNotFoundException(specification);
        }
        if(greeting.exists()) {
            log.debug("search() Greeting with msa=" + msa.toString() + ", telephone=" + telephone + ", filename=" + fileName + " exists");
            return greeting;
        } else {
            throw new GreetingNotFoundException(specification);
        }
    }

    @Override
    public IGreeting create(GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException {
        String fileName = buildFileName(specification);
        String extension = GreetingUtils.getFileExtension(specification.getFormat(), mediaObject.getMediaProperties().getContentType());
        log.debug("create() Greeting with msa=" + msa.toString() + ", telephone=" + telephone + ", filename=" + fileName + ", extention=" + extension);
        
        IGreeting greeting = null;
        try {
            greeting = new GreetingNoSQL(msa, telephone, fileName, extension);
        } catch (Exception e) {
            throw new ProfileManagerException(e);
        }
        return greeting;
    }

}
