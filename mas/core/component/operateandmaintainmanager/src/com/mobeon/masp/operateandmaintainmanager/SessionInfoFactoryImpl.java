package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */


public class SessionInfoFactoryImpl implements SessionInfoFactory {

    private Integer curVal = 1;
    //private Hashtable<String, Vector> lstConnectionInfo;
    //private LinkedList<Integer> numberPol;
    private SortedSet<Integer> numberPol;

    private Hashtable<String,Integer> keyRef;
    private ILogger log;


    ConcurrentHashMap<String,SessionInfoImpl> lstSessions ;

    public SessionInfoFactoryImpl(){
        log = ILoggerFactory.getILogger(SessionInfoFactoryImpl.class);
        lstSessions = new ConcurrentHashMap<String,SessionInfoImpl>();
        //numberPol = new LinkedList<Integer>();
        numberPol = new TreeSet<Integer>();
        keyRef  = new Hashtable<String,Integer>();
    }

    /**
     * return a new sessionObject or if exsist retunrs the exsisting one.
     * @param sessionId
     * @return session object
     */
    public synchronized SessionInfo getSessionInstance(String sessionId, String connectionId) {
        //String connectionId = "001";
        SessionInfoImpl sessionObj;
        String sessionKey=sessionId+connectionId;

        if (lstSessions.containsKey(sessionKey)) {
            sessionObj = lstSessions.get(sessionKey);
            if(log.isDebugEnabled())
                log.debug("Session retreived [Sid:"+sessionId+", Pos:"+sessionObj.getPos()+"]");
        }
        else {
            sessionObj = createNewSession(sessionId,connectionId);
            if(log.isDebugEnabled())
                log.debug("Session created [Sid:"+sessionId+", Pos:"+sessionObj.getPos()+"]");

        }

        return sessionObj;
    }

    /**
     * This method is called when call is ended and no update to the instance will be made.
     * @param sessionInfo
     */
    public synchronized void returnSessionInstance(SessionInfo sessionInfo) {
        SessionInfoImpl curSessionImpl = (SessionInfoImpl)sessionInfo;
        curSessionImpl.setDisposed();  // this object cant be used any more .....Call ended
        if(log.isDebugEnabled())
            log.debug("Session is disposed  [Sid:"+ curSessionImpl.getSessionId()+"]" );


        // remove old sessions. This is to prevent that old sessions dosnent get disposed.
        // If sessions is not read within 5 sec. The session is removed from list.
        for(Map.Entry session:lstSessions.entrySet()) {
            SessionInfoImpl sessionInfoImpl = (SessionInfoImpl)session.getValue() ; //lstSessions.elements().nextElement();
            if (sessionInfoImpl.isDisposed()) {
                if (sessionInfoImpl.isOld()) {
                    lstSessions.remove(sessionInfoImpl.getKey());
                    //numberPol.offer(sessionInfoImpl.getPos()); // Make the pos number reusable.
                    numberPol.add(sessionInfoImpl.getPos()) ; // Make the pos number reusable.
                    if(log.isDebugEnabled())
                        log.debug("Session is flushed [Sid:"+ sessionInfoImpl.getSessionId()+"]" );

                }
            }

        }

    }

    public synchronized HashMap<String,SessionInfoRead> getChangedSessions(){

        HashMap<String,SessionInfoRead> returnSessions = new HashMap<String,SessionInfoRead>();


        //for(Map.Entry service:lstSessions.entrySet()) {
        //    SessionInfoRead session = (SessionInfoRead)service.getValue();
        //    row=parseRowData(session);
        //    pos = session.getPos();
        //    printData.put(pos,row);
        //}


        //while (lstSessions.elements().hasMoreElements()) {
        for(Map.Entry session:lstSessions.entrySet()) {
            SessionInfoImpl sessionInfoImpl = (SessionInfoImpl)session.getValue() ; //lstSessions.elements().nextElement();
            // Check if changed
            if (sessionInfoImpl.isChanged() ) {
                sessionInfoImpl.setRead();  // OK. the mark as read.

                // add object to return map.
                returnSessions.put(sessionInfoImpl.getKey(),(SessionInfoRead)sessionInfoImpl);

                // check if object is disposed.
                if (sessionInfoImpl.isDisposed()){
                    if(log.isDebugEnabled())
                        log.debug("Session is flushed  [Id:"+ sessionInfoImpl.getSessionId().toString()+"]" );
                    lstSessions.remove(sessionInfoImpl.getKey());
                    //numberPol.offer(sessionInfoImpl.getPos()); // Make the pos number reusable.
                    numberPol.add(sessionInfoImpl.getPos()); // Make the pos number reusable.

                }
                if(log.isDebugEnabled())
                    log.debug("Retreiving changed session [Sid:"+sessionInfoImpl.getSessionId()+",Disposed:"+sessionInfoImpl.isDisposed().toString()+"]" );
            }

        }
        return returnSessions;
    }

    private SessionInfoImpl createNewSession(String sessionId,String connectionId) {
        SessionInfoImpl sessionObj = new SessionInfoImpl(this);
        Integer pos;
        String sessionKey = sessionId+connectionId;
        // find free pos
        pos = getPos(sessionKey);
        sessionObj.setPos(pos);
        sessionObj.setSessionId(sessionId);
        sessionObj.setConnectionId(connectionId);

        lstSessions.put(sessionKey,sessionObj);

        return sessionObj;
    }




   /* public synchronized void remove(SessionInfo sessionInfo){
   String sessionId = null;
   try {
       sessionId = sessionInfo.getSessionId() ;
   } catch (IlegalSessionInstanceException e) {
       e.printStackTrace();
   }

   if (lstSessions.containsKey(sessionId)) {


   }

//       Vector<String> empty = new Vector<String>();
//       Integer pos;
//        // Removing post with sessionId
//        if (keyRef.containsKey(sessionId)) {
//            // ta bort ur listan
//            pos = keyRef.get(sessionId);
//            empty.add(time.toString());
//            empty.add(pos.toString());
//            empty.add("EMPTY SLOT");
//            //lstConnectionInfo.remove(pos);
//            lstConnectionInfo.put(pos.toString() ,empty);
//
//            keyRef.remove(sessionId);
//            // addera ledig plats i number kö.
//            numberPol.offer(pos);
//        }
}
   */

    private synchronized Integer getPos(String sessionId){
        // om det finns ett element i keyRef listan. Returera det
        if(log.isDebugEnabled())
            log.debug("Available pos "+ numberPol.toString());
        Integer returnVal;
        if (keyRef.containsKey(sessionId)) {
            return keyRef.get(sessionId);
        }
        else {
            // look if there is a free num in pol.
            if (numberPol.isEmpty()){
                // No..take new number
                returnVal = curVal;
                curVal++;

            }
            else {
                // reuse old number
                //returnVal = numberPol.poll();
                returnVal = numberPol.first();
                numberPol.remove(returnVal);
            }

            return returnVal;

        }
    }

}
