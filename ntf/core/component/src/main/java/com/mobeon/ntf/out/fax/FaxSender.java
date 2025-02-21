package com.mobeon.ntf.out.fax;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.FaxPrintEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.management.ManagementStatus;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.eclipse.angus.mail.smtp.SMTPTransport;

public class FaxSender  extends NtfThread
{
    final static public String AT_FAXHOST ="@fax";
    private ManagedArrayBlockingQueue<Object> faxWorkerQueue;
    private ManagedArrayBlockingQueue<Object> faxSenderQueue;
    private LogAgent log = NtfCmnLogger.getLogAgent(FaxSender.class);
    private String faxServerHost=null;
    private String faxServerPort=null;
    private long nextCheck = 0;
    private boolean isFaxConnectionPoller=false;
    private static boolean isFaxConnectionCheckNeeded=true;

    public FaxSender(ManagedArrayBlockingQueue<Object> faxSenderQueue,ManagedArrayBlockingQueue<Object> faxWorkerQueue,
                     String threadName,boolean isFaxConnectionPoller)
    {
        super(threadName);
        this.faxWorkerQueue = faxWorkerQueue;
        this.faxSenderQueue = faxSenderQueue;
        this.isFaxConnectionPoller=isFaxConnectionPoller;
        this.nextCheck = NtfTime.now+60;
    }

    /**
     * Do one step of the work.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        FaxPrintEvent faxPrintEvent = null;
        String host=null;
        String port=null;

        try {
            // Get an event from the working queue
            if(isFaxConnectionPoller) isFaxConnectionCheckNeeded=true;
            Object obj = faxSenderQueue.poll(10, TimeUnit.SECONDS);
            synchronized(this)
            {
                host=faxServerHost;
                port=faxServerPort;
            }

            if (obj == null && host!=null && port!=null){

                if(isFaxConnectionPoller)
                {

                    if(isFaxConnectionCheckNeeded) {
                        if (NtfTime.now >= nextCheck) {
                            checkConnection(host,port);
                            nextCheck = NtfTime.now + 60;
                        }
                    }

                }
                return false;
            }

            if (obj == null) {
                return false; //check management status
            }

            faxPrintEvent = (FaxPrintEvent)obj;
            if (log.isDebugEnabled())log.debug("Handle new fax to send in FaxSender: " + faxPrintEvent);
            if(host!=null && port!=null)
            {
                transmitfax(faxPrintEvent, host, port);
                faxWorkerQueue.put(faxPrintEvent);
            }
            else
            {
                //No fax server configured
                faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SEND_RETRY);
                faxWorkerQueue.put(faxPrintEvent);
            }

        }  catch (OutOfMemoryError me) {
            try {
                ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                log.error("NTF out of memory, shutting down... ", me);
            } catch (OutOfMemoryError me2) {;} //ignore second exception
            return true; //exit.
        } catch (Exception e) {
            log.error("Exception in Fax Print worker " ,e);
            if(faxPrintEvent != null){
                faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SEND_FAILED);

                try {
                    faxWorkerQueue.put(faxPrintEvent);
                } catch (Throwable t) {
                    // do nothing - let return status false
                }
            }
        }
        return false;
    }


    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (faxSenderQueue.size() == 0)
        {
                //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                if (faxSenderQueue.isIdle(2,TimeUnit.SECONDS)) {
                    return true;
                }
                else
                {
                    if (faxWorkerQueue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                        return(ntfRun());
                    } else
                    {
                        return true;
                    }

                }
        } else {
            return(ntfRun());
        }
    }

    private void checkConnection(String host, String port)
    {
        log.debug("checkConnection to fax server host: "+host+" port: "+port);
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.transport.protocol", "smtp");
        Session smtpSesssion = Session.getDefaultInstance(props);

        Transport transport=null;
        //Connecting to fax gateway
        try{
            transport = smtpSesssion.getTransport();
            transport.connect();
            connectionUp(host,port);
            transport.close();


        }
        catch(MessagingException e)
        {
            connectionDown(host,port);
        }

    }

    private void transmitfax(FaxPrintEvent faxPrintEvent,String host, String port) {

        SMTPMessage smtpmessage=null;
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.transport.protocol", "smtp");
        Session smtpSesssion = Session.getDefaultInstance(props);


        String from = getFaxSourceAddress(faxPrintEvent);

        //Generate the fax print smtp message
        try
        {
            smtpmessage = generateFaxMessage(faxPrintEvent, smtpmessage, smtpSesssion, from);
        }
        catch(Exception e)
        {
            log.error("transmitfax Exception while generating the fax print message giving up: "+faxPrintEvent,e);
            faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SEND_FAILED); //No need to retry since this error is permanent
            return;

        }

        Transport transport=null;
        //Connecting to fax gateway
        try{
            if (log.isDebugEnabled())log.debug("transmitfax Connecting to fax server: "+faxPrintEvent);
            transport = smtpSesssion.getTransport();
            transport.connect();
            connectionUp(host,port);

        }
        catch(MessagingException e)
        {
            log.error("transmitfax Unable to connect to fax server. will retry later: "+faxPrintEvent+ " Message: "+e.getMessage()+" Detail: "+e.toString(),e);
            faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SEND_RETRY); //retry later
            connectionDown(host,port);
            return;
        }

        try{
            if (log.isDebugEnabled())log.debug("transmitfax Start sending fax message: "+faxPrintEvent);
            SMTPTransport.send(smtpmessage);
            faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SEND_OK);
            if (log.isDebugEnabled())log.debug("transmitfax Fax send with success: "+faxPrintEvent);

        }

        catch(MessagingException e)
        {
            log.error("transmitfax Could not send fax print request, will retry later : "+faxPrintEvent+ " Message: "+e.getMessage()+" Detail: "+e.toString(),e);
            faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SEND_RETRY);

        }
        finally
        {
            try{

             transport.close();
            }
            catch(MessagingException e)
            {

            }
        }
    }



    private SMTPMessage generateFaxMessage(FaxPrintEvent faxPrintEvent, SMTPMessage smtpmessage, Session smtpSesssion, String from) throws MessagingException, IOException, MsgStoreException{
            NotificationEmail email =new NotificationEmail(faxPrintEvent);
            email.init();

            String faxPrintNumber = faxPrintEvent.getFaxPrintNumber();

            BodyPart tiffimage = (BodyPart) email.getFaxAttachmentPart();
            /*MimeBodyPart faxMessagePart = new MimeBodyPart();
            faxMessagePart.setContent(tiffimage, "image/tiff");*/
            MimeMultipart requestBody = new MimeMultipart();
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText("Fax Message ");

