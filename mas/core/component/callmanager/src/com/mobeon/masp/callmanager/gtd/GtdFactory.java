package com.mobeon.masp.callmanager.gtd;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.NumberCompletion;
import com.mobeon.masp.callmanager.RedirectingParty;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

/**
 /**
  * This Gtd description factory is used to parse a received GTD and return
  * a parsed {@link com.mobeon.masp.callmanager.gtd.GtdDescription}.
  *
  * @author Malin Flodin
  */

public class GtdFactory {

    private static final ILogger log = ILoggerFactory.getILogger(GtdFactory.class);

    /**
     * This method is used to parse a remote GTD and returns a parsed
     * {@link com.mobeon.masp.callmanager.gtd.GtdDescription}
     * <p>
     *
     * @param   gtd
     * @return  A parsed representation of the gtd. null if there was no GTD.
     */
    public static GtdDescription parseGtd(String gtd) throws GtdParseException {

        GtdDescription gtdDescription = null;

        if (log.isDebugEnabled())
            log.debug("Parsing GTD: " + gtd);

        if (gtd != null) {
            gtdDescription = new GtdDescription();

            BufferedReader bufferedReader = new BufferedReader(new StringReader(gtd));
            String line;
            do{
                try {
                    line = bufferedReader.readLine();
                    if(line != null && line.startsWith(SipConstants.GTD_CGN)){
                        NumberCompletion numberCompletion = cgnLineToNumberCompletion(line);
                        GtdCgn gtdCgn = new GtdCgn();
                        gtdCgn.setNumberCompletion(numberCompletion);
                        gtdDescription.setGtdCgn(gtdCgn);
                    }
                    if(line != null && line.startsWith(SipConstants.GTD_RNI)){
                        RedirectingParty.RedirectingReason redirectingReason = rniLineToReason(line);
                        GtdRni gtdRni = new GtdRni();
                        gtdRni.setRedirectingReason(redirectingReason);
                        gtdDescription.setGtdRni(gtdRni);
                    }

                } catch (IOException e) {
                    throw new GtdParseException("Failed to parse GTD", e);
                }
            } while(line != null);
        }

        if (log.isDebugEnabled())
            log.debug("Parsed GtdDescription: " + gtdDescription);

        return gtdDescription;
    }

    /**
     * Parses a  GTD_CGN line and returns the found number completion. It is
     * assumed that the line is a GTD_CGN line, so do not invokes with other lines.
     * @param line The GTD_CGN line
     * @return the NumberCompletion found
     */
    private static NumberCompletion cgnLineToNumberCompletion(String line) {

        // number completion field is field 3 in a string looking like:
        // CGN,04,y,jjjj
        // or maybe
        // CGN,04,y

        NumberCompletion numberCompletion = NumberCompletion.UNKNOWN;

        int index = 0;
        String value = "";  // value of the numberCompletion field
        StringTokenizer st = new StringTokenizer(line, ",");
        while (st.hasMoreTokens()) {
            if(index == 2){
                value = st.nextToken();
                break;
            }
            st.nextToken();
            index++;
        }
        if(value != null){
            if(value.equals("y"))
                numberCompletion = NumberCompletion.COMPLETE;
            else if(value.equals("n"))
                numberCompletion = NumberCompletion.INCOMPLETE;
        }

        if(log.isDebugEnabled())
            log.debug("Returning "+numberCompletion+" for GTD_CGN line "+line);
        return numberCompletion;
    }

                    /**
     * Parses a  GTD_RNI line and returns the found reason. It is
     * assumed that the line is a GTD_RNI line, so do not invokes with other lines.
     * @param line The GTD_RNI line
     * @return the Reason found
     */
    private static RedirectingParty.RedirectingReason rniLineToReason(String line) {

        // reason field is field 5 in a string looking like:
        // RNI,03,u,02,u
        // or maybe
        // RNI,03,1,03,3

        RedirectingParty.RedirectingReason redirectingReason = RedirectingParty.RedirectingReason.UNKNOWN;

        int index = 0;
        String value = "";  // value of the redirectingReason field
        StringTokenizer st = new StringTokenizer(line, ",");
        while (st.hasMoreTokens()) {
            if(index == 4){ //RNI,ri,orr,rc,rr   where rr (redirecting reason corresponds to index=4 or 4'th
                            //field after RNI
                value = st.nextToken();
                break;
            }
            st.nextToken();
            index++;
        }
        if(value != null){
            if(value.equals("u")||value.equals("rr=u"))
                redirectingReason = RedirectingParty.RedirectingReason.UNKNOWN;
            else if(value.equals("1")||value.equals("rr=1"))
                redirectingReason = RedirectingParty.RedirectingReason.USER_BUSY;
            else if(value.equals("2")||value.equals("rr=2"))
                redirectingReason = RedirectingParty.RedirectingReason.NO_REPLY;
            else if(value.equals("3")||value.equals("rr=3"))
                redirectingReason = RedirectingParty.RedirectingReason.UNCONDITIONAL;
            else if(value.equals("4")||value.equals("rr=4"))
                redirectingReason = RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING;
            else if(value.equals("5")||value.equals("rr=5"))
                redirectingReason = RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE;
            else if(value.equals("6")||value.equals("rr=6"))
                redirectingReason = RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE;
            //all other values are reserved and mapped to UNKNOWN
        }

        if(log.isDebugEnabled())
            log.debug("Returning "+redirectingReason+" for GTD_RNI line "+line);
        return redirectingReason;
    }

}
