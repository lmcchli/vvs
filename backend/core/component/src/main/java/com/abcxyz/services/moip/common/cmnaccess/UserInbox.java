package com.abcxyz.services.moip.common.cmnaccess;

import java.util.Vector;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.MFS;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

/**
 * Class handles a user's in-box querying operations. It allows to do a single MFS query with multiple filters for minimising
 * I/O. The query state files are kept internally, the further queries will be executed directly on these file objects.
 *
 * Step for querying:
 *  1) add all filters using API addFilter for state file attributes filtering
 *  2) add all filters using API addC1Filter for container 1  attributes filtering
 *  3) execute query to MFS using API queryMfs
 *  4) query one particular filter using countStateFile, countC1StateFile, or countStateFile with filter object
 *
 * @author lmchuzh
 *
 */
public class UserInbox {
	private MSA msa;
	private Vector<StateAttributesFilter> filters;
	private StateFile[] stateFiles;

	public UserInbox(MSA userMsa) {
		msa = userMsa;
		resetFileters();
	}

	public void resetFileters() {
		filters = new Vector<StateAttributesFilter>();
	}

	/**
	 * add filter key value
	 * @param key
	 * @param regValue
	 */
	public void addFilter(final String key, final String regValue) {
		StateAttributesFilter filter = new StateAttributesFilter();
		filter.setAttributeValue(key, regValue);

		filters.add(filter);
	}

	/**
	 *
	 * @param key C1 key name defined in {@link Containter1}
	 * @param regValue
	 */
	public void addC1Filter(final String c1Key, final String regValue) {

		StateAttributesFilter filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.getC1StateName(c1Key), regValue);

		filters.add(filter);

	}

	/**
	 * execute query using preset filters
	 */
	public void queryMfs() throws MsgStoreException{

		MFS mfs = CommonMessagingAccess.getMfs();
	    stateFiles = mfs.getStateFiles(msa, filters.toArray(new StateAttributesFilter[filters.size()])) ;
	}

	public int countStateFile(final String key, final String regValue) throws MsgStoreException{

		StateAttributesFilter filter = new StateAttributesFilter();
		filter.setAttributeValue(key, regValue);

		int count = countStateFile(filter);

		return count;
	}

	public int countC1StateFile(final String c1Key, final String regValue) throws MsgStoreException{

		String key = StateAttributes.getC1StateName(c1Key);
		StateAttributesFilter filter = new StateAttributesFilter();
		filter.setAttributeValue(key, regValue);

		int count = countStateFile(key, regValue);

		return count;
	}

	public int countStateFile(StateAttributesFilter filter) throws MsgStoreException{
		if (stateFiles == null) {
			queryMfs();
		}

		int count = 0;
        for (StateFile state: stateFiles) {
        	if (filter.isMatching(state)) {
        		count ++;
        	}
        }
		return count;
	}
	
    public static int getInventory(String recipient, String serviceName, String state) throws MsgStoreException{
        UserInbox userInbox = new UserInbox(new MSA(MfsEventManager.getMSID(recipient)));
        // Set Query attribute -- (all messages of type voice, video, fax, etc...)
        userInbox.addC1Filter(Container1.Message_class, serviceName);
        
        //Set the filter attributes for search (new, read, saved, etc...)
        StateAttributesFilter filter = new StateAttributesFilter();
        filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY,  state);

        return userInbox.countStateFile(filter);
        
    }
}
