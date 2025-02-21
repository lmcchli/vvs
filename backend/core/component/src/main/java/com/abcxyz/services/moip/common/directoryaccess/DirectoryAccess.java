/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2009
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 4-Mar-2009
*/
package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.lang.StackTraceElement;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.MCDException;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.mcd.exceptions.AuthenticationException;
import com.abcxyz.messaging.common.mcd.exceptions.DBUnavailableException;
import com.abcxyz.messaging.common.mcd.exceptions.ProfileNotFoundException;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.identityformatter.IdentityFormatter;
import com.abcxyz.messaging.identityformatter.IdentityFormatterInvalidIdentityException;
import com.abcxyz.messaging.mcd.proxy.MCDProxyService;
import com.abcxyz.messaging.mcd.proxy.MCDProxyServiceFactory;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.services.broadcastannouncement.BroadcastException;
import com.abcxyz.services.moip.broadcastannouncement.BroadcastManager;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributeHelperFactory;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributes;
import com.abcxyz.services.moip.common.complexattributes.IComplexAttributeHelper;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.abcxyz.services.moip.provisioning.validation.DataAccessDelegate;
import com.abcxyz.services.moip.provisioning.validation.InputValidator;
import com.abcxyz.services.moip.provisioning.validation.PrefetchedDataAccessDelegate;
import com.abcxyz.services.moip.provisioning.validation.ValidationException;
import com.abcxyz.services.moip.provisioning.validation.ValidationRequest;
import com.abcxyz.services.moip.provisioning.validation.ValidationRequestImpl;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;

public class DirectoryAccess implements IDirectoryAccess{

    private volatile static DirectoryAccess directoryAccess;

    private MCDProxyService mcdProxy;

    protected static final String SUBSCRIBER_PROFILE_CLASS = "subscriber";

    protected static final String MOIP_SERVICE_CLASS = "MOIP";

    protected static final String COS_IDENTITY_PREFIX = DAConstants.IDENTITY_PREFIX_COS;

    protected LogAgent logAgent;

    protected IdentityFormatter normalizationFormatter = null;

    public static DirectoryAccess getInstance() {
        if (directoryAccess == null) {
            synchronized(DirectoryAccess.class){
                if(directoryAccess == null) {
                    directoryAccess = new DirectoryAccess();
                }
            }
        }
        return directoryAccess;
    }

    protected DirectoryAccess() {
        getMcd();
        initIdentityFormatter();
    }

