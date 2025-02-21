/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.sms;

import com.mobeon.ntf.slamdown.SlamdownList;

/**
 * Interface handling SlamdownInfo results from SMSc
 */
public interface InfoResultHandler {
    
    void allOk(SlamdownList list, int okCount);

    void noneOk(SlamdownList list);

    void retry(SlamdownList list);

    void failed(SlamdownList list);

    void result(SlamdownList list, boolean[] result, int okCount);
}
