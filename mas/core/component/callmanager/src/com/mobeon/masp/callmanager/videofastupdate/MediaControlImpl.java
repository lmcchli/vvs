/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.videofastupdate;

import com.mobeon.masp.callmanager.videofastupdate.xml.XmlMediaControlDocumentBean;
import com.mobeon.masp.callmanager.videofastupdate.xml.XmlMediaControlDocumentBean.MediaControl;
import com.mobeon.masp.callmanager.videofastupdate.xml.XmlVcPrimitiveBean;
import com.mobeon.masp.callmanager.videofastupdate.xml.XmlToEncoderBean;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * This class parses and creates XML documents containing media control
 * information as described in
 * <bold>draft-levin-mmusic-xml-media-control-03.txt</bold>
 * @author Malin Flodin
 */
public class MediaControlImpl {

    private static final MediaControlImpl INSTANCE = new MediaControlImpl();

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // XML document containing a Picture Fast Update request
    private String pictureFastUpdateRequest = null;

    /**
     * @return The single MediaControlImpl instance.
     */
    public static MediaControlImpl getInstance() {
        return INSTANCE;
    }


    /**
     * Creates the single MediaControlImpl instance.
     */
    private MediaControlImpl() {
    }

    /**
     * Parses an XML document that should contain a media control document as
     * described in
     * <bold>draft-levin-mmusic-xml-media-control-03.txt</bold>
     * and returns the list of general_error elements.
     * @param xmlDocument
     * @return a list of the general_errors. An empty list is returned if no
     * errors exist. Null is never returned.
     * @throws XmlException if the document could not be parsed as a media
     * control document.
     */
    public List<String> getGeneralErrors(String xmlDocument) throws XmlException {
        List<String> errors = new ArrayList<String>();

        XmlMediaControlDocumentBean mediaControlDocument =
                XmlMediaControlDocumentBean.Factory.parse(xmlDocument);

        if (log.isDebugEnabled())
            log.debug("Media control XML document is parsed.");

        MediaControl mediaControl = mediaControlDocument.getMediaControl();

        if (mediaControl != null) {
            errors = mediaControl.getGeneralErrorList();
        }

       return errors;
    }

    /**
     * Creates and returns an XML document containin a picture fast update
     * request according to
     * <bold>draft-levin-mmusic-xml-media-control-03.txt</bold>.
     * @return A string contaning a media control xml document with the
     * picture fast update request.
     * @throws IOException if the picture fast update request could not be
     * created.
     */
    public String createPictureFastUpdateRequest() throws IOException {

        if (pictureFastUpdateRequest == null) {
            if (log.isDebugEnabled())
                log.debug("A picture fast update request is created.");

            // Set the options used when creating the XML document
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setCharacterEncoding("utf-8");
            xmlOptions.setUseDefaultNamespace();
            xmlOptions.setSavePrettyPrint();

            // Create document
            XmlMediaControlDocumentBean mediaControlDocument =
                    XmlMediaControlDocumentBean.Factory.newInstance();

            // Create MediaControl
            MediaControl mediaControl = mediaControlDocument.addNewMediaControl();

            // Create VcPrimitive
            XmlVcPrimitiveBean vcPrimitive = mediaControl.addNewVcPrimitive();

            // Create ToEncoder with command Picture Fast Update
            XmlToEncoderBean toEncoder = vcPrimitive.addNewToEncoder();
            toEncoder.addNewPictureFastUpdate();

            // Save the XML document to an OutputStream.
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mediaControlDocument.save(bos, xmlOptions);

            pictureFastUpdateRequest = bos.toString();
        }

        return pictureFastUpdateRequest;
    }

}