    /**
     * init McdProxyServiceImpl
     */
    private synchronized void getMcd() {

        try {

            CommonOamManager oamManager = CommonOamManager.getInstance();
            logAgent = oamManager.getMcdOam().getLogAgent();

            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.bindMcd attempt.");
            }

            //boolean doCaching = oamManager.getMcdOam().getConfigManager().getBooleanValue(MoipMessageEntities.McdCaching);
            boolean doCaching = true;
            mcdProxy = MCDProxyServiceFactory.getMCDProxyService(oamManager.getMcdOam(), doCaching, null) ;

            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.bindMcd successful.");
            }

        }
        catch(Exception e) {
            mcdProxy = null;
            logAgent.error("DirectoryAccess.bindMcd() Exception unable to bind mcd: " + e,e);
        }

    }



    private void initIdentityFormatter(){
        try {
            CommonOamManager oamManager = CommonOamManager.getInstance();
            if(logAgent.isDebugEnabled()) {
                logAgent = oamManager.getMcdOam().getLogAgent();
            }
            String cfgFile = System.getProperty("normalizationconfig", CommonMessagingAccess.NORMALIZATION_CONFIG_PATH);
            ConfigManager ruleFile = OEManager.getConfigManager(cfgFile, logAgent);
            normalizationFormatter = new IdentityFormatter(oamManager.getMfsOam(), ruleFile);
        }catch(Exception e){
            logAgent.error("DirectoryAccess.initIdentityFormatter() Exception unable to initialize identityformatter: " + e,e);
        }
    }

    protected Profile lookup(String profileClass, URI uri) {
        Profile profile = null;
        try {
            profile = lookup(profileClass, uri, false);
        } catch (AuthenticationException ae) {
            logAgent.debug("");
        } catch (DBUnavailableException dbue) {
            logAgent.debug("");
        } catch (MCDException mcd) {
            logAgent.debug("");
        } catch (Exception e) {
            logAgent.debug("");
        }
        return profile;
    }

    protected Profile lookup(String profileClass, URI uri, boolean throwException)
            throws AuthenticationException, DBUnavailableException, MCDException, Exception {

        if(mcdProxy == null) {
            getMcd();
        }

        Profile profile;

        try {
            profile =  mcdProxy.lookupProfile(profileClass, uri, "");
        } catch (AuthenticationException ae) {
            logAgent.error("DirectoryAccess.lookup: AuthenticationException " + ae.getMessage(),ae);
            if (throwException) {
                throw ae;
            }
            return null;
        } catch (DBUnavailableException dbue) {
            logAgent.error("DirectoryAccess.lookup: DBUnavailableException " + dbue.getMessage(),dbue);
            if (throwException) {
                throw dbue;
            }
            return null;
        } catch (ProfileNotFoundException mcd) {
            logAgent.debug("DirectoryAccess.lookup: ProfileNotFoundException " + mcd.getMessage());
            if (throwException) {
                throw mcd;
            }
            return null;
        } catch (MCDException mcd) {
            logAgent.debug("DirectoryAccess.lookup: MCDException " + mcd.getMessage(),mcd);
            if (throwException) {
                throw mcd;
            }
            return null;
        } catch (Exception e) {
            logAgent.debug("DirectoryAccess.lookup: Exception " + e.getMessage(),e);
            if (throwException) {
                throw e;
            }
            return null;
        }

        return profile;
    }

    /**
     * Lookup a BroadcastAnnouncement
     * @param baName The BA name
     * @return newly created BA, or null if not found.
     */
    public IDirectoryAccessBroadcastAnnouncement lookupBroadcastAnnouncement(String baName) {

        Profile baProfile = null;
        IDirectoryAccessBroadcastAnnouncement ba = null;

        if(logAgent.isDebugEnabled()) {
            logAgent.debug("DirectoryAccess.lookupBroadcastAnnouncement(): Finished creating BA for " + baName);
        }
        if (baName != null){
            try {
                baProfile = BroadcastManager.getInstance().getBroadcastAnnouncementProfile(baName);
                if (baProfile!= null){
                    ba = new DirectoryAccessBroadcastAnnouncement(baName, new MoipProfile(baProfile, logAgent), logAgent);
                }else {
                    if(logAgent.isDebugEnabled()) {
                        logAgent.debug("DirectoryAccess.lookupBroadcastAnnouncement(): lookup returned null container for baName " + baName);
                    }
                }
            }catch (BroadcastException e) {
                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("DirectoryAccess.lookupBroadcastAnnouncement(): URISyntaxException while obtaining multiline:" + e.getMessage());
                }
            }
        } else {
            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.lookupBroadcastAnnouncement(): ERROR - BA name passed in is null.");
            }
        }

        if(logAgent.isDebugEnabled()) {
            logAgent.debug("DirectoryAccess.lookupBroadcastAnnouncement(): Finished creating BA for " + baName);
        }
        return ba;
    }


    /*************************************** SUBSCRIBER *****************************************/

    public IDirectoryAccessSubscriber lookupSubscriber(String aSubscriber) {
        IDirectoryAccessSubscriber daSubscriber = null;
        try {
            daSubscriber = lookupSubscriber(aSubscriber, false);
        } catch (AuthenticationException ae) {
            logAgent.debug("lookupSubscriber AuthenticationException ", ae);
        } catch (DBUnavailableException dbue) {
            logAgent.debug("lookupSubscriber DBUnavailableException ", dbue);
        } catch (ProfileNotFoundException pnfe) {
            logAgent.debug("lookupSubscriber ProfileNotFoundException");
        } catch (MCDException mcde) {
            logAgent.debug("lookupSubscriber MCDException ", mcde);
        } catch (Exception e) {
            logAgent.debug("lookupSubscriber Exception ", e);
        }
        return daSubscriber;
    }

    public IDirectoryAccessSubscriber lookupSubscriber(String aSubscriber, boolean throwException)
            throws AuthenticationException, DBUnavailableException, MCDException, Exception {

        Object perf = null;

        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("DA.lookupSubscriber");
            }

            if(aSubscriber==null || aSubscriber.length() < 1){
                logAgent.debug("DirectoryAccess.lookupSubscriber: no subscriber provided");
                return null;
            }

            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.lookupSubscriber: looking up " + aSubscriber);
            }

            Profile subscriberProfile = null;
            try {
                if(aSubscriber.contains(":")){
                    subscriberProfile = this.lookup("subscriber", new URI(aSubscriber), throwException);
                }else if (aSubscriber.matches("\\+?\\d+?")) {
                    subscriberProfile = this.lookup("subscriber", new URI("tel:" + aSubscriber), throwException);
                }

                if(subscriberProfile == null){
                    if(logAgent.isDebugEnabled()) {
                        logAgent.debug("DirectoryAccess.lookupSubscriber: subscriber does not exist");
                    }

                    return null;
                }

                /** 
                 * Logic changed here (note by lmcantl)
                 * In order to support schema extensions, we will retrieve all attributes, and 
                 * look for those that finish with "CosIdentity".
                 * Then, for each of those, we will do a lookup for a profile of type "classofservice"
                 * with that identity, and merge each of them into the sub.
                 */
                MoipProfile cosProfile = buildMergedCosProfileFromAllCosReferredToBySub(subscriberProfile);
                MoipProfile moipSubscriberProfile = new MoipProfile(subscriberProfile, logAgent);
                MoipProfile multilineProfile = getMultilineProfile(aSubscriber, moipSubscriberProfile, cosProfile);

                DirectoryAccessSubscriber DAsubscriber = new DirectoryAccessSubscriber(moipSubscriberProfile, cosProfile, multilineProfile, logAgent);
                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("DirectoryAccess.lookupSubscriber: obtained subscriber " + DAsubscriber.getSubscriberProfile().toString() + DAsubscriber.getCosProfile().toString());
                }
                if(multilineProfile != null) {
                    if(logAgent.isDebugEnabled()) {
                        logAgent.debug("DirectoryAccess.lookupSubscriber: obtained multiline subscriber " + DAsubscriber.getMultilineProfile().toString());
                    }
                }
                return DAsubscriber;
            } catch (URISyntaxException e) {
                logAgent.info("DirectoryAccess.lookupSubscriber ",e);
            }
        } finally {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
        return null;
    }

    private MoipProfile buildMergedCosProfileFromAllCosReferredToBySub(Profile subscriberProfile){
        MoipProfile cosProfile = null;
        KeyValues[] allAttributes = subscriberProfile.getAttributes();
        ArrayList<Profile> allCosProfiles = new ArrayList<Profile>();
        for (KeyValues eachKVPair : allAttributes){
            String attributeName = eachKVPair.getKey();
            if (attributeName.toUpperCase().endsWith("CosIdentity".toUpperCase())){
                // Found a cos identity
                // Now do the lookup
                String allCosValues[] = eachKVPair.getValues();
                String cosIdentity = allCosValues[0];
                if (cosIdentity != null && cosIdentity.trim().length() > 0) {
                    Profile localCosProfile;
                    try {
                        localCosProfile = mcdProxy.lookupProfile(MCDConstants.PROFILECLASS_CLASSOFSERVICE, new URI(cosIdentity),"");
                        allCosProfiles.add(localCosProfile);
                    }
                    catch (MCDException e) {
                        e.printStackTrace();
                    }
                    catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        if(allCosProfiles.size() == 0){
            cosProfile = new MoipProfile(null);
        }
        else {
            // Now build a MoipProfile that combines all the cos we read
            Profile mergedProfile = mergeProfiles(allCosProfiles);
            cosProfile = new MoipProfile(mergedProfile, logAgent);
        }
        return cosProfile;
    }

    private Profile mergeProfiles(ArrayList<Profile> allCosProfiles){
        Profile mergedProfile = new ProfileContainer();
        Iterator<Profile> profileIterator = allCosProfiles.iterator();
        while (profileIterator.hasNext()){
            Profile currentProfile = profileIterator.next();
            URI allIdentities[] = currentProfile.getIdentities();
            for (URI eachURI : allIdentities){
                mergedProfile.addIdentity(eachURI);
            }
            KeyValues[] allAttributes = currentProfile.getAttributes();
            for (KeyValues eachAttribute : allAttributes){
                String attributeName = eachAttribute.getKey();
                String values[] = eachAttribute.getValues();
                for (String eachValue : values){
                    mergedProfile.addAttributeValue(attributeName, eachValue);
                }
            }
        }
        return mergedProfile;
    }

    /**
	private Profile lookupSubscriber(String subscriberIdentity) {

		Profile subscriber;

		try {
			URI uri = getSubscriberURI(subscriberIdentity);
			if(logAgent.isDebugEnabled()) {
				logAgent.debug("DirectoryAccess.lookupSubscriber with URI " + uri.toString());
			}
			subscriber = lookup(SUBSCRIBER_PROFILE_CLASS, uri);

		} catch(DirectoryAccessException dae){
			if(logAgent.isDebugEnabled()) {
				logAgent.debug("DirectoryAccess.lookupSubscriber: DirectoryAccessException " + dae.getMessage());
			}
			return null;
		}

		return subscriber;
	}
     */


    public void updateSubscriber(IDirectoryAccessSubscriber subscriber, String attrName, String attrValue) throws DirectoryAccessException {
        String [] attrValues = new String[1];
        attrValues[0] = attrValue;
        updateSubscriber(subscriber, attrName, attrValues);
    }

    public void updateSubscriber(IDirectoryAccessSubscriber subscriber, String aName, String[] attrValues) throws DirectoryAccessException {
        //keep backwards compatibility, by default replace
        updateSubscriber(subscriber, aName, attrValues, Modification.Operation.REPLACE);
    }

    public void updateSubscriber(IDirectoryAccessSubscriber subscriber, String aName, String [] attrValues, Modification.Operation op) throws DirectoryAccessException {
        Object perf = null;
        String profileClass;
        String scheme;
        URI identity = null;

        try{

            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("DA.updateSubscriber");
            }

            if(mcdProxy == null){
                getMcd();
            }

            String attrName = AttributeNameMapper.getInstance().map(aName);

            boolean isMultiline;
            URI[] identities;
            if(DirectoryAccessSubscriber.hasMultilineService(subscriber.getCosProfile()) && DAConstants.multilineAttributes.contains(attrName)
                    && subscriber.getMultilineProfile() != null){
                isMultiline = true;
                scheme = MCDConstants.IDENTITY_SCHEME_MULTILINE_TEL;
                profileClass = MCDConstants.PROFILECLASS_MULTILINE;
                identities = subscriber.getMultilineProfile().getIdentities(scheme);
                if ((identities != null) && (identities.length >= 1)) {
                    identity = identities[0];
                }
            }
            else {
                isMultiline = false;
                scheme = MCDConstants.IDENTITY_SCHEME_TEL;
                profileClass = MCDConstants.PROFILECLASS_SUBSCRIBER;
                identities = subscriber.getSubscriberProfile().getIdentities(scheme);
                if ((identities != null) && (identities.length >= 1)) {
                    identity = identities[0];
                }
            }

            ValidationRequest validationRequest = constructValidationRequest(subscriber, attrName, attrValues, isMultiline);

            if (identity == null) {
                logAgent.error("DirectoryAccess.updateSubscriber - can not find identity");
                throw new DirectoryAccessException("can not find identity");
            }

            try
            {
                //COMPLEX ATTRIBUTE
                //if it is a complex attribute, we need to split up the attribute into simple attributes
                if(ComplexAttributes.members.containsKey(attrName)){

                    // MOIPTmpGrtHelper handles null attrValues and must be invoked
                    if (attrValues == null && !attrName.equalsIgnoreCase(DAConstants.ATTR_TMP_GRT)) {
                        Modification [] moList = new Modification[1];
                        KeyValues kv = new KeyValues(attrName, attrValues);
                        Modification mo = new Modification(Modification.Operation.REMOVE, kv);
                        moList[0] = mo;
                        // the complex attribute will be removed, that is no value at all
                        mcdProxy.updateProfileAttributes(profileClass, identity, moList);
                    } else {
                        IComplexAttributeHelper helper = ComplexAttributeHelperFactory.getInstance().createHelper(attrName);
                        List<HashMap<String, String[]>> list = helper.disassembleComplexAttribute(attrValues);
                        if(list != null) {
                            validationRequest.removeAttribute(attrName);
                            ArrayList<Modification> modificationList = new ArrayList<Modification>();
                            for(HashMap<String, String[]> simpleAttributes: list) {

                                if(simpleAttributes == null){
                                    throw new DirectoryAccessException("Unable to split into simple attributes");
                                }

                                Iterator<String> iter = simpleAttributes.keySet().iterator();

                                while(iter.hasNext()) {
                                    String key = iter.next();
                                    String[] values = simpleAttributes.get(key);

                                    if (values != null) {
                                        KeyValues kv = new KeyValues(key, values);

                                        for(String v: values){
                                            validationRequest.addAttributeValue(key, v);
                                        }

                                        Modification mo;
                                        if (values != null) {
                                            mo = new Modification(op, kv);
                                        } else {
                                            mo = new Modification(Modification.Operation.REMOVE, kv);
                                        }
                                        modificationList.add(mo);
                                    }
                                }
                            }

                            OAMManager oam = CommonOamManager.getInstance().getMcdOam();
                            InputValidator.getInstance(oam).validateAttributes(validationRequest, false);

                            Modification[] modifs = new Modification[modificationList.size()];
                            modifs = modificationList.toArray(modifs);
                            mcdProxy.updateProfileAttributes(profileClass, identity, modifs);
                        } //there is no else; if list = null than the complex attribute couldn't be disassembled and in this case we don't change the attribute
                    }

                } else {

                    //SIMPLE ATTRIBUTE

                    OAMManager oam = CommonOamManager.getInstance().getMcdOam();
                    InputValidator.getInstance(oam).validateAttributes(validationRequest, false);

                    Modification [] moList = new Modification[1];
                    KeyValues kv = new KeyValues(attrName, attrValues);
                    Modification mo = null;
                    if(attrValues != null) {
                        // the attribute values are okay. Lets update them in MCD
                        mo = new Modification (op, kv);
                    }else {
                        mo = new Modification(Modification.Operation.REMOVE, kv);

                    }

                    moList[0] = mo;
                    mcdProxy.updateProfileAttributes(profileClass, identity, moList);

                }

                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("DirectoryAccess.updateSubscriber - done " + identity.toString());
                }
            }
            catch (ValidationException ve) {
                throw new DirectoryAccessException(ve.getMessage(), ve.getCause());
            }catch(AuthenticationException ae){
                logAgent.error("DirectoryAccess.updateSubscriber: AuthenticationException " + ae.getMessage(),ae);
            }catch(DBUnavailableException dbue){
                logAgent.error("DirectoryAccess.updateSubscriber: DBUnavailableException " + dbue.getMessage(),dbue);
            }catch(MCDException mcd){
                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("DirectoryAccess.updateSubscriber: MCDException " + mcd.getMessage(),mcd);
                }
                throw new DirectoryAccessException(mcd.getMessage(), mcd.getCause());
            }catch(Exception e){
                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("DirectoryAccess.updateSubscriber: Exception " + e.getMessage(),e);
                }
                throw new DirectoryAccessException(e.getMessage(), e.getCause());
            }
        } finally {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
    }


    public boolean removeFromCache(String profileClass, String identity) {
        Object perf = null;
        boolean result = false;

        try {

            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("DA.removeFromCache");
            }

            if(identity==null || identity.isEmpty() || profileClass == null || profileClass.isEmpty()){
                logAgent.debug("DirectoryAccess.removeFromCache: no identity provided");
            } else {
                if(logAgent.isDebugEnabled()) {
                    logAgent.debug("DirectoryAccess.removeFromCache: removing " + profileClass + " with identity " + identity + " from cache.");
                }

                if(mcdProxy == null){
                    getMcd();
                }

                if(identity.contains(":")){
                    mcdProxy.updateProfileAttributes(profileClass, URI.create(identity), null);
                }else{
                    mcdProxy.updateProfileAttributes(profileClass, URI.create("tel:" + identity), null);
                }
                result = true;
            }

        } catch (MCDException mcd) {
            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.removeFromCache: MCDException " + mcd.getMessage(), mcd);
            }

        } finally {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
        return result;
    }


    /**
     * Construct a validationRequest, used to validate the attribute values
     * @param subscriber
     * @param attrName
     * @param attrValues
     * @param isMultiline
     * @return
     * @throws DirectoryAccessException
     */
    private ValidationRequest constructValidationRequest(IDirectoryAccessSubscriber subscriber, String attrName, String [] attrValues, boolean isMultiline) throws DirectoryAccessException {

        String scheme;
        String profileClass;
        Profile profile;

        // validate the new values via Business Rules
        Profile updateAttrProfile = new ProfileContainer();
        URI[] identityArray = subscriber.getSubscriberProfile().getIdentities(MCDConstants.IDENTITY_SCHEME_TEL);
        for (int i=0; i < identityArray.length; i++){
            updateAttrProfile.addIdentity(identityArray[i]);
        }

        if(attrValues != null){
            for (String v : attrValues) {
                updateAttrProfile.addAttributeValue(attrName, v);
            }
        }

        //Construct combined profile for PrefetchedDataAccessDelegate
        if(isMultiline){
            //multiline
            scheme = MCDConstants.IDENTITY_SCHEME_MULTILINE_TEL;
            profileClass = MCDConstants.PROFILECLASS_MULTILINE;

            profile = combineProfiles(subscriber.getMultilineProfile().getProfile(), subscriber.getSubscriberProfile().getProfile());

        } else {
            //non-multiline
            scheme = MCDConstants.IDENTITY_SCHEME_TEL;
            profileClass = MCDConstants.PROFILECLASS_SUBSCRIBER;
            profile = subscriber.getSubscriberProfile().getProfile();
        }


        Profile cosProfile = subscriber.getCosProfile().getProfile();
        URI[] cosIdentityArray = cosProfile.getIdentities();
        for (int i=0; i < cosIdentityArray.length; i++){
            profile.addIdentity(cosIdentityArray[i]);
        }
        profile = combineProfiles(profile, cosProfile);


        DataAccessDelegate dataAccessDelegate = new PrefetchedDataAccessDelegate(profile.getIdentities(scheme)[0].toString(), profile);
        return new ValidationRequestImpl(ValidationRequest.Operation.SET, profileClass, updateAttrProfile, dataAccessDelegate);

    }


    /**
     * Utility method to combine attributes from 2 profiles. If an attribute is present
     * in both profile, the container pc1 attribute is retained.
     * @param pc1
     * @param pc2
     * @return an aggregated profile
     */
    private Profile combineProfiles(Profile pc1, Profile pc2) {

        Iterator<String> itr = pc2.attributeIterator();
        while(itr.hasNext()){
            String attr = itr.next();
            if(!pc1.hasAttribute(attr)){
                List<String> values = pc2.getAttributeValues(attr);
                for(String v: values){
                    pc1.addAttributeValue(attr, v);
                }
            }
        }
        return pc1;
    }




    private URI getSubscriberURI(String subscriberIdentity) throws DirectoryAccessException {
        if(logAgent.isDebugEnabled()) {
            logAgent.debug("DirectoryAccess.getSubscriberURI: " + subscriberIdentity);
        }
        String tmp;

        try {
            if(subscriberIdentity.contains(":")){
                tmp = subscriberIdentity;
            }
            else if(subscriberIdentity.contains("@")){
                tmp = MCDConstants.IDENTITY_SCHEME_MAILTO + ":" + subscriberIdentity;
            }
            else {
                tmp = MCDConstants.IDENTITY_SCHEME_TEL + ":" + subscriberIdentity;
            }

            String formattedIdentity = formatIdentity(tmp);

            return new URI(formattedIdentity);
        }
        catch (URISyntaxException e) {
            throw new DirectoryAccessException("URI could not be constructed: "+subscriberIdentity + " "+e.getMessage());
        }
    }





    /*************************************** COS *********************************************/

    public MoipProfile lookupCos(String cosIdentity) {

        if(logAgent.isDebugEnabled()) {
            logAgent.debug("DirectoryAccess.lookupCos: " + cosIdentity);
        }
        MoipProfile cosProfile = null;

        try {
            URI uri = getCosURI(cosIdentity);
            Profile cos = lookup(MCDConstants.PROFILECLASS_CLASSOFSERVICE, uri);
            if(cos != null) {
                cosProfile = new MoipProfile(cos, logAgent);
            }
        } catch (MCDException e) {
            logAgent.warn("DirectoryAccess.lookupCos: Exception " + e.getMessage());

        }

        return cosProfile;

    }

    private URI getCosURI(String cosIdentity) throws MCDException {
        try {
            if(cosIdentity.startsWith(COS_IDENTITY_PREFIX)){
                return new URI(cosIdentity);
            }
            return new URI(COS_IDENTITY_PREFIX + cosIdentity);
        } catch (URISyntaxException e) {

            throw new MCDException("URI could not be constructed  "+cosIdentity +" "+e.getMessage());
        }
    }


    /*************************************** MULTILINE *********************************************/

    private MoipProfile getMultilineProfile(String subscriber, MoipProfile subscriberProfile, MoipProfile cosProfile) {

        if(DirectoryAccessSubscriber.hasMultilineService(cosProfile)){

            Profile p = lookupMultiline(subscriber);
            if(p != null){
                return new MoipProfile(p, logAgent);
            }

        }
        return null;
    }


    private Profile lookupMultiline(String subscriberIdentity) {

        try {
            return lookup(MCDConstants.PROFILECLASS_MULTILINE, getMultilineURI(subscriberIdentity));
        } catch (DirectoryAccessException e) {

            logAgent.debug("DirectoryAccess.lookupMultiline: exception while obtaining multiline:" + e.getMessage());
        }
        return null;

    }

    private URI getMultilineURI(String subscriberIdentity) throws DirectoryAccessException {
        if(logAgent.isDebugEnabled()) {
            logAgent.debug("DirectoryAccess.getMultilineURI: " + subscriberIdentity);
        }
        URI telURI = getSubscriberURI(subscriberIdentity);
        if(telURI == null) {
            return null;
        }

        String teluri = telURI.toString();

        String multilineuri = teluri.replace(MCDConstants.IDENTITY_SCHEME_TEL, MCDConstants.IDENTITY_SCHEME_MULTILINE_TEL);

        try {
            return new URI(multilineuri);
        }
        catch (URISyntaxException e) {
            throw new DirectoryAccessException("URI could not be constructed");
        }

    }

    private String formatIdentity(String identity) throws DirectoryAccessException{

        String formattedIdentity = null;
        try {
            formattedIdentity = normalizationFormatter.formatIdentity(identity);
        } catch (IdentityFormatterInvalidIdentityException e) {
            throw new DirectoryAccessException("DirectoryAccess.formatIdentity(): normalization exception: " + e.getMessage());
        }


        if (formattedIdentity == null) {
            /**
             * not able to format, put the original address
             */
            formattedIdentity = identity;
            if(logAgent.isDebugEnabled()) {
                logAgent.debug("DirectoryAccess.formatIdentity(): not able to normalize address: " + identity);
            }
        }

        if(logAgent.isDebugEnabled()) {
            logAgent.debug("DirectoryAccess.formatIdentity(): final result address: " + identity + " is normalized to: " + formattedIdentity);
        }

        return formattedIdentity;
    }


    public boolean isProfileUpdatePossible() {
        if(mcdProxy == null) {
            getMcd();
        }

        return mcdProxy.isUpdateOperationAvailable();
    }

    /**
     * This method should only be used by unit tests, to pass in a mock mcdproxy object
     */
    public void setMcdProxy(MCDProxyService aProxy){
        mcdProxy = aProxy;
    }

    /*************************************** MAIN *********************************************/
    public static void main(String[] args) {

        Collection<String> configFilenames = new LinkedList<String>();

        String curDir = System.getProperty("user.dir");
        String masFile = "";
        String backendFile = "";
        String mcrFilename = "";
        String normalizationFile = "";
        if (curDir.endsWith("backend") == false ) {
            backendFile = curDir +  "/../ipms_sys2/backend/cfg/backend.conf";
            mcrFilename = curDir + "/../ipms_sys2/backend/cfg/componentservices.cfg";
            normalizationFile = curDir + "/../ipms_sys2/backend/cfg/formattingRules.conf";
            masFile = curDir + "/../mas/cfg/mas.xml";
        }
        else {
            backendFile = curDir +  "/cfg/backend.conf";
            mcrFilename = curDir + "/cfg/componentservices.cfg";
            normalizationFile = curDir + "/cfg/formattingRules.conf";
            masFile = curDir.substring(0, curDir.indexOf("ipms_sys")) + "mas/cfg/mas.xml";
        }

        System.setProperty("componentservicesconfig", mcrFilename);
        System.setProperty("normalizationconfig", normalizationFile);

        configFilenames.add(masFile);
        configFilenames.add(backendFile);

        IConfiguration configuration = null;
        try{
            configuration = new ConfigurationImpl(null,configFilenames,false);
        }catch(Exception e) {
            System.out.println("Config could not be read");
            System.exit(0);
        }
        // in order to load the mcd extra data into the configuration
        CommonMessagingAccess.getInstance().setConfiguration( configuration);

        System.out.println("Running directory access");

        DirectoryAccess da = DirectoryAccess.getInstance();
        DirectoryAccessSubscriber subscriber = (DirectoryAccessSubscriber) da.lookupSubscriber("56781");
        //da.lookupSubscriber("56781");
        //da.lookupSubscriber("56782");


        try {
            //da.updateSubscriber(subscriber, DAConstants.ATTR_PIN, "1234");
            //da.updateSubscriber(subscriber, DAConstants.ATTR_IN_HOURS_START, "0600");
            da.updateSubscriber(subscriber, DAConstants.ATTR_AUTOPLAY, "yes");
        }
        catch (DirectoryAccessException dae) {
            dae.printStackTrace(System.out);
        }

        /**
         * Retrieve subscriber MOIPPin attributes
         */
        /*		subscriber = (DirectoryAccessSubscriber) da.lookupSubscriber("43645");
		System.out.println("Final MOIPPin decoded: " + subscriber.getStringAttributes(DAConstants.ATTR_PIN)[0]);
		System.out.println("Final MOIPInHoursStart: " + subscriber.getStringAttributes(DAConstants.ATTR_IN_HOURS_START)[0]);
		System.out.println("Final Tmpgrt: " + subscriber.getStringAttributes(DAConstants.ATTR_TMP_GRT)[0]);*/
    }
}
