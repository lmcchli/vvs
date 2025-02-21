package com.mobeon.masp.stream;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import jakarta.activation.MimeType;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-okt-03
 * Time: 09:26:36
 * To change this template use File | Settings | File Templates.
 */
public class MediaUtil {
    public enum Media { UNDEFINED, WAV, MOV, AMR_AUDIO, AMR_VIDEO, DTMF }

    private MimeType VIDEO_QUICKTIME;
    private MimeType VIDEO_3GPP;
    private MimeType AUDIO_3GPP;
    private MimeType AUDIO_WAV;


    public MediaUtil() throws Exception
    {
        VIDEO_QUICKTIME = new MimeType("video/quicktime");
        VIDEO_3GPP = new MimeType("video/3gpp");
        AUDIO_3GPP = new MimeType("audio/3gpp");
        AUDIO_WAV = new MimeType("audio/wav");
    }

    public Media toMedia(String media) {
        if (media.equals("WAV"))
            return Media.WAV;
        else if (media.equals("MOV"))
            return Media.MOV;
        else if (media.equals("AMR_AUDIO"))
            return Media.AMR_AUDIO;
        else if (media.equals("AMR_VIDEO"))
            return Media.AMR_VIDEO;
        else
            return Media.UNDEFINED;
    }

    public IMediaObject createPlayableMediaObject(String inputFileName, Media media) throws MediaObjectException {
        IMediaObject result = null;

        MediaObjectFactory factory = new MediaObjectFactory(10000);
        File f = new File(inputFileName);
        result = factory.create(f);
        switch (media) {
            case WAV:
                result.getMediaProperties().setContentType(AUDIO_WAV);
                break;

            case MOV:
                result.getMediaProperties().setContentType(VIDEO_QUICKTIME);
                break;

            case AMR_VIDEO:
                result.getMediaProperties().setContentType(VIDEO_3GPP);
                break;

            case AMR_AUDIO:
                result.getMediaProperties().setContentType(AUDIO_3GPP);
                break;

            default:
                break;
        }
        result.getMediaProperties().addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS, 0);


        return result;
    }

    public IMediaObject createRecordableMediaObject(Media media) throws Exception{
        IMediaObject result = null;
        MimeType mimeType = null;

        MediaObjectFactory factory = new MediaObjectFactory(1000000);
        result = factory.create();

        switch (media) {
            case WAV:
                mimeType = AUDIO_WAV;
                break;

            case MOV:
                mimeType = VIDEO_QUICKTIME;
                break;

            case AMR_VIDEO:
                mimeType = VIDEO_3GPP;
                break;

            case AMR_AUDIO:
                mimeType = AUDIO_3GPP;
                break;

            default:
                break;
        }
        result.getMediaProperties().setContentType(mimeType);
        return result;
    }

    public Collection<RTPPayload> getPayloads(Media media) {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        switch (media) {
            case WAV:
                list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
                break;

            case MOV:
                list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
                list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
                break;

            case AMR_VIDEO:
                list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
                list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
                break;

            case AMR_AUDIO:
                list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
                break;
            case DTMF:
                list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
                list.add(RTPPayload.get(RTPPayload.AUDIO_DTMF));
                break;

            default:
                break;
        }

        return list;
    }

    /*public MediaMimeTypes getMimeTypes(Media media) {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();

        switch (media) {
            case WAV:
                mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
                break;

            case MOV:
                mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
                mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
                break;

            case AMR_VIDEO:
                mediaMimeTypes.addMimeType(RTPPayload.AUDIO_AMR);
                mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
                break;

            case AMR_AUDIO:
                mediaMimeTypes.addMimeType(RTPPayload.AUDIO_AMR);
                break;

            default:
                break;
        }
        return mediaMimeTypes;
    }*/

    public void save(IMediaObject mo, String name) {
        try {
            InputStream is = mo.getInputStream();
            File f = new File(System.getProperty("user.dir") +
                    File.separator + name);
            FileOutputStream os = new FileOutputStream(f);
            int b = -1;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
            os.close();
            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
