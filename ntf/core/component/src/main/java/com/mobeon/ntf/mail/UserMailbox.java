/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

import java.io.IOException;
import java.util.Vector;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.UserInbox;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;

import static com.mobeon.ntf.Constants.MSG_COUNT_ERR;

/****************************************************************
 * This class handles information from the users mailbox, such as message counts
 * and quota warnings. It does not actually fetch the count until it is
 * requested, so the count is as up to date as possible. If the count is needed
 * again after a long time e.g. in case of retries, a refresh of the count can
 * be requested.
 ****************************************************************/
public class UserMailbox {
    private boolean fetched = false;

    /** Total number of all messages that is not deleted in mailbox */
    private int allTotalCount=MSG_COUNT_ERR;

    /** Total number of new messages in mailbox */
    private int newTotalCount=MSG_COUNT_ERR;

    private int oldTotalCount=MSG_COUNT_ERR;

    private int saveTotalCount=MSG_COUNT_ERR;

    private int newVoiceCount=MSG_COUNT_ERR;

    private int newFaxCount=MSG_COUNT_ERR;

    private int newEmailCount=MSG_COUNT_ERR;

    private int newVideoCount=MSG_COUNT_ERR;

    private int oldVoiceCount=MSG_COUNT_ERR;

    private int oldVideoCount=MSG_COUNT_ERR;

    private int oldEmailCount=MSG_COUNT_ERR;

    private int oldFaxCount=MSG_COUNT_ERR;

    private int saveVoiceCount=MSG_COUNT_ERR;

    private int saveVideoCount=MSG_COUNT_ERR;

    private int saveEmailCount=MSG_COUNT_ERR;

    private int saveFaxCount=MSG_COUNT_ERR;

    private int allUrgentTotalCount=MSG_COUNT_ERR;

    private int newUrgentTotalCount=MSG_COUNT_ERR;

    private int oldUrgentTotalCount=MSG_COUNT_ERR;

    private int saveUrgentTotalCount=MSG_COUNT_ERR;

    private int newUrgentVoiceCount=MSG_COUNT_ERR;
    
    private int newConfidentialVoiceCount=MSG_COUNT_ERR;

    private int newUrgentFaxCount=MSG_COUNT_ERR;

    private int newUrgentEmailCount=MSG_COUNT_ERR;

    private int newUrgentVideoCount=MSG_COUNT_ERR;
    
    private int newConfidentialVideoCount=MSG_COUNT_ERR;

    private int oldUrgentVoiceCount=MSG_COUNT_ERR;

    private int saveUrgentVoiceCount=MSG_COUNT_ERR;

    private int oldUrgentFaxCount=MSG_COUNT_ERR;

    private int saveUrgentFaxCount=MSG_COUNT_ERR;

    private int oldUrgentEmailCount=MSG_COUNT_ERR;

    private int saveUrgentEmailCount=MSG_COUNT_ERR;

    private int oldUrgentVideoCount=MSG_COUNT_ERR;

    private int saveUrgentVideoCount=MSG_COUNT_ERR;

    private boolean hasMail = false;

    private boolean hasFax = false;

    private boolean hasVoice = false;

    private boolean hasVideo = false;

    private MSA msa;

    private final LogAgent logger = NtfCmnLogger.getLogAgent(UserMailbox.class);

    /**
     *@param uid the user uid for logging in to the mailbox.
     *@param mail tells if the users normal emails shall be counted.
     *@param fax tells if the users faxes shall be counted.
     *@param voice tells if the users voice messages shall be counted.
     *@param voice tells if the users video messages shall be counted.
     */
    public UserMailbox(MSA msa, final boolean mail, final boolean fax, final boolean voice, final boolean video) {
        this.hasFax = fax;
        this.hasMail = mail;
        this.hasVoice = voice;
        this.hasVideo = video;
        this.msa = msa;
    }

