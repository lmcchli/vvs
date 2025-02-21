/*
 * XMP_PAGClient.java
 *
 * Created on October 28, 2003, 8:03 AM
 */

package xmpserver.responsecodes;

import java.util.Random;
import java.util.logging.*;

public class XMPResponseCodes{

    Random generator = null;
    private Logger log = null;

    protected static String[] PAGCodes;

    static {
        PAGCodes =  new String[26];
        /*General codes*/
        PAGCodes[0] = "200";
        PAGCodes[1] = "Service successfully completed ";
        PAGCodes[2] = "408";
        PAGCodes[3] = "Request time-out";
        PAGCodes[4] = "421";
        PAGCodes[5] = "Service not available";
        PAGCodes[6] = "500";
        PAGCodes[7] = "Syntax error, request unrecognised";
        PAGCodes[8] = "501";
        PAGCodes[9] = "Syntax error in parameters or arguments";
        PAGCodes[10] = "502";
        PAGCodes[11] = "Resource limit exceeded";

        /*Paging notification codes*/
        PAGCodes[12] = "401";
        PAGCodes[13] = "Paging system number number blocked.";
        PAGCodes[14] = "402";
        PAGCodes[15] = "Paging system is busy in a call.";
        PAGCodes[16] = "404";
        PAGCodes[17] = "Paging system does not answer";
        PAGCodes[18] = "503";
        PAGCodes[19] = "Unknown paging network error";
        PAGCodes[20] = "511";
        PAGCodes[21] = "mailbox-id does not exist.";
        PAGCodes[22] = "512";
        PAGCodes[23] = "Non-valid paging system number.";
        PAGCodes[24] = "513";
        PAGCodes[25] = "Call attempt to paging system failed.";
    }

    protected static String[] ODLCodes;

    static {
        ODLCodes =  new String[30];
        /*General codes*/
        ODLCodes[0] = "200";
        ODLCodes[1] = "Service successfully completed ";
        ODLCodes[2] = "408";
        ODLCodes[3] = "Request time-out";
        ODLCodes[4] = "421";
        ODLCodes[5] = "Service not available";
        ODLCodes[6] = "500";
        ODLCodes[7] = "Syntax error, request unrecognised";
        ODLCodes[8] = "501";
        ODLCodes[9] = "Syntax error in parameters or arguments";
        ODLCodes[10] = "502";
        ODLCodes[11] = "Resource limit exceeded";

        /*Outdail notification codes*/
        ODLCodes[12] = "401";
        ODLCodes[13] = "Phone number blocked.";
        ODLCodes[14] = "402";
        ODLCodes[15] = "Phone number is busy in a call.";
        ODLCodes[16] = "403";
        ODLCodes[17] = "All lines busy";
        ODLCodes[18] = "404";
        ODLCodes[19] = "End user does not answer";
        ODLCodes[20] = "503";
        ODLCodes[21] = "General error, bad command.";
        ODLCodes[22] = "511";
        ODLCodes[23] = "Phone number does not exist.";
        ODLCodes[24] = "512";
        ODLCodes[25] = "Non-valid phone number.";
        ODLCodes[26] = "513";
        ODLCodes[27] = "Call attempt failed.";
        ODLCodes[28] = "514";
        ODLCodes[29] = "Mailbox does not exist.";
    }

 protected static String[] MWICodes;

    static {
        MWICodes =  new String[18];
        /*General codes*/
        MWICodes[0] = "200";
        MWICodes[1] = "Service successfully completed ";
        MWICodes[2] = "408";
        MWICodes[3] = "Request time-out";
        MWICodes[4] = "421";
        MWICodes[5] = "Service not available";
        MWICodes[6] = "500";
        MWICodes[7] = "Syntax error, request unrecognised";
        MWICodes[8] = "501";
        MWICodes[9] = "Syntax error in parameters or arguments";
        MWICodes[10] = "502";
        MWICodes[11] = "Resource limit exceeded";

        /*MWI notification codes*/
        MWICodes[12] = "512";
        MWICodes[13] = "Non-valid phone number.";
        MWICodes[14] = "511";
        MWICodes[15] = "Phone number does not exist.";
        MWICodes[16] = "404";
        MWICodes[17] = "End user does not answer";
    }

    public XMPResponseCodes(Logger l) {
        log = l;
        generator = new Random();
    }

    public String[] getODLCodes(){
        return ODLCodes;
        //        int i = generator.nextInt(30);
        //        return getODLResponseArray(getEvenNumber( i, 30 ));
    }

    public String[] getPAGCodes(){
        return PAGCodes;
        //int i = generator.nextInt(26);
        //return getPAGResponseArray(getEvenNumber( i, 26 ));
    }

    public String[] getMWICodes(){
        return MWICodes;
        //int i = generator.nextInt(18);
        //return getMWIResponseArray(getEvenNumber( i, 18 ));
    }

    private String[] getMWIResponseArray(int i){
        String[] re = new String[2];
        re[0] = MWICodes[i];
        re[1] = MWICodes[(i+1)];
        return re;
    }

    private String[] getODLResponseArray(int i){
        String[] re = new String[2];
        re[0] = ODLCodes[i];
        re[1] = ODLCodes[(i+1)];
        return re;
    }

    private String[] getPAGResponseArray(int i){
        String[] re = new String[2];
        re[0] = PAGCodes[i];
        re[1] = PAGCodes[(i+1)];
        return re;
    }

    private int getEvenNumber(int i, int max ){
        if( i%2 == 0 ) return i;
        int even_number = -1;
        int even_or_odd = 1;

        while(even_or_odd != 0){
            even_number = generator.nextInt(max);
            even_or_odd = even_number%2;
        }
        if(even_number%2 != 0) return 0;
        else return even_number;
    }
}
