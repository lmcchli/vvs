/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.abcxyz.services.moip.ntf.result;

import java.util.HashMap;
import java.util.Map;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.oe.lib.OEManager;

/**
 * 
 * 
 * @author lmcyvca
 */
public class NotificationActionResolver {

    private Map<ResultCode, NotificationAction> codeMap = new HashMap<ResultCode, NotificationAction>();
    private NotificationAction defaultAction = NotificationAction.retry;

    public void load(String configFileName) throws ConfigurationDataException {
        codeMap.clear();

        ConfigManager configManager = OEManager.getConfigManager(configFileName);

        Map<String, Map<String, String>> errorCodesTable = configManager.getTable("ErrorCodes.Table");

        if (errorCodesTable != null) {
            for (Map.Entry<String, Map<String, String>> entry : errorCodesTable.entrySet()) {
                String errorCodeStr = entry.getKey();
                Map<String,String> row = entry.getValue();
                String actionCodeStr = row.get("errorCodeAction");
                
                ResultCode errorCode = new ResultCode(errorCodeStr);
                NotificationAction action = NotificationAction.valueOf(actionCodeStr);
                
                codeMap.put(errorCode, action);
            }
        }
        
        String defaultActionStr = configManager.getParameter("default");
        defaultAction = NotificationAction.valueOf(defaultActionStr);
    }

    NotificationAction getDefaultNotificationAction() {
        return defaultAction;
    }
    
    NotificationAction getNotificationAction(ResultCode code) {
        NotificationAction action = defaultAction;
        
        if (codeMap != null && code != null) {
            NotificationAction mappedAction = codeMap.get(code);

            if (mappedAction != null) {
                action = mappedAction;
            }
        }

        return action;
    }
}
