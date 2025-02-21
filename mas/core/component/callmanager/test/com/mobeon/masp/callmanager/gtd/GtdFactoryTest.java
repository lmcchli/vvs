package com.mobeon.masp.callmanager.gtd;

import com.mobeon.masp.callmanager.NumberCompletion;
import com.mobeon.masp.callmanager.RedirectingParty;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-feb-02
 * Time: 21:03:19
 * To change this template use File | Settings | File Templates.
 */
public class GtdFactoryTest extends junit.framework.TestCase {

    public void test() throws Exception {

        // Verify that a CGN with numbercompletion=y is correctly parsed
        GtdDescription gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,y,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.COMPLETE);

        // Verify that a GTD with numbercompletion=n is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,n,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.INCOMPLETE);

        // Verify that a GTD with numbercompletion=u is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,u,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a GTD with numbercompletion=Y is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,Y,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a GTD with numbercompletion=N is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,N,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a GTD with numbercompletion empty is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a GTD where the GTD line is missing is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a GTD with unrecognized numbercompletion is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,g,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify another variant that a GTD with unrecognized numbercompletion is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,ydfuurufg,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);


        // Verify that a GTD with lowercase GTD is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "cgn,04,y,1,y,4,1133\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a GTD so short that there is no numbercompletion field,
        // is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify another variant where the GTD is so short that there is no numbercompletion field,
        // is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,04,\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        // Verify that a null GTD is correctly parsed
        gtdDescription = GtdFactory.parseGtd(null);
        assertTrue(gtdDescription == null);