    /**
     * This constructor is for testing only.
     *
     * @param v the number of voice messages.
     *@param f the number of FAX messages.
     *@param e the number of email messages.
     *@param m the number of video messages.
     *@param q true if quota is exceeded.
     */
    public UserMailbox(final int v, final int f, final int e, final int m, final int uv, final int uf, final int ue, final int um,final boolean q) {
        hasFax = true;
        hasMail = true;
        hasVoice = true;
        hasVideo = true;
        newVoiceCount = v;
        newFaxCount = f;
        newEmailCount = e;
        newVideoCount = m;
        newUrgentVoiceCount = uv;
        newUrgentFaxCount =uf;
        newUrgentEmailCount =ue;
        newUrgentVideoCount =um;
        allUrgentTotalCount= uv + uf + ue + um;
        newUrgentTotalCount= uv + uf + ue + um;;
        allTotalCount = v + f + e + m;
        newTotalCount = v + f + e + m;
        fetched = true;
    }

    /**
     *@return the total number of new messages of the types the subscriber has
     *         access to or -1 if no message count could be obtained
     */
    public int getNewTotalCount() {
        refresh();

        return newTotalCount;
    }

    /**
     *@return the total number of old messages of the types the subscriber has
     *         access to or -1 if no message count could be obtained
     */
    public int getOldTotalCount() {
        refresh();

        return oldTotalCount;
    }

    /**
     *@return the total number of save messages of the types the subscriber has
     *         access to or -1 if no message count could be obtained
     */
    public int getSaveTotalCount() {
        refresh();

        return saveTotalCount;
    }

    /**
     * @return Total number of messages in mailbox, new and old. Does not care
     *         if subscriber has access to messages type or not
     */
    public int getAllTotalCount() {
        refresh();
        return allTotalCount;
    }



    /**
     *@return the total number of new urgent messages of the types the subscriber has
     *         access to or -1 if no message count could be obtained
     */
    public int getNewUrgentTotalCount() {
        refresh();

        return newUrgentTotalCount;
    }

    /**
     *@return the total number of old messages of the types the subscriber has
     *         access to or -1 if no message count could be obtained
     */
    public int getOldUrgentTotalCount() {
        refresh();

        return oldUrgentTotalCount;
    }

    /**
     *@return the total number of save messages of the types the subscriber has
     *         access to or -1 if no message count could be obtained
     */
    public int getSaveUrgentTotalCount() {
        refresh();

        return saveUrgentTotalCount;
    }
    /**
     * @return Total number of messages in mailbox, new and old. Does not care
     *         if subscriber has access to messages type or not
     */
    public int getAllUrgentTotalCount() {
        refresh();
        return allUrgentTotalCount;
    }


    /**
     *@return the total number of unseen voice messages or -1 if no message
     *         count could be obtained.
     */
    public int getNewVoiceCount() {
        refresh();

        return newVoiceCount;
    }

    /**
     *@return the total number of  voice messages or -1 if no message
     *         count could be obtained.
     */
    public int getVoiceTotalCount() {
        refresh();
        return newVoiceCount+ oldVoiceCount + saveVoiceCount;
    }
    /**
     *@return the total number of  fax messages or -1 if no message
     *         count could be obtained.
     */
    public int getFaxTotalCount() {
        refresh();
        return newFaxCount+ oldFaxCount + saveFaxCount;
    }
    /**
     *@return the total number of  video messages or -1 if no message
     *         count could be obtained.
     */
    public int getVideoTotalCount() {
        refresh();
        return newVideoCount+ oldVideoCount + saveVideoCount;
    }

    /**
     *@return the total number of unseen urgent voice messages or -1 if no message
     *         count could be obtained.
     */
    public int getNewUrgentVoiceCount() {
        refresh();

        return newUrgentVoiceCount;
    }
    
    /**
     *@return the total number of unseen confidential voice messages or -1 if no message
     *         count could be obtained.
     */
    public int getNewConfidentialVoiceCount() {
        refresh();

        return newConfidentialVoiceCount;
    }

    /**
     *@return the total number of old voice messages or -1 if no message count
     *         could be obtained.
     */
    public int getOldVoiceCount() {
        refresh();

        return oldVoiceCount;
    }

    /**
     *@return the total number of old urgent voice messages or -1 if no message count
     *         could be obtained.
     */
    public int getOldUrgentVoiceCount() {
        refresh();

        return oldUrgentVoiceCount;
    }

    /**
     *@return the total number of save voice messages or -1 if no message count
     *         could be obtained.
     */
    public int getSaveVoiceCount() {
        refresh();

        return saveVoiceCount;
    }

