/*
 * XmpServerQueue.java
 *
 * Created on den 22 december 2005, 17:13
 */

package com.mobeon.common.xmp.server;

import com.mobeon.common.util.logging.ILogger;
import com.mobeon.common.xmp.XmpConstants;
import java.util.*;


public class XmpResponseQueue extends Thread {
    /** Pending requests, of XmpTransaction keyed by TId (Integer) */
    private HashMap<Integer, XmpTransaction> transactions = new HashMap<Integer, XmpTransaction>();

     private HashMap<Integer, IXmpAnswer> rspQueue = new HashMap<Integer, IXmpAnswer>();

    private HttpServer server;

    private ILogger log = null;

    String clientId;

    /** Creates a new instance of XmpServerQueue */
    public XmpResponseQueue(String clientId, HttpServer server) {
        this.clientId = clientId;
        this.server = server;
        start();
    }

    public String getClientId() {
        return clientId;
    }

    public void setLogger(ILogger l) {
        log = l;
    }

    public boolean isAResponseReady () {
        synchronized(rspQueue) {
    	   return !(rspQueue.isEmpty());
        }
    }

    /**
     * Stores a answer in the response queue
     *@param answer the answer to store.
     */
    public void addResponse(IXmpAnswer answer) {

        Object result = null;
        synchronized (transactions) {
            result = transactions.remove(answer.getTransactionId());
        }
	    if (result != null) {
	       synchronized (rspQueue) {
               rspQueue.put(answer.getTransactionId(), answer);
	       }
	       synchronized (this) {
               this.notifyAll();
           }
        }

    }

    /**
     * Put a response back in the queue.
     *
     * @param answer The resonse
     */
    public void reAddResponse(IXmpAnswer answer) {
    	synchronized (rspQueue) {
            rspQueue.put(answer.getTransactionId(), answer);
    	}
    }


    public void addTransaction(XmpTransaction transaction) {
	synchronized (transactions) {
	    transactions.put(transaction.getTransactionId(), transaction);
	}
    }

     /**
     * Scans pending transactions and returns an error response for each 
     * expired transaction.
     *@param now current time.
     */
     private void expiredTransactions(Date now) {
         Iterator<Integer> it;
         Integer key;
         try {

             synchronized (transactions) { // Get a key copy.
                 HashMap<Integer, XmpTransaction> copy = (HashMap<Integer, XmpTransaction>) transactions.clone();
                 it = copy.keySet().iterator();
             }
             for (; it.hasNext();) {
                 XmpTransaction t;
                 key = (Integer) it.next();
                 synchronized (transactions) {
                     t = (XmpTransaction) transactions.get(key);
                 }
                 if (t != null && t.isExpired(now)) { //Forget the transaction
                     Object result = null;
                     synchronized (transactions) {
                         result = transactions.remove(t.getTransactionId());
                     }
                     if (result != null) {
                         ServiceHandler sh = t.getServiceHandler();
                         sh.cancelRequest(clientId, t.getTransactionId());
                         XmpAnswer answer = new XmpAnswer(XmpConstants.TIMEOUT,"Transaction validity period expired");
                         answer.setTransactionId(t.getTransactionId());
                         synchronized(rspQueue) {
                            rspQueue.put(answer.getTransactionId(), answer);
                         }
                     }
                 }
             }
             // Catch all exceptions that have gone this far,
             // and log where they came from
         } catch (Exception e) {
             log.error("Failed to clear expired transactions, " + e.toString());
         }
     }

    /**
     * @return An xmp answer, or null if the key in not in the table.
     */
    public IXmpAnswer getAnswer(Integer transactionId) {
    	synchronized (rspQueue) {
    	   	return (IXmpAnswer) rspQueue.remove(transactionId);
    	}
    }

    
    /**
     * Returns the list of transaction IDs for the pending responses.
     * 
     * @return Array of transaction IDs.
     */
    Integer[] getAnswerIds() {
        synchronized (rspQueue) {
            Set<Integer> transactionIds = rspQueue.keySet(); 
            return transactionIds.toArray(new Integer[transactionIds.size()]);
        }
    }

    
    /**
     * Returns an answer if it is waiting in the queue, don't wait
     * if the queue is empty.
     *
     * @return An xmp answer, or null if the queue is empty.
     */
    public IXmpAnswer getBufferedAnswer(Integer transactionId) {
    	synchronized (rspQueue) {
            return (IXmpAnswer) rspQueue.remove(transactionId);
    	}
    }

    public void run() {
        while( true ) {
            try {
                while (true) {
                    Date now = new Date();
                    expiredTransactions(now);
                    try {
                        sleep(2000);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            } catch (Exception e) {
                log.error("Unexpected: ", e);
            } catch (OutOfMemoryError e) {
                log.fatal("Out of memory, shutting down...", e);
                System.exit(0);
            }
        }
    }
}
