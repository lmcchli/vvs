/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.mobeon.masp.execution_engine.platformaccess.util.GreetingTypeUtil;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.IMultimediaDistributionList;
import com.mobeon.masp.profilemanager.IProfile;
import com.mobeon.masp.profilemanager.IProfileManager;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.VmSubscriber;
import com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException;

/**
 * Takes care of the subscriber profile related functions in the PlatformAccess interface.
 * Uses the IProfileManager interface to retrieve correct IProfiles.
 * <p/>
 * An PlatformAccessException will be thrown when some error occurs either in the IProfile interface or if invalid data
 * is sent into the functions.
 *
 * @author ermmaha
 */
public class SubscriberProfileManager {
    public static final String MAIL = "mail";
    public static final String MAILHOST = "mailhost";

    /**
     * logger
     */
    private static ILogger log = ILoggerFactory.getILogger(SubscriberProfileManager.class);
    /**
     * object for accessing the Profile
     */
    private IProfileManager iProfileManager;


    /**
     * Constructor
     *
     * @param iProfileManager
     */
    public SubscriberProfileManager(IProfileManager iProfileManager) {
        this.iProfileManager = iProfileManager;
    }

    /**
     * Retrieves the Mailbox for a subscriber
     *
     * @param phoneNumber
     * @return the subscriber's Mailbox object
     * @throws PlatformAccessException If some error occured in the IProfile interface.
     */
    public IMailbox subscriberGetMailbox(String phoneNumber) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            Exception ex;
            try {
                return iProfile.getMailbox();
            } catch (HostException e) {
                ex = e;
            }
            if (ex != null) {
                throw new PlatformAccessException(EventType.MAILBOX, "subscriberGetMailbox:phoneNumber=" + phoneNumber, ex);
            }
        }
        return null;
    }

    /**
     * Checks if a subscriber specified with phonenumber exist in the system
     *
     * @param phoneNumber
     * @return true if the subscriber exists, false if not (or if some error)
     */
    public boolean subscriberExist(String phoneNumber) {
        VmSubscriber subscriber=null;

        subscriber=(VmSubscriber) getProfile(phoneNumber);

        return ( subscriber != null && subscriber.getSubscriber().hasVoiceMailService() );
    }

    /**
     * Retrieves a String attribute from the subscriber's profile.
     *
     * @param phoneNumber
     * @param attrName
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber
     */
    public String[] subscriberGetStringAttribute(String phoneNumber, String attrName) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetStringAttribute, phoneNumber=" + phoneNumber + ", value for " + attrName +
                            " " + Arrays.toString(iProfile.getStringAttributes(attrName)));
                }
                return iProfile.getStringAttributes(attrName);
            } catch (UnknownAttributeException e) {
                throw new PlatformAccessException(
                        EventType.DATANOTFOUND, "subscriberGetStringAttribute:attrName=" + attrName, e);
            }
        }
        throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberGetStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Retrieves a the list of identities associated to the subscriber's profile.
     *
     * @param phoneNumber
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occured in the IProfile interface or invalid phoneNumber
     */
    public String[] subscriberGetIdentitiesAsString(String phoneNumber, String scheme) throws PlatformAccessException{
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetIdentitiesAsString, phoneNumber=" + phoneNumber);
                }
                return iProfile.getIdentities(scheme);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.DATANOTFOUND, "subscriberGetIdentitiesAsString:phoneNumber=" + phoneNumber, e);
            }
        }
        throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberGetStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }


    /**
     * Retrieves a integer attribute from the subscriber's profile
     *
     * @param phoneNumber
     * @param attrName
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber
     */
    public int[] subscriberGetIntegerAttribute(String phoneNumber, String attrName) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetIntegerAttribute, phoneNumber=" + phoneNumber + ", value for " + attrName +
                            " " + Arrays.toString(iProfile.getIntegerAttributes(attrName)));
                }
                return iProfile.getIntegerAttributes(attrName);
            } catch (UnknownAttributeException e) {
                throw new PlatformAccessException(
                        EventType.DATANOTFOUND, "subscriberGetIntegerAttribute:attrName=" + attrName, e);
            }
        }
        throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberGetIntegerAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Retrieves a boolean attribute from the subscriber's profile
     *
     * @param phoneNumber
     * @param attrName
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber
     */
    public boolean[] subscriberGetBooleanAttribute(String phoneNumber, String attrName) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In getBooleanAttributes, phoneNumber=" + phoneNumber + ", value for " + attrName +
                            " " + Arrays.toString(iProfile.getBooleanAttributes(attrName)));
                }
                return iProfile.getBooleanAttributes(attrName);
            } catch (UnknownAttributeException e) {
                throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberGetBooleanAttribute:attrName=" + attrName, e);
            }
        }
        throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberGetBooleanAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Sets a string attribute in the subscriber's profile
     *
     * @param phoneNumber
     * @param attrName
     * @param attrValues  with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber.
     */
    public void subscriberSetStringAttribute(String phoneNumber, String attrName, String[] attrValues) {
    	subscriberSetStringAttribute(phoneNumber, attrName, attrValues, Modification.Operation.REPLACE);
    }

    public void subscriberSetStringAttribute(String phoneNumber, String attrName, String[] attrValues, Modification.Operation op) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberSetStringAttribute, phoneNumber=" + phoneNumber + ", value for " + attrName +
                            " " + Arrays.toString(attrValues));
                }
                iProfile.setStringAttributes(attrName, attrValues, op);
                return;
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.PROFILEWRITE, "subscriberSetStringAttribute:attrName=" + attrName, e);
            }
        }
        throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberSetStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }


    /**
     * Sets a integer attribute in the subscriber's profile
     *
     * @param phoneNumber
     * @param attrName
     * @param attrValues  with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber
     */
    public void subscriberSetIntegerAttribute(String phoneNumber, String attrName, int[] attrValues) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberSetIntegerAttribute, phoneNumber=" + phoneNumber + ", value for " + attrName +
                            " " + Arrays.toString(attrValues));
                }
                iProfile.setIntegerAttributes(attrName, attrValues);
                return;
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.PROFILEWRITE, "subscriberSetIntegerAttribute:attrName=" + attrName, e);
            }
        }
        throw new PlatformAccessException(
                EventType.DATANOTFOUND, "subscriberSetIntegerAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Sets a boolean attribute in the subscriber's profile
     *
     * @param phoneNumber
     * @param attrName
     * @param attrValues  with the values, if single value the first element is used.
     */
    public void subscriberSetBooleanAttribute(String phoneNumber, String attrName, boolean[] attrValues) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberSetBooleanAttribute, phoneNumber=" + phoneNumber + ", value for " + attrName +
                            " " + Arrays.toString(attrValues));
                }
                iProfile.setBooleanAttributes(attrName, attrValues);
                return;
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.PROFILEWRITE, "subscriberSetBooleanAttribute:attrName=" + attrName, e);
            }
        }
        throw new PlatformAccessException(
                EventType.DATANOTFOUND, "subscriberSetBooleanAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Retrieves a String attribute from the subscriber's COS
     *
     * @param phoneNumber for the subscriber
     * @param attrName    name of the attribute
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occured in the IProfile interface or invalid phoneNumber
     */
    public String[] subscriberGetCosStringAttribute(String phoneNumber, String attrName) {
    	throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetCosStringAttribute is unimplemented");

    }

    /**
     * Retrieves a integer attribute from the subscriber's COS
     *
     * @param phoneNumber for the subscriber
     * @param attrName    name of the attribute
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber
     */
    public int[] subscriberGetCosIntegerAttribute(String phoneNumber, String attrName) {
    	throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetCosIntegerAttribute is unimplemented");

    }

    /**
     * Retrieves a boolean attribute from the subscriber's COS
     *
     * @param phoneNumber for the subscriber
     * @param attrName    name of the attribute
     * @return Array with the values, if single value the first element is used.
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber
     */
    public boolean[] subscriberGetCosBooleanAttribute(String phoneNumber, String attrName) {
    	throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetCosBooleanAttribute is unimplemented");
    }


    /**
     * Retrieves the greeting for a subscriber
     *
     * @param phoneNumber  for the subscriber
     * @param greetingType the type of greeting, can be one of "allcalls", "cdg", "temporary", "noAnswer", "busy",
     *                     "outOfHours", "ExternalAbsence".
     * @param mediaType    type of media, can be one of "voice", "video"
     * @param cdgNumber    called dependent number. Only valid if greetingType="cdg"
     * @return value       specified greeting
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber.
     */
    public IMediaObject subscriberGetGreeting(String phoneNumber, String greetingType, String mediaType,
                                              String cdgNumber) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetGreeting, phoneNumber=" + phoneNumber + ", greetingType=" + greetingType +
                            ", mediaType=" + mediaType + ", cdgNumber=" + cdgNumber);
                }
                return iProfile.getGreeting(
                        GreetingTypeUtil.getGreetingSpecification(greetingType, mediaType, cdgNumber, null));
            } catch (IllegalArgumentException e) {
                //some error with the format on the greetingType or mediaType parameter
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "subscriberGetGreeting:greetingType=" + greetingType, e);
            } catch (ProfileManagerException e) {
                if (e instanceof GreetingNotFoundException) {
                    throw new PlatformAccessException(
                            EventType.DATANOTFOUND, "subscriberGetGreeting:greetingType=" + greetingType, e);
                }
                throw new PlatformAccessException(
                        EventType.MAILBOX, "subscriberGetGreeting:greetingType=" + greetingType, e);
            }
        }
        throw new PlatformAccessException(
                EventType.DATANOTFOUND, "subscriberGetGreeting:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Stores the specified greeting.
     *
     * @param phoneNumber  the phone number of the subscriber
     * @param greetingType the type of greeting, can be one of "allcalls", "cdg", "temporary", "noAnswer", "busy",
     *                     "outOfHours", "ExternalAbsence".
     * @param mediaType    the type of media, can be one of "voice", "video".
     * @param cdgNumber    the called dependent number. Only valid if greetingType="cdg".
     * @param greeting     the greeting, if null the greeting is deleted
     * @param duration     duration of the greeting
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber.
     */
    public void subscriberSetGreeting(String phoneNumber, String greetingType, String mediaType, String cdgNumber,
                                      IMediaObject greeting, String duration) {

        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberSetGreeting, phoneNumber=" + phoneNumber + ", greetingType=" + greetingType +
                            ", mediaType=" + mediaType + ", cdgNumber=" + cdgNumber);
                }
                iProfile.setGreeting(
                        GreetingTypeUtil.getGreetingSpecification(greetingType, mediaType, cdgNumber, duration), greeting);
                return;
            } catch (IllegalArgumentException e) {
                //some error with the format on the greetingType or mediaType parameter
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "subscriberSetGreeting:greetingType=" + greetingType, e);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.MAILBOX, "subscriberSetGreeting:greetingType=" + greetingType, e);
            }
        }
        throw new PlatformAccessException(
                EventType.DATANOTFOUND, "subscriberSetGreeting:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Retrieves the spokenname for a subscriber
     *
     * @param phoneNumber for the subscriber
     * @param mediaType   the type of media, can be one of "voice", "video"
     * @return value      the specified spokenname
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber.
     */
    public IMediaObject subscriberGetSpokenName(String phoneNumber, String mediaType) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetSpokenName, phoneNumber=" + phoneNumber + ", mediaType=" + mediaType);
                }
                return iProfile.getSpokenName(GreetingTypeUtil.getGreetingFormat(mediaType));
            } catch (IllegalArgumentException e) {
                // some error with the format on the mediaType parameter
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "subscriberGetSpokenName:mediaType=" + mediaType, e);
            } catch (ProfileManagerException e) {
                if (e instanceof GreetingNotFoundException) {
                    throw new PlatformAccessException(
                            EventType.DATANOTFOUND, "subscriberGetSpokenName:mediaType=" + mediaType, e);
                }
                throw new PlatformAccessException(
                        EventType.MAILBOX, "subscriberGetSpokenName:mediaType=" + mediaType, e);
            }
        }
        throw new PlatformAccessException(
                EventType.DATANOTFOUND, "subscriberGetSpokenName:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Stores the specified spokenname.
     *
     * @param phoneNumber the phone number of the subscriber
     * @param mediaType   the type of media, can be one of "voice", "video".
     * @param spokenName  the spokenname, if null the spokenname is deleted
     * @throws PlatformAccessException If some error occurred in the IProfile interface or invalid phoneNumber.
     */
    public void subscriberSetSpokenName(String phoneNumber, String mediaType, IMediaObject spokenName, String duration) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberSetSpokenName, phoneNumber=" + phoneNumber + ", mediaType=" + mediaType);
                }
                iProfile.setSpokenName(GreetingTypeUtil.getGreetingFormat(mediaType), spokenName, duration);
                return;
            } catch (IllegalArgumentException e) {
                // some error with the format on the mediaType parameter
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "subscriberSetSpokenName:mediaType=" + mediaType, e);
            } catch (ProfileManagerException e) {
                if (log.isDebugEnabled()) log.debug("Exception in subscriberSetSpokenName " + e);
                throw new PlatformAccessException(
                        EventType.MAILBOX, "subscriberSetSpokenName:mediaType=" + mediaType, e);
            }
        }
        throw new PlatformAccessException(
                EventType.DATANOTFOUND, "subscriberSetSpokenName:phoneNumber=" + phoneNumber, "phoneNumber not found");
    }

    /**
     * Retrieves a list of DistributionList ids for a subscriber with the specified telephonenumber.
     *
     * @param phoneNumber
     * @return list of ids
     */
    public String[] subscriberGetDistributionListIds(String phoneNumber) {
    	List<String> allListIds = new Vector<String>();
    	IProfile iProfile = getProfile(phoneNumber);
    	if (iProfile != null){
    		try {
    			IMultimediaDistributionList[] allLists = iProfile.getDistributionLists();
    			for (int i=0; i < allLists.length; i++){
    				int currentId = allLists[i].getId();
    				allListIds.add(String.valueOf(currentId));
    			}
    		} catch (ProfileManagerException e) {
    			throw new PlatformAccessException(
    					EventType.DATANOTFOUND, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, " distribution lists not found");
    		}
    	}
    	else {
    		throw new PlatformAccessException(
    				EventType.DATANOTFOUND, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, "phoneNumber not found");
    	}

    	String arrayOfIds[] = new String[allListIds.size()];
    	return allListIds.toArray(arrayOfIds);
    }

    /**
     * Retrieves a list of DistributionList ids for a subscriber with the specified telephonenumber.
     *
     * @param phoneNumber
     * @return list of ids
     */
    public String subscriberGetDistributionListMsid(String phoneNumber, String distListNumber) {
        List<String> allListIds = new Vector<String>();
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null){
            try {
                return iProfile.getDistributionListsMsid(distListNumber);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.DATANOTFOUND, "subscriberGetDistributionListMsid:phoneNumber=" + phoneNumber, " distListNumber="+distListNumber+" Not found ");
            }
        }
        else {
            throw new PlatformAccessException(
                    EventType.DATANOTFOUND, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, "phoneNumber not found");
        }
    }


    /**
     * Adds a member to the DistributionList specified with the distListNumber.
     *
     * @param phoneNumber    To find the profile that has the list
     * @param distListNumber Distributionlist to remove member from
     * @param distListMember Address on member to add
     */
    public void distributionListAddMember(String phoneNumber, String distListNumber, String distListMember) {

    	IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In distributionListAddMember, phoneNumber=" + phoneNumber + ", distListNumber=" + distListNumber + ", distListMember " + distListMember);
                }
                iProfile.addMemberToDistributionList(distListNumber, distListMember);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, e.getMessage(), e);
            }
        }
        else {
        	throw new PlatformAccessException(EventType.DATANOTFOUND, "distributionListAddMember=" + phoneNumber, " phoneNumber not found");
        }

    }

    /**
     * Removes a member from the DistributionList specified with the distListNumber.
     *
     * @param phoneNumber    To find the profile that has the list
     * @param distListNumber Distributionlist to remove member from
     * @param distListMember Address of member to remove
     */
    public void distributionListDeleteMember(String phoneNumber, String distListNumber, String distListMember) {

    	IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In distributionListDeleteMember, phoneNumber=" + phoneNumber + ", distListNumber=" + distListNumber + ", distListMember " + distListMember);
                }
                iProfile.deleteMemberFromDistributionList(distListNumber, distListMember);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, e.getMessage(), e);
            }
        }
        else {
        	throw new PlatformAccessException(EventType.DATANOTFOUND, "distributionListDeleteMember=" + phoneNumber, "phoneNumber not found");
        }

    }

    /**
     * Returns a list with the members in the specified distribution list. The list is empty if no members exist in the list.
     *
     * @param phoneNumber
     * @param distListNumber
     * @return list with members
     */
    public String[] distributionListGetMembers(String phoneNumber, String distListNumber) {
    	IProfile iProfile = getProfile(phoneNumber);
    	if (iProfile != null){
    		try {
    			IMultimediaDistributionList theList = iProfile.getDistributionList(distListNumber);
    			return theList.getMembers();
    		}
    		catch (DistributionListException e) {
    			throw new PlatformAccessException(
    					EventType.DATANOTFOUND, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, " distribution list members for list " +  distListNumber + " could not be retrieved");

			}
    		catch (ProfileManagerException e) {
    			throw new PlatformAccessException(
    					EventType.DATANOTFOUND, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, " distribution list " +  distListNumber + " not found");
    		}
    	}
    	else {
    		throw new PlatformAccessException(
    				EventType.DATANOTFOUND, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, "phoneNumber not found");
    	}
    }

    /**
     * Returns the spoken name for the specified distribution list.
     *
     * @param phoneNumber
     * @param distListNumber
     * @return spoken name mediaobject
     */
    public IMediaObject distributionListGetSpokenName(String phoneNumber, String distListNumber) {
    	 IProfile iProfile = getProfile(phoneNumber);
         if (iProfile != null) {
             try {
                 if (log.isDebugEnabled()) {
                     log.debug("In distributionListGetSpokenName, phoneNumber=" + phoneNumber + ", distListNumber=" + distListNumber);
                 }
                 IMediaObject theSpokenName = iProfile.getSpokenNameForDistributionList(distListNumber);
                 return theSpokenName;
             } catch (ProfileManagerException e) {
                 throw new PlatformAccessException(
                         EventType.SYSTEMERROR, e.getMessage(), e);
             }
         }
         else {
         	throw new PlatformAccessException(EventType.DATANOTFOUND, "distributionListGetSpokenName=" + phoneNumber, "phoneNumber not found");
         }
    }

    /**
     * Stores the spoken name for the specified distribution list.
     *
     * @param phoneNumber
     * @param distListNumber
     * @param spokenName
     */
    public void distributionListSetSpokenName(String phoneNumber, String distListNumber, IMediaObject spokenName) {
    	 IProfile iProfile = getProfile(phoneNumber);
         if (iProfile != null) {
             try {
                 if (log.isDebugEnabled()) {
                     log.debug("In distributionListSetSpokenName, phoneNumber=" + phoneNumber + ", distListNumber=" + distListNumber);
                 }
                 iProfile.setSpokenNameForDistributionList(distListNumber, spokenName);
             } catch (ProfileManagerException e) {
                 throw new PlatformAccessException(
                         EventType.SYSTEMERROR, e.getMessage(), e);
             }
         }
         else {
         	throw new PlatformAccessException(EventType.DATANOTFOUND, "distributionListSetSpokenName=" + phoneNumber, "phoneNumber not found");
         }
    }

    /**
     * Adds a distribution list to the profile specified with phoneNumber
     *
     * @param phoneNumber
     * @param distListNumber
     */
    public void subscriberAddDistributionList(String phoneNumber, String distListNumber) {
        IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberAddDistributionList, phoneNumber=" + phoneNumber + ", distListNumber=" + distListNumber);
                }
                iProfile.createDistributionList(distListNumber);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, e.getMessage(), e);
            }
        }
        else {
        	throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberAddDistributionList:phoneNumber=" + phoneNumber, "phoneNumber not found");
        }

    }

    /**
     * Deletes a distribution list from the profile specified with phoneNumber
     *
     * @param phoneNumber
     * @param distListNumber
     */
    public void subscriberDeleteDistributionList(String phoneNumber, String distListNumber) {
    	IProfile iProfile = getProfile(phoneNumber);
        if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberDeleteDistributionList, phoneNumber=" + phoneNumber + ", distListNumber=" + distListNumber);
                }
                iProfile.deleteDistributionList(distListNumber);
            } catch (ProfileManagerException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, e.getMessage(), e);
            }
        }
        else {
        	throw new PlatformAccessException(EventType.DATANOTFOUND, "subscriberDeleteDistributionList=" + phoneNumber, "phoneNumber not found");
        }

    }

    /**
     * Indicates if the searches should be limited to the user directory in the local sub/domain, e.g. when using iMux
     *
     * @param limit true if limit scope, false if not
     */
    public void systemSetPartitionRestriction(boolean limit) {
    }

    /**
     * Returns a String array of telephonenumbers for the subscribers that matches the specified attribute and value.
     * For example "uid", "161074"
     *
     * @param attribute the attribute to use when specifying the search.
     * @param value     the value to use when specifying the search.
     * @return an array of phonenumbers to the matching subscribers, empty if no subscribers are found.
     */
    public String[] systemGetSubscribers(String attribute, String value) {
    	throw new PlatformAccessException(EventType.SYSTEMERROR, "systemGetSubscribers is unimplemented");
    }

    /**
     * Creates a new subscriber in the user directory.
     *
     * @param attrNames  list of attribute names for the subscriber, the attrValues with the same index corresponds to this name.
     *                   The names should be the attributenames defined in CAI IWD
     * @param attrValues list of attribute values for the subscriber, the attrNames with the same index corresponds to this name.
     * @param adminUid   uid for an useradmin in the user directory.
     * @param cosName    name on CoS to use for the new subscriber (optional, use null if not included)
     */
    public void subscriberCreate(String[] attrNames, String[] attrValues, String adminUid, String cosName) {
        throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberCreate is unimplemented");
    }

    /**
     * Deletes a subscriber from the user directory.
     *
     * @param telephoneNumber identifies the subscriber to delete.
     * @param adminUid        uid for an useradmin in the user directory.
     */
    /*
    public void subscriberDelete(String telephoneNumber, String adminUid) {
        throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberDelete is unimplemented");
    }
    */

    public String subscriberGetMuid(String phoneNumber) {
    	
		String muid = null;
		
    	IProfile iProfile = getProfile(phoneNumber);
    	
    	if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetMuid, phoneNumber=" + phoneNumber);
                }
        		muid = iProfile.getIdentity("muid");
            } catch (ProfileManagerException e) {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberGetMuid, iProfile.getIdentity() threw an exception, returning null");
                }
                return null;
            }
            
            if (muid == null) {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberDelete, subscribers's muid not found, returning null");
                }
            }
            
    	} else {
            if (log.isDebugEnabled()) {
                log.debug("In subscriberDelete, subscriber profile not found, returning null");
            }
            return null;
    	}
    	
    	return muid;
    	
    }
    
    public boolean subscriberDelete(String phoneNumber) {
		String muid = null;
		
    	IProfile iProfile = getProfile(phoneNumber);
    	
    	if (iProfile != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberDelete, phoneNumber=" + phoneNumber);
                }
        		muid = iProfile.getIdentity("muid");
            } catch (ProfileManagerException e) {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberDelete, iProfile.getIdentity() threw an exception, returning false");
                }
                return false;
            }
            
            if (muid == null) {
                if (log.isDebugEnabled()) {
                    log.debug("In subscriberDelete, subscribers's muid not found, returning false");
                }
                return false;
            }
            
    	} else {
            if (log.isDebugEnabled()) {
                log.debug("In subscriberDelete, subscriber profile not found, returning false");
            }
            return false;
    	}
    	
    	return iProfileManager.deleteProfile(muid);
    }

    public boolean subscriberAutoprovision(String phoneNumber, String subscriberTemplate) {
        return iProfileManager.autoprovisionProfile(phoneNumber, subscriberTemplate);
    }

    
    /**
     * Internal function to retrieve an IProfile for the specified phoneNumber.
     * If found the IProfile is added to the cache.
     *
     * @param phoneNumber
     * @return the retrieved IProfile
     */
    private synchronized IProfile getProfile(String phoneNumber) {
    	return iProfileManager.getProfile(phoneNumber);
    }

    public boolean isProfileUpdatePossible(String phoneNumber) {
        return iProfileManager.isProfileUpdatePossible(phoneNumber);
    }

    public boolean subscriberRemoveFromCache(String phoneNumber) {
        return iProfileManager.removeProfileFromCache("subscriber", phoneNumber);
    }

}