    /**
     *@return the total number of old urgent voice messages or -1 if no message count
     *         could be obtained.
     */
    public int getSaveUrgentVoiceCount() {
        refresh();

        return saveUrgentVoiceCount;
    }
    /**
     * @return the total number of unseen fax messages or 0 if the subscriber
     *         does not have fax access or -1 if no message count could be
     *         obtained.
     */
    public int getNewFaxCount() {
        refresh();

        return newFaxCount;
    }

    /**
     * @return the total number of unseen urgent fax messages or 0 if the subscriber
     *         does not have fax access or -1 if no message count could be
     *         obtained.
     */
    public int getNewUrgentFaxCount() {
        refresh();

        return newUrgentFaxCount;
    }

    /**
     * @return the total number of old fax messages or 0 if the subscriber does
     *         not have fax access or -1 if no message count could be obtained.
     */
    public int getOldFaxCount() {
        refresh();

        return oldFaxCount;
    }

    /**
     * @return the total number of old urgent fax messages or 0 if the subscriber does
     *         not have fax access or -1 if no message count could be obtained.
     */
    public int getOldUrgentFaxCount() {
        refresh();

        return oldUrgentFaxCount;
    }

    /**
     * @return the total number of save fax messages or 0 if the subscriber does
     *         not have fax access or -1 if no message count could be obtained.
     */
    public int getSaveFaxCount() {
        refresh();

        return saveFaxCount;
    }

    /**
     * @return the total number of save urgent fax messages or 0 if the subscriber does
     *         not have fax access or -1 if no message count could be obtained.
     */
    public int getSaveUrgentFaxCount() {
        refresh();

        return saveUrgentFaxCount;
    }


    /**
     * @return the total number of unseen email messages or 0 if the subscriber
     *         does not have email access or -1 if no message count could be
     *         obtained.
     */
    public int getNewEmailCount() {
        refresh();

        return newEmailCount;
    }

    /**
     * @return the total number of unseen email messages or 0 if the subscriber
     *         does not have email access or -1 if no message count could be
     *         obtained.
     */
    public int getNewUrgentEmailCount() {
        refresh();

        return newUrgentEmailCount;
    }

    /**
     * @return the total number of save email messages or 0 if the subscriber
     *         does not have email access or -1 if no message count could be
     *         obtained.
     */
    public int getSaveEmailCount() {
        refresh();

        return oldEmailCount;
    }

    /**
     * @return the total number of save email messages or 0 if the subscriber
     *         does not have email access or -1 if no message count could be
     *         obtained.
     */
    public int getSaveUrgentEmailCount() {
        refresh();

        return saveUrgentEmailCount;
    }

    /**
     * @return the total number of old email messages or 0 if the subscriber
     *         does not have email access or -1 if no message count could be
     *         obtained.
     */
    public int getOldEmailCount() {
        refresh();

        return oldEmailCount;
    }

    /**
     * @return the total number of old email messages or 0 if the subscriber
     *         does not have email access or -1 if no message count could be
     *         obtained.
     */
    public int getOldUrgentEmailCount() {
        refresh();

        return oldUrgentEmailCount;
    }

    /**
     * @return the total number of new video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getNewVideoCount() {
        refresh();

        return newVideoCount;
    }

    /**
     * @return the total number of new urgent video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getNewUrgentVideoCount() {
        refresh();

        return newUrgentVideoCount;
    }
    
    /**
     * @return the total number of new confidential video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getNewConfidentialVideoCount() {
        refresh();

        return newConfidentialVideoCount;
    }

    /**
     * @return the total number of old video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getOldVideoCount() {
        refresh();

        return oldVideoCount;
    }

    /**
     * @return the total number of save video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getSaveVideoCount() {
        refresh();

        return saveVideoCount;
    }

    /**
     * @return the total number of old video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getOldUrgentVideoCount() {
        refresh();

        return oldUrgentVideoCount;
    }

    /**
     * @return the total number of save video messages or 0 if the subscriber
     *         does not have video access or -1 if no message count could be
     *         obtained.
     */
    public int getSaveUrgentVideoCount() {
        refresh();

        return saveUrgentVideoCount;
    }