            requestBody.addBodyPart(mbp1);

            requestBody.addBodyPart(tiffimage);

            MimeMessage requestMail =
                new MimeMessage(smtpSesssion);
            requestMail.setContent(requestBody);
            requestMail.setRecipient(Message.RecipientType.TO,
                                     new InternetAddress(faxPrintNumber +AT_FAXHOST));

            requestMail.setHeader("Reply-to", "noreply" +AT_FAXHOST);
            requestMail.setHeader("Errors-to", "noreply" +AT_FAXHOST);

            requestMail.setHeader("From", from+ AT_FAXHOST);
            requestMail.setSubject("Fax Message");
            requestMail.saveChanges();

             smtpmessage = new SMTPMessage(requestMail);
            smtpmessage.setEnvelopeFrom(from+ AT_FAXHOST);
            smtpmessage.setNotifyOptions(SMTPMessage.NOTIFY_NEVER);
        return smtpmessage;
    }

    private String getFaxSourceAddress(FaxPrintEvent faxPrintEvent) {
        UserInfo profile = faxPrintEvent.getSubcriberProfile();

        String cosName =null;
        String from=null;
        if(profile!=null)
        {
            cosName = profile.getCosName();
        }
        SMSAddress sourceAddressFax = Config.getSourceAddress("faxprint",cosName);


        if(sourceAddressFax!=null)
        {
           from = sourceAddressFax.getNumber();
        }


        if(from==null || from.isEmpty())
        {
            if(profile!=null){
                from = profile.getInboundFaxNumber();
            }
        }

        if(from==null|| from.isEmpty())
        {
            log.error("Unable to get fax print source address for "+faxPrintEvent);
            from="1111111111";
        }
        return from;
    }

    public void connectionDown(String name, String port) {
      isFaxConnectionCheckNeeded=false;
      ManagementStatus mStatus = ManagementInfo.get().getStatus(NotificationConfigConstants.FAX_SERVER_TABLE, name);

      if (mStatus.isUp()  && name!=null &&  port!=null ) {
        mStatus.setHostName(name);
        try {
          mStatus.setPort(Integer.parseInt(port));
        } catch (java.lang.NumberFormatException e) {
            mStatus.setPort(25);
        }
        mStatus.setZone("Unspecified");
        mStatus.down();
        log.info("No connection to Fax server: " + name );
      }
    }
    public void connectionUp(String name, String port) {
      isFaxConnectionCheckNeeded=false;
      ManagementStatus mStatus = ManagementInfo.get().getStatus(NotificationConfigConstants.FAX_SERVER_TABLE, name);

      if (!mStatus.isUp() && name!=null &&  port!=null) {
          mStatus.setHostName(name);
          try {
            mStatus.setPort(Integer.parseInt(port));
          } catch (java.lang.NumberFormatException e) {
              mStatus.setPort(25);
          }
          mStatus.setZone("Unspecified");
          mStatus.down();
          log.info("Connection restored to Fax server: " + name );
        }
        mStatus.up();
    }



    /**
     * Method for updating cache of MailTransferAgents
     * with current MCR content. Called from timer.
     */
    public void updateConfig(String faxServerHost, String faxServerPort) {
        synchronized(this)
        {
            this.faxServerHost=faxServerHost;
            this.faxServerPort=faxServerPort;
            isFaxConnectionCheckNeeded=true;
        }
    }
}
