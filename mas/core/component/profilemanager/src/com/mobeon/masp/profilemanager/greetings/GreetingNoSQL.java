package com.mobeon.masp.profilemanager.greetings;

import com.abcxyz.messaging.common.message.MSA;
import com.mobeon.masp.profilemanager.mediafile.AbstractMediaFileNoSQL;


/**
 * The GreetingNoSQL class manages greeting information in the database.
 * <p>
 * Greetings are managed using two entries: a media entry that contains the sound or video data for a greeting
 * and a property entry that contains additional information. When a Greeting object is created it, loads
 * the property entry and the media entry into memory if they exists. When a greeting is created with
 * non-existing media and property entries, it has no media stream and no property. Its setter methods can
 * be used to fill the greeting. When it is ready, it can be stored on in the databse using the {@link #store()}
 * method.
 * </p>
 */
class GreetingNoSQL extends AbstractMediaFileNoSQL implements IGreeting {
    
    static final String DEFAULT_GREETING_FNAME = "greeting";

    /**
     * Creates a new Greeting object
     * 
     * @param msa associated to the greeting
     * @param telephone associated to the greeting.
     * @param name of the greeting with no extension.
     * @throws Exception If there is any error.
     */
    GreetingNoSQL(MSA msa, String telephone, String name) throws Exception {
        super(msa, telephone, name);
        if (name == null){
            setName(DEFAULT_GREETING_FNAME);
        }
    }

    /**
     * Creates a new Greeting object
     * 
     * @param msa associated to the greeting
     * @param telephone associated to the greeting.
     * @param name of the greeting with no extension.
     * @param the extension
     * @throws Exception If there is any error.
     */
    public GreetingNoSQL(MSA msa, String telephone, String name, String extension) throws Exception {
        super(msa, telephone, name, extension);
    }


}