    /**
     * Refreshes the message counts for this user.
     *
     * @throws IOException
     */
    public synchronized void refresh() {
        if (fetched) {
            return;
        }


        allTotalCount = MSG_COUNT_ERR;
        allUrgentTotalCount = MSG_COUNT_ERR;

        newTotalCount = MSG_COUNT_ERR;
        newVoiceCount = MSG_COUNT_ERR;
        newFaxCount   = MSG_COUNT_ERR;
        newEmailCount = MSG_COUNT_ERR;
        newVideoCount = MSG_COUNT_ERR;
        newUrgentTotalCount = MSG_COUNT_ERR;
        newUrgentVoiceCount = MSG_COUNT_ERR;
        newConfidentialVoiceCount = MSG_COUNT_ERR;
        newUrgentFaxCount   = MSG_COUNT_ERR;
        newUrgentEmailCount = MSG_COUNT_ERR;
        newUrgentVideoCount = MSG_COUNT_ERR;
        newConfidentialVideoCount = MSG_COUNT_ERR;

        oldTotalCount = MSG_COUNT_ERR;
        oldVoiceCount = MSG_COUNT_ERR;
        oldFaxCount   = MSG_COUNT_ERR;
        oldEmailCount = MSG_COUNT_ERR;
        oldVideoCount = MSG_COUNT_ERR;
        oldUrgentTotalCount = MSG_COUNT_ERR;
        oldUrgentVoiceCount = MSG_COUNT_ERR;
        oldUrgentFaxCount   = MSG_COUNT_ERR;
        oldUrgentEmailCount = MSG_COUNT_ERR;
        oldUrgentVideoCount = MSG_COUNT_ERR;

        saveTotalCount = MSG_COUNT_ERR;
        saveVoiceCount = MSG_COUNT_ERR;
        saveFaxCount   = MSG_COUNT_ERR;
        saveEmailCount = MSG_COUNT_ERR;
        saveVideoCount = MSG_COUNT_ERR;
        saveUrgentTotalCount = MSG_COUNT_ERR;
        saveUrgentVoiceCount = MSG_COUNT_ERR;
        saveUrgentFaxCount   = MSG_COUNT_ERR;
        saveUrgentEmailCount = MSG_COUNT_ERR;
        saveUrgentVideoCount = MSG_COUNT_ERR;

        try {
            getCount();
            fetched = true;
        } catch(final IOException ioe) {
            logger.error("UserMailbox failed: " + ioe.getMessage());
        } catch(final MsgStoreException msge) {
            logger.error("UserMailbox failed: " + msge.getMessage());
        }

    }

    /**
     * Tells if a message count has been done for this user.
     *
     * @return true if a message count has been fetched
     */
    public boolean isCountFetched() {
        return fetched;
    }

