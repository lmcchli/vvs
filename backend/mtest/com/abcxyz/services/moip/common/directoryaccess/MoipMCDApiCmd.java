package com.abcxyz.services.moip.common.directoryaccess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
//import java.util.Properties;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.mcd.proxy.ldap.McdProxyServiceImpl;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.mcd.proxy.MCDProxyService;
import com.abcxyz.messaging.mcd.proxy.MCDProxyServiceFactory;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.externalcomponentregister.IServiceInstance;


public class MoipMCDApiCmd {
	private String DEFAULT_MCD_HOST = "142.133.118.50";
	//private static final String DEFAULT_MCD_HOST = "172.30.164.98";

    private int DEFAULT_MCD_PORT = 30389;
    //private static final int DEFAULT_MCD_PORT = 388;

    private int DEFAULT_POOL_SIZE = 5;

	private Profile profile=null;
	private String cosName="";
	private String subsIdentity="";
	private String msid ="";
	private URI theURI;
	private String curPath;
	private String [] identities;

	private MCDProxyService mcdProxy;
	private LogAgent logAgent;

	public MoipMCDApiCmd() {
		curPath = System.getProperty("user.dir") + "/";
		readConfigFile(curPath + "moipMcdAPiCmd.config");
	}

    private void binding() {
    	CommonOamManager oamManager = CommonOamManager.getInstance();
    	logAgent = oamManager.getMcdOam().getLogAgent();

    	OAMManager mcdManager = oamManager.getMcdOam();
   	
    	try {    	
    		//TODO read these from config file rather than hardcoding
    	mcdManager.getConfigManager().setParameter(MCDConstants.CONFIG_HOST, "172.30.164.98");
    	mcdManager.getConfigManager().setParameter(MCDConstants.CONFIG_PORT, "388");
    	mcdManager.getConfigManager().setParameter(MCDConstants.CONFIG_OPCO, "opco1");
    	mcdManager.getConfigManager().setParameter(MCDConstants.CONFIG_BIND, "cn=Directory Manager");

			mcdManager.getConfigManager().setParameter(MCDConstants.CONFIG_PASSWORD, "mcd");

    	mcdManager.getConfigManager().setParameter(MCDConstants.MAX_RETRIES, "0");
    	mcdManager.getConfigManager().setParameter(MCDConstants.CONFIG_POOLSIZE, "5");	
		} catch (ConfigurationDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		
		
    	mcdProxy = MCDProxyServiceFactory.getMCDProxyService(oamManager.getMcdOam(), false, null) ;
    	//mcdSrv = new McdProxyServiceImpl(DEFAULT_MCD_HOST, DEFAULT_MCD_PORT, DEFAULT_POOL_SIZE, new StdoutLogger());

        KeyValues[] credentials= new KeyValues[3];
        credentials[0] = new KeyValues(MCDConstants.KEYVALUE_BIND_DN_FIELD_NAME, new String[]{"cn=Directory Manager"});
        credentials[1] = new KeyValues(MCDConstants.KEYVALUE_PASSWORD_FIELD_NAME, new String[]{"mcd"});
        credentials[2] = new KeyValues(MCDConstants.KEYVALUE_OPCO_ID_FIELD_NAME, new String[]{"opco1"});

        try {
        	mcdProxy.bindOpco(credentials);
        	System.out.println("binding okay");
        } catch ( Exception e) {
    		System.out.println("Exception at binding :" + e.getMessage());
    		e.printStackTrace();
        	System.exit(-1);
        }
    }


    private void lookupProfile(String id) {
    	try {
        	System.out.println("lookupProfile() - identity = " + id);

			binding();
			String profileClass = "subscriber";
			if (id.startsWith("cos:")) {
				profileClass = "classOfService";
			}
    		Profile sp = mcdProxy.lookupProfile(profileClass, new URI(id), "MOIP");

    		this.theURI = sp.getIdentities("msid")[0];
    		this.msid = this.theURI.toString();

    		System.out.println("msid value =" + msid);
    		System.out.println("reading profile:\n" + sp.toString());
    		System.out.println("lookupProfile done!!!");

    	} catch (Exception e) {
    		System.out.println("Exception at lookupProfile :" + e.getMessage());
    	}
    }

    private void deleteProfile(String theIdentity) {
    	try {
    			System.out.println("deleteProfile() - subscriber identity = " + theIdentity);

    			String profileClass = "subscriber";
    			if (theIdentity.startsWith("cos:")) {
    				profileClass = "classOfService";
    			}

    			lookupProfile(theIdentity);

    			mcdProxy.deleteProfile(profileClass, this.theURI);

        		System.out.println("deleteProfile done!!!");
    	} catch (Exception e) {
    		System.out.println("Exception at deleteProfile :" + e.getMessage());
    	}
    }


    private void createSubscriber() {
    	try {
    		binding();

    		msid ="";

    		profile = new ProfileContainer();

			readFile(curPath + "subscriber_template.txt");

			profile.addIdentity(new URI(msid));

			if (identities == null || identities.length < 0) {
				printHelp();
				System.exit(-1);
			}

			for (String id: identities) {
				profile.addIdentity(new URI(id));
			}

			profile.addAttributeValue("objectClass", "subscriber");
            profile.addAttributeValue("objectClass", "MOIPSubscriber");

            System.out.println("msid =" + msid);

            mcdProxy.createProfile("subscriber", new URI(msid), profile);

    		System.out.println("createSubscriber done");

    	} catch (Exception e) {
    		System.out.println("Exception at createSubscriber :" + e.getMessage());
    		e.printStackTrace();
    	}
    }

    private void createCos() {
    	try {
    		System.out.println("createCos ...");

    		binding();

            msid ="";

			profile = new ProfileContainer();

			readFile(curPath + "cos_template.txt");

			profile.addIdentity(new URI(msid));
			profile.addIdentity(new URI(cosName));

    		profile.addAttributeValue("objectClass", "classOfService");
    		profile.addAttributeValue("objectClass", "MOIPclassOfService");

            System.out.println("msid =" + msid);

            mcdProxy.createProfile("classOfService", new URI(msid), profile);

            System.out.println("createCos done");

    	} catch (Exception e) {
    		System.out.println("Exception at createCos :" + e.getMessage());
    	}
    }

    private boolean parsingArgs(String [] args) {

        int i = 0;
        String arg;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];

            if (arg.equals("-help")) {
                printHelp();
            }
            else if (arg.equals("-cs")) {
                    createSubscriber();
            }
            else if (arg.equals("-cc")) {
                    createCos();
            }
            else if (arg.equals("-l")) {
            	        System.out.println("lookup subscriber profile...");
                        if (i < args.length) {
                        	subsIdentity = args[i++];
                        	lookupProfile(subsIdentity);
                        }
                        else {
                            printHelp();
                            return false;
                        }
            }
            else if (arg.equals("-d")) {
                        if (i < args.length) {
                        	subsIdentity = args[i++];
                        	deleteProfile(subsIdentity);
                        }
                        else {
                            printHelp();
                            return false;
                        }
           } else {
               printHelp();
           }
        }
        return true;
    }


    private void readFile(String fileName) {
		 try {
			    System.out.println("reading file...." + fileName);
		        BufferedReader in = new BufferedReader(new FileReader(fileName));
		        String str;
		        while ((str = in.readLine()) != null) {
		            process(str);
		        }
		        in.close();
		 } catch (IOException e) {
	    		System.out.println("Exception at readFile :" + e.getMessage());
		 }
    }

    private void process(String lineStr) {

    	String attrName  = lineStr.substring(0, lineStr.indexOf(":"));
    	String attrValue = lineStr.substring(lineStr.indexOf(":")+1);

    	System.out.println("line read:" + lineStr + " --> split into (attrName,attrValue)=(" + attrName + "," + attrValue +")");

    	if ("MSID".equalsIgnoreCase(attrName)) {
    		msid = "msid:" + attrValue;

    		System.out.println("msid=" + msid);
    		return;
    	}
    	else if ("Identity".equalsIgnoreCase(attrName)) {
    		if (attrName.isEmpty()) {
    			printHelp();
    			System.exit(-1);
    		}
    		identities = attrValue.split(",");
    		return;
    	}
    	else if ("CosName".equalsIgnoreCase(attrName)) {
    		cosName = "cos:" + attrValue;
    		System.out.println("cosName=" + cosName);
    		return;
    	}
    	else if(attrName.startsWith("MOIP")){
    		profile.addAttributeValue(attrName, attrValue);
    	}

    }

    private boolean readConfigFile(String configFile) {
		 try {
	    		System.out.println("readConfigFile..." + configFile);

		        BufferedReader in = new BufferedReader(new FileReader(configFile));
		        String str;
		        while ((str = in.readLine()) != null) {
		            processConfigFile(str);
		        }
		        in.close();
		 } catch (IOException e) {
	    		System.out.println("Exception at readConfigFile :" + e.getMessage());
	    		return false;
		 }
		 return true;
    }

    private void processConfigFile(String lineStr) {
    	String attrName  = lineStr.substring(0, lineStr.indexOf("="));
    	String attrValue = lineStr.substring(lineStr.indexOf("=")+1);

    	if ("MCDHOST".equalsIgnoreCase(attrName)) {
    		DEFAULT_MCD_HOST = attrValue;
    		System.out.println("MCD HOST=" + DEFAULT_MCD_HOST);
    	}
    	else if ("MCDPORT".equalsIgnoreCase(attrName)) {
    		DEFAULT_MCD_PORT = Integer.parseInt(attrValue);
    		System.out.println("MCD PORT=" + DEFAULT_MCD_PORT);
    	}
    	else if ("MCDPOOLSIZE".equalsIgnoreCase(attrName)) {
    		DEFAULT_POOL_SIZE = Integer.parseInt(attrValue);
    		System.out.println("MCD POOL SIZE=" + DEFAULT_POOL_SIZE);
    	}
    	else {
    		System.out.println("Not the right config file: (MCDHOST=172.30.164.98, MCDPORT=389, MCDPOOLSIZE=5)");
    		System.exit(-1);
    	}
    }


    private void printHelp() {

    	System.out.println("Help: MoipMCDApiCmd \n\t " +
    			           "-cs [cs=create subscriber]  eg. subscriber_template.txt \n\t" +
    			           "-cc [cc=create cos]         eg. cos_template \n\t" +
    			           "-l  [l=lookup subscriber]  <identity eg. tel:5266> \n\t" +
    			           "-d  [d=delete susbscriber] <identity eg. tel:5266>");
    }

	public static void main(String[] args) {

		MoipMCDApiCmd mcdProv = new MoipMCDApiCmd();

		if (args.length < 1) {
			mcdProv.printHelp();
			System.exit(-2);
		}

		if (! mcdProv.parsingArgs(args) ) {
			mcdProv.printHelp();
			System.exit(-2);
		}

		System.out.println("Running MoipMCDApiCmd - done");
	}

}
