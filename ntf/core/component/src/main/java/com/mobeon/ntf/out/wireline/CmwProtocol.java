/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wireline;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.userinfo.CmwFilterInfo;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CmwProtocol extends XmpProtocol with the parameters specific to call-MWI
 * notification.
 */
public class CmwProtocol extends XmpProtocol {
    private static CmwProtocol inst = new CmwProtocol();

    public static CmwProtocol get() {
        return inst;
    }
    
    protected void addServiceSpecificItems(Document doc,
                                           Element service,
                                           Object o,
                                           String caller,
                                           String mailbox_id) {
        service.setAttribute("service-id", IServiceName.CALL_MWI_NOTIFICATION);
        addParameterElement(doc, service, "mailbox-id", mailbox_id);
        addParameterElement(doc, service, "called-number", (String)o);
        addParameterElement(doc, service, "called-type-of-number", "" + Config.getTypeOfNumber());
        addParameterElement(doc, service, "called-numbering-plan-id", "" + Config.getNumberingPlanIndicator());
        addParameterElement(doc, service, "calling-number", caller);
        addParameterElement(doc, service, "calling-type-of-number", "" + Config.getTypeOfNumber());
        addParameterElement(doc, service, "calling-numbering-plan-id", "" + Config.getNumberingPlanIndicator());
    }
}
