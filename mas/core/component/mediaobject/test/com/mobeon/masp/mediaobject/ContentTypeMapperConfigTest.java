/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.IGroup;


public class ContentTypeMapperConfigTest {

    private IConfiguration configuration;

	public ContentTypeMapperConfigTest() throws Exception {
		setUp();
	}

    @BeforeClass
    static public void startup() throws Exception {
        BasicConfigurator.configure();
    }


    @AfterClass
    static public void tearDown() {
    }

    protected void setUp() throws Exception {
    	configuration = getConfiguration("cfg/" + CommonOamManager.MAS_SPECIFIC_CONF);
    }

    /**
     *
     * @throws Exception if test case fails.
     */
    @Test
    public void testConfiguration() throws Exception {

        IGroup masSpecificGroup = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF);
		Map<String, Map<String, String>> contentTypeMapperTable = masSpecificGroup.getTable(ContentTypeMapperImpl.CONTENT_TYPE_MAPPER_TABLE);

		Set<String> contentTypeMapperKeySet = contentTypeMapperTable.keySet();
		Iterator<String> contentTypeMapperIterator = contentTypeMapperKeySet.iterator();


		while(contentTypeMapperIterator.hasNext()){
			String contentTypeMimeType = contentTypeMapperIterator.next();
			Map<String, String> contentTypeMap = contentTypeMapperTable.get(contentTypeMimeType);

			String fileExtName = contentTypeMap.get(ContentTypeMapperImpl.FILE_EXT_NAME);
			String codecMimeType = contentTypeMap.get(ContentTypeMapperImpl.CODEC_MIME_TYPES);

			if(contentTypeMimeType.equalsIgnoreCase("video/quicktime")){
				assertEquals(fileExtName, "mov");
				assertEquals(codecMimeType, "video/h263,audio/pcmu");
			}
			else if(contentTypeMimeType.equalsIgnoreCase("video/3gpp")){
				assertEquals(fileExtName, "3gp");
				assertEquals(codecMimeType, "video/h263,audio/amr");
			}else if(contentTypeMimeType.equalsIgnoreCase("audio/wav")){
				assertEquals(fileExtName, "wav");
				assertEquals(codecMimeType, "audio/pcmu");
			}else if(contentTypeMimeType.equalsIgnoreCase("audio/3gpp")){
				assertEquals(fileExtName, "3gp");
				assertEquals(codecMimeType, "audio/amr");
			}else if(contentTypeMimeType.equalsIgnoreCase("text/plain")){
				assertEquals(fileExtName, "txt");
				assertEquals(codecMimeType, "text/plain");
			}else {
				fail();
			}
		}
    }

    public IConfiguration getConfig() {
    	return configuration;
    }

    private IConfiguration getConfiguration(String... files) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(files);
        return configurationManager.getConfiguration();
    }

}
