package com.mobeon.masp.chargingaccountmanager;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.logging.ILoggerFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Date: 2007-nov-29
 *
 * @author emahagl
 */
public class ChargingAccountManagerTest extends MockObjectTestCase implements Thread.UncaughtExceptionHandler {
    private static String VVA_DATEFORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static String UCIP_DATEFORMAT_STRING = "yyyyMMdd'T'HH:mm:ssZ";
    private static final String cfgFile = "../chargingaccountmanager/test/com/mobeon/masp/chargingaccountmanager/chargingaccountmanager.xml";
    private static final String LOG4J_CONFIGURATION = "../chargingaccountmanager/log4jconf.xml";

    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    protected Mock jmockAir;
    protected Mock jmockAir2;
    protected IConfiguration configuration;

    protected Map<Thread, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<Thread, Throwable>());

    private int clientId;

    public ChargingAccountManagerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        configuration = getConfiguration();
        jmockAir = mock(IAir.class);
        jmockAir2 = mock(IAir.class);
    }

    /**
     * @throws Exception if test case fails.
     */
    public void testChargingAccountManager() throws Exception {
        ChargingAccountManager cam = createMockedChargingAccountManager();

        // Test valid scenario
        HashMap<String, Object> responseTable = new HashMap<String, Object>();
        responseTable.put("responseCode", 0);
        jmockAir.expects(once()).method("execute").with(
                eq("UpdateBalanceAndDate"), isA(List.class)).will(
                returnValue(responseTable));
        jmockAir.expects(once()).method("isAvailable").will(returnValue(true));

        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("UpdateBalanceAndDate");
        request.addParameter("subscriberNumber", "161074");
        IChargingAccountResponse toTest = cam.sendRequest(request, 0);
        assertEquals("0", toTest.getParameter("responseCode"));

        // Test invalid clientIds
        try {
            toTest = cam.sendRequest(request, 2);
            fail("Expected ChargingAccountException");
        } catch (ChargingAccountException e) {
        }
        try {
            toTest = cam.sendRequest(request, -1);
            fail("Expected ChargingAccountException");
        } catch (ChargingAccountException e) {
        }
    }

    private ChargingAccountManager createMockedChargingAccountManager() {
        ChargingAccountManager cam = new ChargingAccountManager();
        cam.setConfiguration(configuration);
        cam.init();

        ArrayList<IAir> airClientList = new ArrayList<IAir>();
        airClientList.add((IAir) jmockAir.proxy());
        airClientList.add((IAir) jmockAir2.proxy());
        cam.setAirClientList(airClientList);
        return cam;
    }

    /**
     * Needs an AIR simulator for this test.
     *
     * @throws Exception if test case fails.
     */
    public void XtestMChargingAccountManager() throws Exception {
        ChargingAccountManager cam = createChargingAccountManager();
        clientId = cam.getNextClientId();

        /*try {
            updateBalance(cam);
        } catch (ChargingAccountException e) {
        }*/

        //clientId = cam.getNextClientId();

        /*try {
            getBalance(cam);
        } catch (ChargingAccountException e) {
        }

        clientId = cam.getNextClientId();*/

        /*try {
            getAccountDetails(cam);
        } catch (ChargingAccountException e) {
        } */

        updateSubscriberSegmentation(cam);
        //updateFafList(cam);
        /*
        clientId = cam.getNextClientId();
        updateAccountDetails(cam);*/
    }

    private void updateBalance(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("UpdateBalanceAndDate");
        request.addParameter("subscriberNumber", "1069");
        request.addParameter("serviceFeeExpiryDate", getCurrentTime());

        request.addParameter("adjustmentAmountRelative", "100");
        request.addParameter("transactionCurrency", "SEK");

        request.addParameter("promotionNotificationFlag", "true");
        // request.addParameter("firstIVRCallSetFlag", "true");
        // request.addParameter("accountActivationFlag", "true");

        // request.addParameter("dedicatedAccountID", "123456");
        // request.addParameter("adjustmentAmountRelative", "666");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        System.out.println("");
    }

    private void getBalance(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("GetBalanceAndDate");
        request.addParameter("subscriberNumber", "1069");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        String param = response.getParameter("accountValue1");
        System.out.println("accountValue1=" + param);
        param = response.getParameter("currency1");
        System.out.println("currency1=" + param);
        param = response.getParameter("creditClearanceDate");
        System.out.println("creditClearanceDate=" + param);

        // This parameter is located in the chargingResultInformation struct.
        param = response.getParameter("cost1");
        System.out.println("cost1=" + param);

        // param = response.getParameter("chargingResultInformation");
        // System.out.println("chargingResultInformation=" + param);
    }

    private void getAccountDetails(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("GetAccountDetails");
        // Parameternames are case-sensitive.
        request.addParameter("subscriberNumber", "1069");
        request.addParameter("subscriberNumberNAI", "1");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        String param = response.getParameter("serviceClassCurrent");
        System.out.println("serviceClassCurrent=" + param);
        param = response.getParameter("supervisionExpiryDate");
        System.out.println("supervisionExpiryDate=" + param);
        param = response.getParameter("serviceFeeExpiryDate");
        System.out.println("serviceFeeExpiryDate=" + param);
    }

    private void updateAccountDetails(ChargingAccountManager cam)
            throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("UpdateAccountDetails");
        request.addParameter("subscriberNumber", "161074");
        request.addParameter("pinCodeValidationFlag", "false");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        String param = response.getParameter("serviceClassCurrent");
        System.out.println("serviceClassCurrent" + param);
    }

    private void refill(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("Refill");
        request.addParameter("subscriberNumber", "161074");
        request.addParameter("transactionAmount", "5000");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);

        // refillAmount1 is located in the refillValueTotal struct which is
        // located in the refillInformation struct
        String param = response.getParameter("refillAmount1");
        System.out.println("refillAmount1=" + param);
        param = response.getParameter("activationStatusFlag");
        System.out.println("activationStatusFlag=" + param);
    }

    private void getAccumulators(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("GetAccumulators");
        request.addParameter("subscriberNumber", "161074");
        request.addParameter("chargingType", "1");
        request.addParameter("chargingIndicator", "0");
        request.addParameter("reservationCorrelationID", "56789");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        String param = response.getParameter("accumulatorInformation");
        System.out.println("accumulatorInformation=" + param);
    }

    private void updateSubscriberSegmentation(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("UpdateSubscriberSegmentation");
        request.addParameter("subscriberNumber", "1069");

        String[] params = new String[]{"serviceOfferingID", "serviceOfferingActiveFlag"};
        String[] values = new String[]{"1", "true"};
        request.addArrayParameter("serviceOfferings", params, values);

        params = new String[]{"serviceOfferingID", "serviceOfferingActiveFlag"};
        values = new String[]{"2", "false"};
        request.addArrayParameter("serviceOfferings", params, values);

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        String param = response.getParameter("responseCode");
        System.out.println("responseCode=" + param);
    }

    private void updateFafList(ChargingAccountManager cam) throws Exception {
        ChargingAccountRequest request = new ChargingAccountRequest();
        request.setName("UpdateFaFList");
        request.addParameter("subscriberNumber", "1069");
        request.addParameter("fafAction", "SET");

        request.addParameter("fafNumber", "1069");
        request.addParameter("fafIndicator", "1");
        request.addParameter("owner", "Subscriber");

        IChargingAccountResponse response = cam.sendRequest(request, clientId);
        String param = response.getParameter("responseCode");
        System.out.println("responseCode=" + param);
    }

    /**
     * Needs an AIR simulator for this test.
     *
     * @throws Exception if test case fails.
     */
    public void XtestMultithreadedRequests() throws Throwable {
        final ChargingAccountManager cam = createChargingAccountManager();
        clientId = cam.getNextClientId();

        int size = 5;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        getAccountDetails(cam);
                        threadSleep(10);
                    } catch (Exception e) {
                        fail("Exception in run " + e);
                    }
                }
            });
            threads[i].setUncaughtExceptionHandler(this);
            threads[i].start();
        }
        threadSleep(100);
        for (Thread thread : threads) {
            thread.join();
            if (exceptionMap.containsKey(thread)) {
                throw exceptionMap.get(thread);
            }
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        exceptionMap.put(t, e);
    }

    public void testDateUtil() throws Exception {
        // test current time which is UCIP formatted.
        Date toTest = DateUtil.getDateFromValue(getCurrentTime());
        assertEquals(Calendar.getInstance().getTime().toString(), toTest.toString());

        // test invalid date (not UCIP formatted)
        try {
            DateUtil.getDateFromValue("Fri Dec 07 07:39:19 CET 2007");
            fail("Expected ChargingAccountException");
        } catch (ChargingAccountException e) {
        }

        // test invalid date again
        try {
            DateUtil.getDateFromValue("");
            fail("Expected ChargingAccountException");
        } catch (ChargingAccountException e) {
        }

        // test current time
        String strToTest = DateUtil.getStringFromDate(new Date());
        assertEquals(strToTest, getCurrentTime());
    }

    private ChargingAccountManager createChargingAccountManager() {
        ChargingAccountManager chargingAccountManager = new ChargingAccountManager();
        chargingAccountManager.setConfiguration(configuration);
        chargingAccountManager.init();
        return chargingAccountManager;
    }

    private IConfiguration getConfiguration() throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFile);
        return configurationManager.getConfiguration();
    }

    private static String getCurrentTime() {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UCIP_DATEFORMAT_STRING);
        return simpleDateFormat.format(time);
    }

    public static void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Exception in sleep " + e);
        }
    }

}
