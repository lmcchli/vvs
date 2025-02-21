package com.mobeon.ntf.mail.test;

import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.mail.EmailStore;
import com.mobeon.ntf.util.PersistentObject;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: enikfyh
 * Date: 2007-nov-14
 * Time: 13:39:15
 */
public class TestEmailListHandler extends EmailListHandler {
      int count = 0;


      public TestEmailListHandler() {
        super(null, null, 0);
      }

      public void addEmailList(Vector v){
        count++;
    }

    public int getCount() {
        return count;
    }

    public void resetCount() {
        count = 0;
    }
}
