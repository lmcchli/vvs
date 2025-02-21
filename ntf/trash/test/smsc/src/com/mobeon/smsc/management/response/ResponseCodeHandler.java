package com.mobeon.smsc.management.response;

import java.util.Vector;
import java.util.Random;
import java.util.Date;

/**
 * This class receives a list of response codes for binging, submitting and general
 * requests. This list is transformed into a procentuell values for binding
 * , submitting and general response codes. When a the SMS-C receives
 * a PDU request a response code is returned according to the procentuell 
 * value for each of these request types.
 */
public class ResponseCodeHandler {
    
    /**Vector of bind response codes*/
    private Vector bind = new Vector(100);
    /**Vector of submit response codes*/
    private Vector submit = new Vector(100);
    /**Usead to get time for seed in Random*/
    private Date date = new Date();
    /**Fixed bind response code*/
    private int bindResultCode = -1;
    /**Shall the code revert to normal once used?*/
    private boolean bindResultOnce = false;
    /**Fixed submit response code*/
    private int submitResultCode = -1;
    /**Shall the code revert to normal once used?*/
    private boolean submitResultOnce = false;
    /**Fixed enquire link response code*/
    private int enquireLinkResultCode = -1;
    /**Shall the code revert to normal once used?*/
    private boolean enquireLinkResultOnce = false;
    /**Fixed cancel response code*/
    private int cancelResultCode = -1;
    /**Shall the code revert to normal once used?*/
    private boolean cancelResultOnce = false;
    /**Creates a new instance of this class.*/
    public ResponseCodeHandler() {
        
    }
    
    /**
     *Adds a responsecode for bind requests and its corrosponding procentuell value.
     *@param responseCode is the response code
     *@param procent is the procentuell value for this response code.
     */
    public void setBindResponseData(int responseCode, int procent){
        for(int i = 0; i < procent; i++){
            if( bind.size() > 100 ){
                bind.add(new Integer(responseCode));
            }
        }
    }
           
    /**
     *Adds a responsecode for submit requests and its corrosponding procentuell value.
     *@param responseCode is the response code
     *@param procent is the procentuell value for this response code.
     */
    public void setSubmitResponseData(int responseCode, int procent){         
        for(int i = 0; i < procent; i++){
            if( bind.size() > 100 ){
                submit.add(new Integer(responseCode));
            }
        }
    }
         
    /**
     *Adds a responsecode for both submit and bind requests and its corrosponding procentuell value.
     *@param responseCode is the response code
     *@param procent is the procentuell value for this response code.
     */
    public void setGeneralResponseData(int responseCode, int procent){                 
        for(int i = 0; i < procent; i++){
            if( bind.size() > 100 ){
                bind.add(new Integer(responseCode));
                submit.add(new Integer(responseCode));
            }
        }
    }
    
    /**
     *Gets a response code for a submit request according to the
     *@return a response code according to the traffic data.     
     */
    public int getSubmitResponseCode(){     
        if (submitResultCode < 0) {
            if( submit.size() == 0 ) return 0;
            Random rand = new Random(date.getTime());                        
            int positon = rand.nextInt()%submit.size();
            try{
                return ((Integer)submit.get(positon)).intValue();
            }catch(NumberFormatException e){
                return 0;
            }catch(NullPointerException e){
                return 0;
            }catch(Exception e){
                return 0;
            }                        
        } else {
            int i = submitResultCode;
            if (submitResultOnce) { submitResultCode = -1; }
            return i;
        }
    }
         
    /**
     *Gets a response code for a bind request according to the traffic model.
     *@return a response code according to the traffic data.     
     */
    public int getBindResponseCode(){                     
        if (bindResultCode < 0) {
            if( bind.size() == 0 ) return 0;
            Random rand = new Random(date.getTime());                        
            int positon = rand.nextInt()%submit.size();
            try{
                return ((Integer)bind.get(positon)).intValue();
            }catch(NumberFormatException e){
                return 0;
            }catch(NullPointerException e){
                return 0;
            }catch(Exception e){
                return 0;
            }                        
        } else {
            int i = bindResultCode;
            if (bindResultOnce) { bindResultCode = -1; }
            return i;
        }
    }

    /**
     *Gets a response code for an enquire link request according to the traffic model.
     *@return a response code according to the traffic data.     
     */
    public int getEnquireLinkResponseCode(){                     
        if (enquireLinkResultCode < 0) {
            return 0;
        } else {
            int i  = enquireLinkResultCode;
            if (enquireLinkResultOnce) { enquireLinkResultCode = -1; }
            return i;
        }
    }

    /**
     *Gets a response code for a cancel request according to the traffic model.
     *@return a response code according to the traffic data.     
     */
    public int getCancelResponseCode(){                     
        if (cancelResultCode < 0) {
            return 0;
        } else {
            int i  = cancelResultCode;
            if (cancelResultOnce) { cancelResultCode = -1; }
            return i;
        }
    }

    public void setResultCode(int bc, boolean bonce, int sc, boolean sonce, int ec, boolean eonce, int cc, boolean conce) {
        bindResultCode = bc;
        bindResultOnce = bonce;
        submitResultCode = sc;
        submitResultOnce = sonce;
        enquireLinkResultCode = ec;
        enquireLinkResultOnce = eonce;
        cancelResultCode = cc;
        cancelResultOnce = conce;
    }

}