    /**
     * getCount searches the MFS for this userId for unread messages and counts
     * them.
     *
     * @throws MsgStoreException
     */
    private void getCount() throws IOException, MsgStoreException {
    	UserInbox userInbox = new UserInbox(msa);

    	userInbox.addFilter(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
    	userInbox.addFilter(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_READ);

    	if (hasVoice || hasMail) {
    		userInbox.addC1Filter(Container1.Message_class, ServiceName.VOICE);
    	}

        if (hasFax || hasMail) {
    		userInbox.addC1Filter(Container1.Message_class, ServiceName.FAX);
        }

        if (hasVideo || hasMail) {
    		userInbox.addC1Filter(Container1.Message_class, ServiceName.VIDEO);
        }

        //execute querying
        userInbox.queryMfs();

        //retrieving results

        // Get all unread messages
        newTotalCount = userInbox.countStateFile(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        // Get all read messages
        oldTotalCount = userInbox.countStateFile(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_READ);
        // Get all read messages
        saveTotalCount = userInbox.countStateFile(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_SAVED);
        // Total number of messages
        allTotalCount = newTotalCount + oldTotalCount+saveTotalCount;


        StateAttributesFilter filter;

    	filter = new StateAttributesFilter();
        // Get all new urgent message count voice message
        filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
        filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
        newUrgentTotalCount = userInbox.countStateFile(filter);

    	filter = new StateAttributesFilter();
        // Get all new urgent message count voice message
        filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_READ);
        filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
        oldUrgentTotalCount = userInbox.countStateFile(filter);


        filter = new StateAttributesFilter();
        // Get all new urgent message count voice message
        filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_SAVED);
        filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
        saveUrgentTotalCount = userInbox.countStateFile(filter);


        allUrgentTotalCount = newUrgentTotalCount + oldUrgentTotalCount+saveUrgentTotalCount;

        if (hasVoice || hasMail) {
        	filter = new StateAttributesFilter();
            // Get unread voice message
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            newVoiceCount = userInbox.countStateFile(filter);

            // Get read voice messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_READ);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            oldVoiceCount = userInbox.countStateFile(filter);

            // Get read voice messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_SAVED);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            saveVoiceCount = userInbox.countStateFile(filter);

            // Get unread urgent voice message
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            newUrgentVoiceCount = userInbox.countStateFile(filter);

            // Get read urgent voice messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_READ);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            oldUrgentVoiceCount = userInbox.countStateFile(filter);

            // Get read urgent voice messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_SAVED);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            saveUrgentVoiceCount = userInbox.countStateFile(filter);

            // Get unread confidential voice message
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VOICE);
            StateFile[] stateFiles = CommonMessagingAccess.getMfs().getStateFiles(msa, filter);
            newConfidentialVoiceCount = 0;
            for( StateFile stateFile: stateFiles ) {
                // needed for backward compatibility with moip
                String confidentialValue = stateFile.getAttribute(MoipMessageEntities.CONFIDENTIALITY_HEADER);

                if (confidentialValue == null) {
                    // If not in C2, try search in C1 for backward compatibility
                    if (logger.isDebugEnabled()) logger.debug("confidentiality is not in C2. Try search in C1.");
                    confidentialValue = stateFile.getC1Attribute(Container1.Privacy);
                }

                if (confidentialValue != null) {
                    if (confidentialValue.equalsIgnoreCase(MoipMessageEntities.MFS_PRIVATE)) {
                        newConfidentialVoiceCount++;
                    }
                }
            }
            

        } else {
            newVoiceCount = 0;
            newUrgentVoiceCount = 0;
            oldVoiceCount = 0;
            oldUrgentVoiceCount = 0;
            saveVoiceCount = 0;
            saveUrgentVoiceCount = 0;
            newConfidentialVoiceCount = 0;
        }

        if (hasFax || hasMail) {
            // Get unread FAX message
        	filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.FAX);
            newFaxCount = userInbox.countStateFile(filter);

