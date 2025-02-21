package com.mobeon.common.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.activation.MimetypesFileTypeMap;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;


public class BinaryContent implements ContentPart {    
    private static MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
    
    private String name = null;
    private String type = null ;
    private byte[] content = null;
    
    public BinaryContent(String partName, String type, byte[] content) {
        this.name = partName;
        this.type = type;
        this.content = content;
    }
    
    public String getPartName() {        
        return name;
    }
   
    public String getContentType() {      
        return type;
    }
    
    public MimeBodyPart getMimeBodyPart() {
        try {
            MimeBodyPart part = new MimeBodyPart();
            part.setFileName(name);
            part.setContent(content, type);   
            return part;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }    
    
    public byte[] getBytes() {
        return content;
    }

    //good candidate for refactoring into transcodefacade
    public static String mapFileExtension(String contentType) {
    	if (contentType != null) {
    		if(contentType.equalsIgnoreCase("audio/amr")) {
    			return "amr";
            } else if (contentType.equalsIgnoreCase("audio/mp3")) {
                return "mp3";
    		} else if (contentType.equalsIgnoreCase("audio/wav")) {
    			return "wav";
    		} else if (contentType.equalsIgnoreCase("video/mpeg")) {
    			return "mpg";    			
            } else if (contentType.equalsIgnoreCase("video/3gpp")) {
                return "3gp";
            } else if (contentType.equalsIgnoreCase("video/quicktime")) {
                return "mov";
            }
    	}    	
    	return null;
    }

    public static String makeNewFileName(String origFileName, String targetContentType) {    	    	
    	if(origFileName != null && targetContentType != null) {
    		String newExt = mapFileExtension(targetContentType);        	
    		if (newExt != null && !origFileName.endsWith("." + newExt)) { //check we actually changed the type    			    			
    			String[] fileName = origFileName.split("\\.");     	 		
    			if (fileName != null && fileName.length > 1) {
    				String newFileName = "";
    				for (int i =0; i < fileName.length-1; i++) {
    					newFileName += fileName[i];
    				}
    				newFileName += "." + newExt;
    				return newFileName;
    			}
    		}
    	}    	
    	return origFileName;
    }

    public static byte[] readBytes(InputStream is) throws IOException {        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[16384]; //16K buffer
        int nRead = 0;          
        while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {           
            bytes.write(buffer, 0, nRead);                
        }     
        return bytes.toByteArray();   
    }
    
    public static BinaryContent readBinaryContent(File file) throws IOException {
        byte[] bytes = BinaryContent.readBytes(new FileInputStream(file));
        String fileName = file.getName();
        String type = mimeTypesMap.getContentType(file);
        return new BinaryContent(fileName, type, bytes);
    }

}
