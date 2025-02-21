/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierMdrGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;

public class NotifierMdr {
    
    /** Private utility members */
    private INotifierUtil notifierUtil = null;
    
    private static NotifierMdr _inst = null;
    private INotifierMdrGenerator mdrGenerator = null;

    /**
     * Constructor
     */
    private NotifierMdr() {
        this.mdrGenerator = TemplateSmsPlugin.getMdrGenerator();
        this.notifierUtil = TemplateSmsPlugin.getUtil();
    }

    public static NotifierMdr get() {
        if(_inst == null) {
            _inst = new NotifierMdr();
        }
        return _inst;
    }

    /** NotifierMdrAction */
    public enum NotifierMdrAction {
        DELIVERED   ("delivered"),
        PHONEONDELIVERED ("phoneOnDelivered"),
        RETRY       ("retry"),
        FAILED      ("failed"),
        EXPIRED     ("expired"),
        DISCARDED    ("discarded");

        private String name;

        NotifierMdrAction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public void generateMdr(NotifierEvent notifierEvent, NotifierMdrAction action) {
        generateMdr(notifierEvent, action, null);
    }

    public void generateMdr(NotifierEvent notifierEvent, NotifierMdrAction action, String message) {
        if(notifierEvent.getNotifierType() != null) {
            String mdrName = notifierEvent.getNotifierTypeName() + action.getName();
            String mail = notifierUtil.getNormalizedTelephoneNumber(notifierEvent.getReceiverNumber());

            if (action.getName().equals(NotifierMdrAction.DELIVERED.getName())){
                mdrGenerator.generateMdrDelivered(mail,
                        notifierEvent.getNotifierType().getMdrPortType(), 
                        notifierEvent.getNotifierType().getMdrName());
            }
            else if (action.getName().equals(NotifierMdrAction.EXPIRED.getName())){
                mdrGenerator.generateMdrExpired(mail,
                        notifierEvent.getNotifierType().getMdrPortType(), 
                        notifierEvent.getNotifierType().getMdrName());
            }
            else if (action.getName().equals(NotifierMdrAction.FAILED.getName())){
                if(message==null || message.isEmpty()){
                    message=mdrName;
                }
                mdrGenerator.generateMdrFailed(mail,
                        notifierEvent.getNotifierType().getMdrPortType(),
                        notifierEvent.getNotifierType().getMdrName(),
                        message);
            }
            else if (action.getName().equals(NotifierMdrAction.DISCARDED.getName())){
                if(message==null || message.isEmpty()){
                    message=mdrName;
                }
                mdrGenerator.generateMdrDiscarded(mail,
                        notifierEvent.getNotifierType().getMdrPortType(),
                        notifierEvent.getNotifierType().getMdrName(),
                        message);
            }
            else if (action.getName().equals(NotifierMdrAction.PHONEONDELIVERED.getName())) {
                mdrGenerator.generateMdrPhoneOnDelivered(mail);
            }
        }        
    }
}
