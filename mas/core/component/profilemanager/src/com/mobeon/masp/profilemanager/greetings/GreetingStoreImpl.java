/**
 * 
 */
package com.mobeon.masp.profilemanager.greetings;

import com.abcxyz.messaging.common.message.MSA;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;

/**
 * @author ealebie
 *
 */
public class GreetingStoreImpl implements IGreetingStore {

    private static final ILogger log = ILoggerFactory.getILogger(GreetingStoreImpl.class);

    protected MSA msa = null;

    /**
     * Before MiO 5.0, this corresponds to : private/moip/<telephone>/Greeting<p>
     * In MiO 5.0+, this corresponds to : private_moip_<telephone>_Greeting<p>
     */
    private String folder = null;

    /**
     * Before MiO 5.0, this corresponds to : /opt/mfs/internal/msa/private/moip/<p>
     * In MiO 5.0+, this corresponds to : /opt/mfs/internal/msa/<p>
     * In MiO 5.4, this does not apply anymore<p>
     */
    private String roothPath = null;    



    public GreetingStoreImpl(String msa, String folder)
    {
        this.msa = new MSA(msa);
        this.folder = folder;
        roothPath = CommonMessagingAccess.getInstance().getMoipPrivateFolder(msa, true);
        // Not so clean solution for GA
        // In a Geored situation, when doing a deposit, we fetch the spoken name of the originator.
        // If the private folder is in read only mode, we need to fetch it in mfs2.
        // Ideally, the correct path should be obtained through an MFS API, but we need more time to think of a proper solution
        boolean isReadWrite = CommonMessagingAccess.getInstance().isStorageOperationsAvailable(this.msa, this.msa);

        if(!isReadWrite) {
            roothPath = roothPath.replaceFirst("mfs", "mfs2");
        }		
    }

    /* (non-Javadoc)
     * @see com.mobeon.masp.profilemanager.greetings.IGreetingStore#create(com.mobeon.masp.profilemanager.greetings.GreetingSpecification)
     */
    @Override
    public IGreeting create(GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException {
        String path = new String();
        String fileName = buildFileName(specification);
        String extension = GreetingUtils.getFileExtension(specification.getFormat(), mediaObject.getMediaProperties().getContentType());
        path = roothPath.concat("/").concat(folder).concat("/").concat(fileName);
        log.debug("create() Greeting with msa=" + msa.toString() + ", path=" + path + ", extention=" + extension);

        IGreeting greeting = null;

        try {
            greeting = new Greeting(path, extension);
        } catch (Exception e) {
            throw new ProfileManagerException(e);
        }
        return greeting;

    }

    /* (non-Javadoc)
     * @see com.mobeon.masp.profilemanager.greetings.IGreetingStore#search(com.mobeon.masp.profilemanager.greetings.GreetingSpecification)
     */
    @Override
    public IGreeting search(GreetingSpecification specification) throws GreetingNotFoundException {

        String path = new String();
        String fileName = buildFileName(specification);
        path = roothPath.concat("/").concat(folder).concat("/").concat(fileName);
        log.debug("search() for Greeting with msa=" + msa.toString() + ", path=" + path);

        IGreeting greeting;
        try {
            greeting = new Greeting(path);
        } catch (Exception e) {
            throw new GreetingNotFoundException(specification);
        }
        if(greeting.exists()) {
            log.debug("search() for Greeting with msa=" + msa.toString() + ", path=" + path + " exists");
            return greeting;
        } else {
            throw new GreetingNotFoundException(specification);
        }
    }

    /* (non-Javadoc)
     * @see com.mobeon.masp.profilemanager.greetings.IGreetingStore#store(com.mobeon.masp.profilemanager.greetings.IGreeting)
     */
    @Override
    public void store(IGreeting greeting) throws ProfileManagerException{

        try {
            greeting.store();
        } catch (Exception e) {
            throw new ProfileManagerException(e);
        }
    }

    /**
     * Returns the proper filename (without a path and without an extension) based on the provided {@code specification}.<p>
     * It uses the :
     * <ul>
     * <li>GreetingSpecification.GreetingType
     * <li>GreetingSpecification.GreetingFormat
     * </ul>
     * 
     * For example : allcalls_voice<p>
     * 
     * @param specification
     * @return  the filename (without a path and without and extension)
     */
    protected String buildFileName(GreetingSpecification specification)
    {
        return GreetingUtils.getFileName(specification);
    }


}
