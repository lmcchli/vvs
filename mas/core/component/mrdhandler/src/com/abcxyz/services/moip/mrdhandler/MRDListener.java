/**
COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.

THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
TO ANY OTHER PERSON OR ENTITY.
TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
*/
package com.abcxyz.services.moip.mrdhandler;

import java.util.Hashtable;
import java.util.List;
import java.util.MissingResourceException;

import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.mrd.client.service.MrdSocketHandler;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.abcxyz.messaging.mrd.operation.MsgServerOperations;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * 
 * This class is use to create a listener for MRD protocol.
 * @author esebpar
 * @since MiO2.0
 *
 */
public class MRDListener {

    private static ILogger log = ILoggerFactory.getILogger(MRDListener.class);
    
    private Hashtable<String,MsgServerOperations> msgServers;
    
    private int port = -1;
    private int corePoolSize = -1;
    private int maxPoolSize = -1;
    
    private OAMManager oamManager;
    
    private CommonMessagingAccess cma;
    
    public MRDListener(){
        
    }
    
    public void setPort(int port){
        this.port = port;
        if(log.isDebugEnabled()){
            log.debug("MRDListener.setPort: Setting the port to " + Integer.toString(port));
        }
    }
    
    public void setMaxPoolSize(int maxPoolSize){
        this.maxPoolSize = maxPoolSize;
        if(log.isDebugEnabled()){
            log.debug("MRDListener.setMaxPoolSize: Setting the maximum pool size to " + Integer.toString(maxPoolSize));
        }
    }
    
    public void setCorePoolSize(int corePoolSize){
        this.corePoolSize = corePoolSize;
        if(log.isDebugEnabled()){
            log.debug("MRDListener.setCorePoolSize: Setting the core pool size to " + Integer.toString(corePoolSize));
        }
    }
    
    public void setCommonMessagingAccess(CommonMessagingAccess commonMessagingAccess){
        this.cma = commonMessagingAccess;
    }
    
    /**
     * Setting the messaging server that we want to listen for.
     * @param msgServers A list of messaging server, it is not 
     *        List<MsgServerOperations> because it is call by the spring framework. 
     */
    @SuppressWarnings("unchecked")
    public void setMsgServers(List msgServers){
        if(log.isDebugEnabled()){
            log.debug("MRDListener.setMsgServers: Setting the msgServers.");
        }
        if(this.msgServers == null){
            this.msgServers = new Hashtable<String, MsgServerOperations>();
        }
        
        for(Object entry : msgServers){
            if(entry instanceof MsgServerOperations){
                if(log.isDebugEnabled()){
                    log.debug("MRDListener.setMsgServers: Adding message server of class: " + ((MsgServerOperations)entry).getMsgServerClass());
                }
                this.msgServers.put(((MsgServerOperations)entry).getMsgServerClass(), (MsgServerOperations)entry);
            }else{
                //As we only want object of type MsgServerOperations in the list
                //we throw an exception if we have anything else
                throw new RuntimeException("Invalid type, the list need to be of type MsgServerOperations");
            }
        }
    }
    
    public void init(){
        if(log.isDebugEnabled()){
            log.debug("MRDListener.init: Initialise the mrd listener.");
        }
        if(cma == null){
            oamManager = CommonOamManager.getInstance().getMrdOam();
        }else{
            oamManager = cma.getOamManager().getMrdOam();
        }
        
        if(corePoolSize <= 0){
            setCorePoolSize(oamManager.getConfigManager().getIntValue(DispatcherConfigMgr.ServerSocketCorePoolSize));
        }
        if(maxPoolSize <= 0){
            setMaxPoolSize(oamManager.getConfigManager().getIntValue(DispatcherConfigMgr.ServerSocketMaxPoolSize));
        }
        if(port <= 0){
            setPort(oamManager.getConfigManager().getIntValue(MoipMessageEntities.MasMrdServicePort));
            if(port <= 0){
                setPort(MoipMessageEntities.MAS_SERVICE_DEFAULT_PORT);
            }
        }
        
        start();
    }
    
    /**
     * Start the MRD listener.
     */
    public void start(){
        if(log.isDebugEnabled()){
            log.debug("MRDListener.start: Starting the mrd listener on port: " + Integer.toString(port));
        }
        MrdSocketHandler mrdListener;
        mrdListener = new MrdSocketHandler(oamManager, corePoolSize, maxPoolSize);
        if(!mrdListener.startMultiple(msgServers, port, null)){
            throw new MissingResourceException("Can't start the MRD listener",
                "MRDListener", "MRDListener");
        }

    }
}
