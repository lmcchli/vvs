package com.abcxyz.service.moip.common.cmnaccess;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.abcxyz.messaging.oe.common.configuration.ConfigurationConstants;
import com.abcxyz.messaging.oe.common.system.OE;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.CommonOamManagerTest;
import com.mobeon.common.configuration.ConfigurationException;

/**
 *
 * common utilities for testing
 */

public class CommonTestingSetup{

	private static String mfsTestPath = "/tmp/moip/mfs";
	private static String schedulerTestPath = "/tmp/moip/events";
	private static boolean setupDone = false;

	public static void setup() throws ConfigurationException, ConfigurationDataException {
		if (setupDone) {
			return;
		}

		setupDone = true;
		
		/*
		 * This code determines the JVM architecture to provide the name of the
		 * linkutils library to the MFS component. On UNIX, MFS loads a DSO to
		 * access some system functionality. So the property abcxyz.mfs.libpath
		 * is only effective on UNIX and is ignored on Windows.
		 * 
		 * The linkutils library must be specified with the LD_LIBRARY_PATH 
		 * environment variable.
		 * 
		 * This code has been added because the build machines did not have a 64-bit
		 * JVM installed so we needed a 32-bit version of the linkutils share library.
		 * It can be removed when they do.
		 */
        String userDir = System.getProperty("user.dir");
        System.setProperty("backendConfigDirectory", userDir + "/../ipms_sys2/backend/cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );
		System.setProperty("abcxyz.services.messaging.productPrefix","moip");
		
        String platform = System.getProperty("sun.arch.data.model");
        
		if (platform.equals("32")) {
			SystemPropertyHandler.setProperty("abcxyz.mfs.libpath", "linkutils-Linux_x86_32");
		}

		File[] rootList = File.listRoots();
		if (rootList != null && rootList.length > 0) {
			mfsTestPath = new File(rootList[0].getAbsolutePath() + mfsTestPath).getAbsolutePath();
			schedulerTestPath = new File(rootList[0].getAbsolutePath() + schedulerTestPath).getAbsolutePath();
			
			// Copy the topology config files to the test path
	        SystemPropertyHandler.setProperty(ConfigurationConstants.EMS_ROOT_ENV, rootList[0].getAbsolutePath()+"opt");
	        
	        String globalCommon = rootList[0].getAbsolutePath()+"opt"+File.separator+"global"+File.separator+"common";
            String mioCommon = rootList[0].getAbsolutePath()+"opt"+File.separator+"mio"+File.separator+"common";
            String moipConfig = rootList[0].getAbsolutePath()+"opt"+File.separator+"moip"+File.separator+"config";
            String configFilesSourceDir = System.getProperty("user.dir") + File.separator+".."+File.separator+"msgcore_oe"+File.separator+"cfg";
            
            (new File(mioCommon)).mkdirs();
            (new File(globalCommon)).mkdirs();
            (new File(moipConfig)).mkdirs();
            
            String mioCommonConfigFilesName[] = {
                    "topology.conf",
                    "topology.xsd"
            };
            for (String fileName : mioCommonConfigFilesName) {
                CommonTestingSetup.copyFile(new File(configFilesSourceDir, fileName), new File(mioCommon, fileName));
            }
            
            String globalCommonConfigFilesName[] = {
                    "formattingrules.conf",
                    "formattingrules.xsd",
                    "messagingservices.conf",
                    "mnrSpecific.conf",
                    "mnrSpecific.xsd",
                    "ssmgSpecific.conf",
                    "ssmgSpecific.xsd",
                    "slaTable.xml",
                    "slaTable.xsd",
                    "numberAnalysisTable.xml",
                    "numberAnalysisTable.xsd"
            };
            for (String fileName : globalCommonConfigFilesName) {
                CommonTestingSetup.copyFile(new File(configFilesSourceDir, fileName), new File(globalCommon, fileName));
            }

            String moipConfigFilesName[] = {
                    "oe.xsd",
                    "oe.conf",
                    "lerSpecific.xsd",
                    "lerSpecific.conf",
                    "services.xsd",
                    "services.xml"
            };
            for (String fileName : moipConfigFilesName) {
                CommonTestingSetup.copyFile(new File(configFilesSourceDir, fileName), new File(moipConfig, fileName));
            }

		}
		
		//set MFS root path
		CommonOamManagerTest.initOam();

	    System.setProperty("abcxyz.mfs.userdir.create", "true");
        System.setProperty("abcxyz.messaging.scheduler.memory", "true");

		CommonOamManager.getInstance().getMfsOam().getConfigManager().setParameter(MfsConfiguration.MfsRootPath, mfsTestPath);
		CommonOamManager.getInstance().getMrdOam().getConfigManager().setParameter(DispatcherConfigMgr.EventsRootPath, schedulerTestPath);
		CommonOamManager.getInstance().getMrdOam().getConfigManager().setParameter(DispatcherConfigMgr.SchedulerID, "0"); //0 for NTF, 100 for MAS
		(new File(mfsTestPath)).mkdirs();
		(new File(schedulerTestPath)).mkdirs();

	}

	public static void tearDown() {
    	deleteDir(new File(mfsTestPath));
	}

    public static void deleteDir(String dir) {
    	deleteDir(new File(dir));
    }

    public static void deleteMfsDir() {
    	deleteDir(new File(mfsTestPath));
    }

    public static void deleteSchedulerDir() {
    	deleteDir(new File(schedulerTestPath));
    }

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }

        // The directory is now empty so delete it
        dir.delete();
    }
    
    public static String getMfsTestPath() {
    	return mfsTestPath;
    }
    
    public static String getSchedulerTestPath() {
    	return schedulerTestPath;
    }

    private static void copyFile(File srcFile, File dstFile){
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
              out.write(buf, 0, len);
            }
          } catch(FileNotFoundException ex){
              System.out.println(ex.getMessage() + " in the specified directory.");
          } catch(IOException e){
              System.out.println(e.getMessage());      
          } finally {
              if (in != null) {
                  try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
              }
              if (out != null) {
                  try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
              }
          }
        }
}
