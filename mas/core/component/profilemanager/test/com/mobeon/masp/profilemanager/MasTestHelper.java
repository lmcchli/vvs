package com.mobeon.masp.profilemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import com.abcxyz.messaging.common.util.FileExtension;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;

public class MasTestHelper {
	private static String oldFileLinkValue = null;
	private static String mfsTestPath =null;
	private static String schedulerTestPath = null;
	
	private static List<String> configFilenames = new Vector<String>();
	
	public static String getMfsTestRootPath(){
		return CommonOamManager.getInstance().getMfsOam().getConfigManager().getParameter(MfsConfiguration.MfsRootPath);
	}

	public static void setupMasTestConfiguration(String testName){
		try {
			oldFileLinkValue = SystemPropertyHandler.getProperty("abcxyz.mfs.filelink");
			SystemPropertyHandler.setProperty("abcxyz.mfs.filelink", "com.abcxyz.messaging.mfs.util.WindowsLink");
			System.setProperty("abcxyz.mfs.filelink", "com.abcxyz.messaging.mfs.util.WindowsLink");
			FileExtension tempDir = FileExtension.createTempDir(testName);
			String configPath = createTestConfiguration(tempDir);
			System.out.println("Created directory for config files " + configPath);
			String platform = System.getProperty("sun.arch.data.model");
			if (platform.equals("32")) {
				SystemPropertyHandler.setProperty("abcxyz.mfs.libpath", "linkutils-Linux_x86_32");
			}

			File[] rootList = File.listRoots();
			if (rootList != null && rootList.length > 0) {
				mfsTestPath = new File(rootList[0].getAbsolutePath() + mfsTestPath).getAbsolutePath();
				schedulerTestPath = new File(rootList[0].getAbsolutePath() + schedulerTestPath).getAbsolutePath();
			}

			configFilenames.add(configPath + File.separator + "backend.conf");
			IConfiguration configuration = new ConfigurationImpl(null,configFilenames,false);
			CommonOamManager.getInstance().setConfiguration(configuration);

			System.setProperty("abcxyz.mfs.userdir.create", "true");
			System.setProperty("abcxyz.messaging.scheduler.memory", "true");

			CommonOamManager.getInstance().getMfsOam().getConfigManager().setParameter(MfsConfiguration.MfsRootPath, mfsTestPath);
			CommonOamManager.getInstance().getMrdOam().getConfigManager().setParameter(DispatcherConfigMgr.EventsRootPath, schedulerTestPath);
			CommonOamManager.getInstance().getMrdOam().getConfigManager().setParameter(DispatcherConfigMgr.SchedulerID, "0"); //0 for NTF, 100 for MAS
			(new File(mfsTestPath)).mkdirs();
			(new File(schedulerTestPath)).mkdirs();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void copyConfigurationFiles(){
		File cfgDirectory = new File("cfg" + File.separator);
		cfgDirectory.mkdirs();

		String curDir = System.getProperty("user.dir") + File.separator;
		FileExtension originalBackendConfDirectory = new FileExtension(curDir + ".." + File.separator + "ipms_sys2" + File.separator + "backend" + File.separator + "cfg");
		FileExtension originalBackendConf = new FileExtension(originalBackendConfDirectory.getAbsolutePath() + File.separator + "backend.conf");
		FileExtension originalBackendXsd = new FileExtension(originalBackendConfDirectory.getAbsolutePath() + File.separator + "backend.xsd");
		FileExtension originalComponentServicesConf = new FileExtension(originalBackendConfDirectory.getAbsolutePath() + File.separator + "componentservices.cfg");
		
		File myBackendConf = new FileExtension(cfgDirectory.getAbsolutePath() + File.separator + "backend.conf");
		File myBackendXsd = new FileExtension(cfgDirectory.getAbsolutePath() + File.separator + "backend.xsd");
		File myComponentServicesConf = new FileExtension(cfgDirectory.getAbsolutePath() + File.separator + "componentservices.cfg");
		try {
			originalBackendConf.copyFile(myBackendConf);
			originalBackendXsd.copyFile(myBackendXsd);
			originalComponentServicesConf.copyFile(myComponentServicesConf);
		}
		catch (Exception e){
			System.err.println("Oups, big problem copying files: " + e.getMessage());
		}
		//backend.conf
	}
	
	private static String createTestConfiguration(FileExtension aDir){
		File configDirectory = new File(aDir + File.separator + "cfg");
		if (!configDirectory.exists()){
			configDirectory.mkdirs();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n");
		sb.append("	<xs:element name=\"Abcxyz.config\" type=\"Abcxyz.configType\"/>\n");
		sb.append("	<xs:complexType name=\"Abcxyz.configType\">\n");
		sb.append("		<xs:all>\n");
		sb.append("					<xs:element name=\"isHash\" type=\"xs:boolean\" fixed=\"true\" minOccurs=\"0\" maxOccurs=\"0\" />\n");
		sb.append("					<xs:element name=\"configFileType\" type=\"xs:integer\" fixed=\"5\" minOccurs=\"0\" maxOccurs=\"0\">\n");
		sb.append("					</xs:element>\n");
		sb.append("<xs:element name=\"Abcxyz.component-specific\" type=\"ComponentSpecificType\"/>\n");
		sb.append("</xs:all>\n");
		sb.append("</xs:complexType>\n");
		sb.append("				<xs:complexType name=\"ComponentSpecificType\" mixed=\"true\">\n");
		sb.append("		<xs:all>\n");
		sb.append("			<xs:element name=\"Abcxyz.ComponentSpecificConfigItem\" type=\"ComponentSpecificConfigItemType\"/>\n");
		sb.append("		</xs:all>\n");
		sb.append("</xs:complexType>\n"); 
		
		sb.append("<xs:complexType name=\"ComponentSpecificConfigItemType\" mixed=\"true\">\n");
		sb.append("<xs:all>\n");
		sb.append("		<xs:element name=\"Cm.masComponentName\"	type=\"xs:string\" default=\"mas1@PL_2_4\"  minOccurs=\"0\"/>\n"); 
		sb.append("		<xs:element name=\"Cm.ntfComponentName\"	type=\"xs:string\" default=\"ntf1@PL_2_4\" minOccurs=\"0\"/>\n"); 
		sb.append("		<xs:element name=\"Cm.mccComponentName\"	type=\"xs:string\" default=\"mcc1@PL_2_3\" minOccurs=\"0\"/>\n"); 
		sb.append("		<xs:element name=\"Cm.masEventsRootPath\"	type=\"xs:string\" default=\"/opt/moip/events/mas\" minOccurs=\"0\"/>\n"); 
		sb.append("		<xs:element name=\"Cm.masRetrySchema\"	type=\"xs:string\" default=\"30 30 30 STOP\" minOccurs=\"0\"/>\n"); 
		sb.append("		<xs:element name=\"Cm.masExpireTimeInMinute\"	type=\"xs:integer\" default=\"0\" minOccurs=\"0\"/>\n"); 
		sb.append("		<xs:element name=\"Cm.profilerEnabled\"	type=\"xs:boolean\" default=\"false\" minOccurs=\"0\"/>\n"); 

		sb.append("</xs:all>\n");		
		sb.append("</xs:complexType>\n");
		sb.append("</xs:schema>\n");
		
		PrintWriter out;
		try {
			out = new PrintWriter(new File(configDirectory + File.separator + "backend.xsd"));
			out.print(sb.toString());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder sbConf = new StringBuilder();
		sbConf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<Abcxyz.config xmlns:xlink=\"http://www.w3.org/TR/2000/REC-XLINK-20010627\" xsi:noNamespaceSchemaLocation=\"backend.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Abcxyz.component-specific><Abcxyz.ComponentSpecificConfigItem></Abcxyz.ComponentSpecificConfigItem></Abcxyz.component-specific></Abcxyz.config>\n");
		try {
			out = new PrintWriter(new File(configDirectory+ File.separator +"backend.conf"));
			out.print(sbConf.toString());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return configDirectory.getAbsolutePath();
	}
	
	public static void tearDownMasTestConfiguration(String testName){
		try {
			FileExtension tempDir = FileExtension.createTempDir(testName);
			tempDir.delete();
			tempDir = new FileExtension(mfsTestPath);
			tempDir.delete();
			tempDir = new FileExtension(schedulerTestPath);
			tempDir.delete();

			if (oldFileLinkValue != null){
				SystemPropertyHandler.setProperty("abcxyz.mfs.filelink",oldFileLinkValue);
				System.setProperty("abcxyz.mfs.filelink", oldFileLinkValue);

			}
		}
		catch (Exception e){
		}
	}
}
