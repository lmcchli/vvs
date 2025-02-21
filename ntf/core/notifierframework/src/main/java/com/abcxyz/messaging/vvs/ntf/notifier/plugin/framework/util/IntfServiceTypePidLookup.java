package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * Combines configuration settings with SMS knowledge to give a convenient
 * interface to information about replacement and cancel in the SMSC and phone.
 *
 * The PID to be used for this content. PID is used for replace in the
 * mobile devices. The PID value is determined by the position
 * in ReplaceNotification parameter. E.g. ReplaceNotification=c,h where c gets
 * the PID number 65 and h 66 up to 71. Only seven configurable content types are allowed.
 * Why seven? Determined by the GSM/WCDMA and SMPP specifications.
 *
 * However we allow replace and Cancel in the SMSC above 7 as this is done using
 * the service_type "VXx" which can be up to 6 characters.
 *
 * The value of the service type. Service type is one of the SMPP PDU
 * parameters used to identify an SMS PDU in the SMS-C. The value
 * is set to VM followed by the content type or depositType position in
 * the ReplaceNotification parameter. E.g. vm1 if the content type is c
 * and vm2 if the content type is h. The first position in the table represents
 * VM1 and so on.
 * 
 * This will allow the plug-in and NTF to lookup Service Type and related info
 * used for Cancel and Replace by NTF.
 * 
 * The information is compiled from ReplaceNotifications.List in notification.conf
 * and refreshed on a configuration refresh.
 *
 */
public interface IntfServiceTypePidLookup {

    /**
     * Tells if notifications with this content should be replaced (in the
     * SMSC).
     *@param contentType - the name of the template for the SMS content.
     *@return true if the template is one that should be replaced and replace in
     *the SMSC has not been disabled.
     */
    public boolean isReplace(String contentType);

    /**
     * Finds the service type for notifications with this content.
     *@param replaceTypeName - the type of content phrase in the replace.list.
     *@return A unique service type for this template if the template is
     *in the table. 
     */
    public String getServiceType(String replaceTypeName);

    /**
     *Gets the PID for a specific notification content.
     *The PID is used to replace on the phone rather than SMSC.
     *@param replaceTypeName - replaceTypeName - the type of content phrase in the replace.list.
     *@return A unique PID for this notification content if the template is one
     * that should be replaced and 0 otherwise.
     */
    public int getPid(String replaceTypeName);

    /**
     *Gets the Position in replace/cancel Table given the contentType from *.cphr
     *@param contentType - the name of the template for the SMS content.
     *@return A unique Position that should be replaced and -1 otherwise.
     */
    public int getPosition(String contentType);
    
    /**
     *Gets the Position in replace/cancel based on the serviceType.
     *
     *Provided as a convenience method as plug-ins can override the
     *serviceType and may want to look up the position in order to
     *cancel or replace by the ServiceType.
     *
     *Since ServieType can be duplicated in the table, it is possible
     *more than one might be indicated hence the array. If there is the 
     *caller must determine the correct one by other or point out that
     *there should not be duplicates in the list as a plug-in limitation.
     *
     *You could for example lookup the position via the contentType
     *instead.
     *
     *@param serviceType - the serviceType of the SMS message
     *@return An array of position that could be replaced or cancelled.
     *Note: will return an array of length 1 with value -1 to indicate
     *not defined in the table.
     */
    public int[] getPositionByServiceType(String serviceType);
    
    /**
     * Finds the service type for notifications with this position.
     *@param position - The position in the table
     *@return A unique service type for this template if the template is
     *in the table. 
     */
    public String getServiceType(int position);

    /**
     *Gets the PID for a specific notification content.
     *The PID is used to replace on the phone rather than SMSC.
     *@param position - The position in the table
     *@return A unique PID for this notification content if the template is one
     * that should be replaced and 0 otherwise.
     */
    public int getPid(int position);
    
}