        // Verify that an empty string CNG is correctly parsed
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getCallingPartyCompletion() == NumberCompletion.UNKNOWN);

        //Test of RNI (Redirecting Information) proceed here
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,6\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,u\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,1\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,2\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.NO_REPLY);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,3\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNCONDITIONAL);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,4\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,5\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,F\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

        //===============

         gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=6\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=u\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=1\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=2\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.NO_REPLY);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=3\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNCONDITIONAL);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=4\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "RNI,03,N,1,rr=5\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);


        //===============

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,aa1f2adec97611d9adcc0003ba909185\r\n" +
                        "RNI,03,N,1,F\r\n"+
                        "CGN,04,,1,y,4,1133,aa1f2adec97611d9adcc0003ba909185\r\n" +
                        "RNI,03,N,1,5\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);

        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\n" +
                        "CGN,aa1f2adec97611d9adcc0003ba909185\r\n" +
                        "RNI,03,N,1,F\r\n"+
                        "CGN,04,,1,y,4,1\r\n\"+133,aa1f2adec97611d9adcc0003ba909185\r\n" +
                        "RNI,03,N,1,5,\r\n"+
                        "GCI,aa1f2adec97611d9adcc0003ba909185\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);

        //Test cases from MVAS
             // Order of IAM and RNI
  //doAssertRNI("1.1", count, fail, "",                                0, 0,1,0,1);
        //Needed ??
        gtdDescription = GtdFactory.parseGtd(
                "");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("1.2", count, fail, "\r\nXXX,0,1,0,1\r\n",             0, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "\r\nXXX,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("1.3", count, fail, "XXX\r\nRNI,0,1,0,1\r\n",          0, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "XXX\r\nRNI,0,1,0,1\r\n");
        //TODO: returns USER_BUSY OK?
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("1.4", count, fail, "IAM\r\nXXX,0,1,0,1\r\n",          0, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nXXX,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("1.5", count, fail, "RNI,0,1,0,1\r\nIAM\r\n",          0, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "RNI,0,1,0,1\r\nIAM\r\n");
        //TODO: returns USER_BUSY OK?
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("1.6", count, fail, "IAM\r\nXXX,RNI,0,1,0,1\r\n",      0, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nXXX,RNI,0,1,0,1\r\n");
        //TODO: returns USER_BUSY OK?
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("1.7", count, fail, "XXX,IAM\r\nRNI,0,1,0,1\r\n",      0, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "XXX,IAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);
        //returns USER_BUSY although XXX, preceeds IAM (TODO: is this OK? Or should it be Unknown ?)


  // IAM and RNI somewhere
  //doAssertRNI("2.1",  count, fail, "IAM\r\nRNI,0,1,0,1\r\n",          4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.2",  count, fail, "XXX\r\nIAM\r\nRNI,0,1,0,1\r\n",   4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "XXX\r\nIAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.3",  count, fail, "IAM\r\nRNI,0,1,0,1\r\nXXX\r\n",   4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nRNI,0,1,0,1\r\nXXX\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.4",  count, fail, "IAM\r\nXXX,4\r\nRNI,0,1,0,1\r\n", 4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nXXX,4\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.11", count, fail, "IAM\nRNI,0,1,0,1\n",              4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\nRNI,0,1,0,1\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.12", count, fail, "XXX\nIAM\nRNI,0,1,0,1\n",         4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "XXX\nIAM\nRNI,0,1,0,1\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.13", count, fail, "IAM\nRNI,0,1,0,1\nXXX\n",         4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\nRNI,0,1,0,1\nXXX\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.14", count, fail, "IAM\nXXX,4\nRNI,0,1,0,1\n",       4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\nXXX,4\nRNI,0,1,0,1\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.21", count, fail, "IAM\n\rRNI,0,1,0,1\n\r",          4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\n\rRNI,0,1,0,1\n\r");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.22", count, fail, "XXX\n\rIAM\n\rRNI,0,1,0,1\n\r",   4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "XXX\n\rIAM\n\rRNI,0,1,0,1\n\r");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.23", count, fail, "IAM\n\rRNI,0,1,0,1\n\rXXX\n\r",   4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\n\rRNI,0,1,0,1\n\rXXX\n\r");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.24", count, fail, "IAM\n\rXXX,4\n\rRNI,0,1,0,1\n\r", 4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\n\rXXX,4\n\rRNI,0,1,0,1\n\r");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.25", count, fail, "IAM\n\rXXX,4\n\rIAM\n\rRNI,0,1,0,1\n\r", 4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\n\rXXX,4\n\rIAM\n\rRNI,0,1,0,1\n\r");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("2.26", count, fail, "IAM\n\rRNI,0,1,0,1\n\rXXX,4\n\rRNI,6,7,8,9\n\r", 4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\n\rRNI,0,1,0,1\n\rXXX,4\n\rRNI,6,7,8,9\n\r");
        //TODO: returns UNKNOWN OK ?  Or should USER_BUSY be returned from the first RNI field ?
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);


  // RNI parameter non presence
  //doAssertRNI("3.1",  count, fail, "IAM\r\nRNI",                     -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nRNI");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.2",  count, fail, "IAM\r\nRNI,",                    -1, 0,1,0,1);
          gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nRNI,");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.3",  count, fail, "IAM\r\nRNI,0",                   -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nRNI,0");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.4",  count, fail, "IAM\r\nRNI,0,",                  -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
                "IAM\r\nRNI,0,");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.5",  count, fail, "IAM\r\nRNI,0,1",                 -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.6",  count, fail, "IAM\r\nRNI,0,1,",                -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.7",  count, fail, "IAM\r\nRNI,0,1,0",               -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

 // doAssertRNI("3.8",  count, fail, "IAM\r\nRNI,0,1,0,",              -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("3.9",  count, fail, "IAM\r\nRNI,0,1,0,1",              4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("3.10", count, fail, "IAM\r\nRNI,0,1,0,1\r",            4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1\r");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("3.11", count, fail, "IAM\r\nRNI,0,1,0,1\r\n",          4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("3.12", count, fail, "IAM\r\nRNI,0,1,0,1,5\r\n",        4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1,5\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);


  // RNI parameter non presence (string terminated by \r\n
  //doAssertRNI("4.1",  count, fail, "IAM\r\nRNI\r\n",                 -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.2",  count, fail, "IAM\r\nRNI,\r\n",                -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.3",  count, fail, "IAM\r\nRNI,0\r\n",               -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.4",  count, fail, "IAM\r\nRNI,0,\r\n",              -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.5",  count, fail, "IAM\r\nRNI,0,1\r\n",             -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.6",  count, fail, "IAM\r\nRNI,0,1,\r\n",            -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.7",  count, fail, "IAM\r\nRNI,0,1,0\r\n",           -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.8",  count, fail, "IAM\r\nRNI,0,1,0,\r\n",          -1, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);

  //doAssertRNI("4.9",  count, fail, "IAM\r\nRNI,0,1,0,1\r\n",          4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("4.10", count, fail, "IAM\r\nRNI,0,1,0,1\r\n",          4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

  //doAssertRNI("4.11", count, fail, "IAM\r\nRNI,0,1,0,1,5\r\n",        4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1,5\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);

     /*
     The following Test Cases from MVAS are not run since parsing for ri, orr and rc is not implemented,
     nor are the descriptive terms: ri, orr, rc and rr recognized in MAS, the rr value is the only value
     parsed (which excludes "rr=")
  // RNI parameter domain ok
  doAssertRNI("5.1",  count, fail, "IAM\r\nRNI,0,1,0,1\r\n",          4, 0,1,0,1);
  doAssertRNI("5.2",  count, fail, "IAM\r\nRNI,0,1,00,1\r\n",         4, 0,1,0,1);
  doAssertRNI("5.3",  count, fail, "IAM\r\nRNI,6,6,31,6\r\n",         4, 6,6,31,6);
  doAssertRNI("5.4",  count, fail, "IAM\r\nRNI,6,u,31,u\r\n",         4, 6,-1,31,-1);
  doAssertRNI("5.5",  count, fail, "IAM\r\nRNI,16,17,131,18\r\n",     4, 16,17,131,18);
  doAssertRNI("5.6",  count, fail, "IAM\r\nRNI,1,2,3,4\r\n",          4, 1,2,3,4);
  doAssertRNI("5.7",  count, fail, "IAM\r\nRNI,ri=1,2,3,4\r\n",       4, 1,2,3,4);
  doAssertRNI("5.8",  count, fail, "IAM\r\nRNI,1,orr=2,3,4\r\n",      4, 1,2,3,4);
  doAssertRNI("5.9",  count, fail, "IAM\r\nRNI,1,2,rc=3,4\r\n",       4, 1,2,3,4);
  doAssertRNI("5.10", count, fail, "IAM\r\nRNI,1,2,3,rr=4\r\n",       4, 1,2,3,4);
  doAssertRNI("5.11", count, fail, "IAM\r\nRNI,ri=1,orr=2,rc=3,rr=4\r\n",       4, 1,2,3,4);


  // RNI parameter domain not ok
  doAssertRNI("6.1",  count, fail, "IAM\r\nRNI,1,2,3,4\r\n",          4, 1,2,3,4);
  doAssertRNI("6.2",  count, fail, "IAM\r\nRNI,1,2,3,\r\n",          -1, 1,2,3,4);
  doAssertRNI("6.3",  count, fail, "IAM\r\nRNI,1,2,3,x\r\n",         -1, 1,2,3,4);
  doAssertRNI("6.4",  count, fail, "IAM\r\nRNI,1,2,3,-1\r\n",        -1, 1,2,3,4);
  doAssertRNI("6.5",  count, fail, "IAM\r\nRNI,1,2,,4\r\n",          -1, 1,2,3,4);
  doAssertRNI("6.6",  count, fail, "IAM\r\nRNI,1,2,x,4\r\n",         -1, 1,2,3,4);
  doAssertRNI("6.7",  count, fail, "IAM\r\nRNI,1,2,u,4\r\n",         -1, 1,2,3,4);
  doAssertRNI("6.8",  count, fail, "IAM\r\nRNI,1,2,-1,4\r\n",        -1, 1,2,3,4);
  doAssertRNI("6.9",  count, fail, "IAM\r\nRNI,1,,3,4\r\n",          -1, 1,2,3,4);
  doAssertRNI("6.10", count, fail, "IAM\r\nRNI,1,x,3,4\r\n",         -1, 1,2,3,4);
  doAssertRNI("6.11", count, fail, "IAM\r\nRNI,1,-1,3,4\r\n",        -1, 1,2,3,4);
  doAssertRNI("6.12", count, fail, "IAM\r\nRNI,,2,3,4\r\n",          -1, 1,2,3,4);
  doAssertRNI("6.13", count, fail, "IAM\r\nRNI,x,2,3,4\r\n",         -1, 1,2,3,4);
  doAssertRNI("6.14", count, fail, "IAM\r\nRNI,u,2,3,4\r\n",         -1, 1,2,3,4);
  doAssertRNI("6.15", count, fail, "IAM\r\nRNI,-1,2,3,4\r\n",        -1, 1,2,3,4);
         */
        //However some of the test cases are choosen to run anyway to test robustness (the results might differ
        //from MVAS parsing, but this is not expected to cause problem with MTG-C (TODO: not sure about other gateways)
        // RNI parameter domain ok
        //doAssertRNI("5.1",  count, fail, "IAM\r\nRNI,0,1,0,1\r\n",          4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,0,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);
        //doAssertRNI("5.2",  count, fail, "IAM\r\nRNI,0,1,00,1\r\n",         4, 0,1,0,1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,0,1,00,1\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.USER_BUSY);
        //doAssertRNI("5.3",  count, fail, "IAM\r\nRNI,6,6,31,6\r\n",         4, 6,6,31,6);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,6,6,31,6\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE);
        //doAssertRNI("5.4",  count, fail, "IAM\r\nRNI,6,u,31,u\r\n",         4, 6,-1,31,-1);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,6,u,31,u\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);
        //doAssertRNI("5.5",  count, fail, "IAM\r\nRNI,16,17,131,18\r\n",     4, 16,17,131,18);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,16,17,131,18\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.UNKNOWN);
        //doAssertRNI("5.6",  count, fail, "IAM\r\nRNI,1,2,3,4\r\n",          4, 1,2,3,4);
        //doAssertRNI("5.7",  count, fail, "IAM\r\nRNI,ri=1,2,3,4\r\n",       4, 1,2,3,4);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,ri=1,2,3,4\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING);
        //doAssertRNI("5.8",  count, fail, "IAM\r\nRNI,1,orr=2,3,4\r\n",      4, 1,2,3,4);
        //doAssertRNI("5.9",  count, fail, "IAM\r\nRNI,1,2,rc=3,4\r\n",       4, 1,2,3,4);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,1,2,rc=3,5\r\n");
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);

        //doAssertRNI("5.10", count, fail, "IAM\r\nRNI,1,2,3,rr=4\r\n",       4, 1,2,3,4);
        //doAssertRNI("5.11", count, fail, "IAM\r\nRNI,ri=1,orr=2,rc=3,rr=4\r\n",       4, 1,2,3,4);
        gtdDescription = GtdFactory.parseGtd(
              "IAM\r\nRNI,ri=1,orr=2,rc=3,rr=4\r\n");
        //TODO: MAS does not recognize "rr" the result i "UNKNOWN", but this should be OK since Gateways should not send
        //the descriptive part only the value. But inorder to prevent problems maybe rr=u,-1,0-6 should be recognized.
        //rr=4 has been implemented, other still missing (TODO)
        assertTrue(gtdDescription.getRedirectingPartyRedirectingReason() == RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING);

    }
}