            // Get sent FAX messages
        	filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_READ);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.FAX);
            oldFaxCount = userInbox.countStateFile(filter);

            // Get sent FAX messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_SAVED);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.FAX);
            saveFaxCount = userInbox.countStateFile(filter);

            // Get unread urgent FAX message
        	filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.FAX);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            newUrgentFaxCount = userInbox.countStateFile(filter);

            // Get sent urgent FAX messages
        	filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_READ);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.FAX);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            oldUrgentFaxCount = userInbox.countStateFile(filter);

            // Get sent urgent FAX messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_SAVED);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.FAX);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            saveUrgentFaxCount = userInbox.countStateFile(filter);

        } else {
            newFaxCount = 0;
            newUrgentFaxCount = 0;
            oldFaxCount = 0;
            oldUrgentFaxCount = 0;
            saveFaxCount = 0;
            saveUrgentFaxCount = 0;
        }

        if (hasVideo || hasMail) {
        	// Get unread video message
        	filter = new StateAttributesFilter();
        	filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        	filter.setC1AttributeValue(Container1.Message_class,ServiceName.VIDEO);
        	newVideoCount = userInbox.countStateFile(filter);

        	// Get read video messages
        	filter = new StateAttributesFilter();
        	filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_READ);
        	filter.setC1AttributeValue(Container1.Message_class,ServiceName.VIDEO);
        	oldVideoCount = userInbox.countStateFile(filter);

            // Get read video messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_SAVED);
            filter.setC1AttributeValue(Container1.Message_class,ServiceName.VIDEO);
            saveVideoCount = userInbox.countStateFile(filter);

        	// Get unread video message
        	filter = new StateAttributesFilter();
        	filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        	filter.setC1AttributeValue(Container1.Message_class,ServiceName.VIDEO);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
        	newUrgentVideoCount = userInbox.countStateFile(filter);

        	// Get read video messages
        	filter = new StateAttributesFilter();
        	filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_READ);
        	filter.setC1AttributeValue(Container1.Message_class,ServiceName.VIDEO);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
        	oldUrgentVideoCount = userInbox.countStateFile(filter);

            // Get read video messages
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_SAVED);
            filter.setC1AttributeValue(Container1.Message_class,ServiceName.VIDEO);
            filter.setC1AttributeValue(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
            saveUrgentVideoCount = userInbox.countStateFile(filter);
            
            // Get unread confidential video message
            filter = new StateAttributesFilter();
            filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  MoipMessageEntities.MESSAGE_NEW);
            filter.setC1AttributeValue(Container1.Message_class, ServiceName.VIDEO);
            // needed for backward compatibility with moip
            StateFile[] stateFiles = CommonMessagingAccess.getMfs().getStateFiles(msa, filter);
            newConfidentialVideoCount = 0;
            for( StateFile stateFile: stateFiles ) {
                
                String confidentialValue = stateFile.getAttribute(MoipMessageEntities.CONFIDENTIALITY_HEADER);

                if (confidentialValue == null) {
                    // If not in C2, try search in C1 for backward compatibility
                    if (logger.isDebugEnabled()) logger.debug("confidentiality is not in C2. Try search in C1.");
                    confidentialValue = stateFile.getC1Attribute(Container1.Privacy);
                }

                if (confidentialValue != null) {
                    if (confidentialValue.equalsIgnoreCase(MoipMessageEntities.MFS_PRIVATE)) {
                        newConfidentialVideoCount++;
                    }
                }
            }
          

        } else {
        	newVideoCount = 0;
            newUrgentVideoCount = 0;
        	oldVideoCount = 0;
        	oldUrgentVideoCount = 0;
            saveVideoCount = 0;
            saveUrgentVideoCount = 0;
            newConfidentialVideoCount = 0;

        }

        if (hasMail) {
            newEmailCount = newTotalCount - newFaxCount - newVoiceCount - newVideoCount;
            newUrgentEmailCount = newUrgentTotalCount - newUrgentFaxCount - newUrgentVoiceCount - newUrgentVideoCount;
            oldEmailCount = oldTotalCount - oldFaxCount - oldVoiceCount - oldVideoCount;
            oldUrgentEmailCount = oldUrgentTotalCount - oldUrgentFaxCount - oldUrgentVoiceCount - oldUrgentVideoCount;
            saveEmailCount = saveTotalCount - saveFaxCount - saveVoiceCount - saveVideoCount;
            saveUrgentEmailCount = saveUrgentTotalCount - saveUrgentFaxCount - saveUrgentVoiceCount - saveUrgentVideoCount;

        } else {
            newEmailCount = 0;
            newUrgentEmailCount = 0;
            oldEmailCount = 0;
            oldUrgentEmailCount = 0;
            saveEmailCount = 0;
            saveUrgentEmailCount = 0;
        }
    }

    public MessageInfo getFirstNewMessageInfo() {

        Vector<StateAttributesFilter> filters = new Vector<StateAttributesFilter>();

        if (hasVoice || hasMail) {
            StateAttributesFilter stateAttributesFilter = new StateAttributesFilter();
            stateAttributesFilter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class), ServiceName.VOICE);
            stateAttributesFilter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
            filters.add(stateAttributesFilter);
        }

        if (hasVideo || hasMail) {
            StateAttributesFilter stateAttributesFilter = new StateAttributesFilter();
            stateAttributesFilter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class), ServiceName.VIDEO);
            stateAttributesFilter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
            filters.add(stateAttributesFilter);
        }

        if (hasFax || hasMail) {
            StateAttributesFilter stateAttributesFilter = new StateAttributesFilter();
            stateAttributesFilter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class), ServiceName.FAX);
            stateAttributesFilter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
            filters.add(stateAttributesFilter);
        }

        MessageInfo messageInfo = null;
        try {
            //stateFiles = CommonMessagingAccess.getInstance().searchStateFiles(msa, stateAttributesFilter);
            StateFile[] stateFiles = CommonMessagingAccess.getMfs().getStateFiles(msa, filters.toArray(new StateAttributesFilter[filters.size()]));
            if (stateFiles == null || stateFiles.length == 0) {
                return null;
            } else {
                messageInfo = new MessageInfo(stateFiles[0].omsa, stateFiles[0].rmsa, stateFiles[0].omsgid, stateFiles[0].rmsgid);
            }
        } catch (MsgStoreException mse) {
            logger.error("MsgStoreException for " + msa + " " + mse.getMessage(), mse);
        }
        return messageInfo;
    }
}
