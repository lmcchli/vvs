/*
COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.

THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
THEREOF, MAY NOT BE PROVIDED OR OTHERWISE MADE AVAILABLE
TO ANY OTHER PERSON OR ENTITY.
TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
*/

package com.abcxyz.services.moip.common.cmnaccess;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.data.MessageInfo;

public class IDBase64Gen {

    private static final int MSID_LENGTH = 16;

    private static final char[] alphabet64 = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', '-', '_'
    };

    private static final char[] alphabet16 = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' };

    public static String encodeId(MSA msa, String id1, String id2, String serverId, char prefix) throws IDBase64FormatException {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        builder.append(encodeInBase64(msa, id1, id2));
        builder.append(serverId);
        return builder.toString();
    }

    public static String encodeMessageId(MSA omsa, String omsgId, String rmsgId, String serverId, char prefix) throws IDBase64FormatException {
        return encodeId(omsa, omsgId, rmsgId, serverId, prefix);
    }

    public static String encodeRecipientId(MSA rmsa, String rmsgId, String omsgId, String serverId, char prefix) throws IDBase64FormatException {
        return encodeId(rmsa, rmsgId, omsgId, serverId, prefix);
    }

    public static MessageInfo decodeMessageId(String id) throws IDBase64FormatException {
        return decodeMessageInfo(id, true);
    }

    public static MessageInfo decodeRecipientId(String id) throws IDBase64FormatException {
        return decodeMessageInfo(id, false);
    }

    private static MessageInfo decodeMessageInfo(String id, boolean isMessageId) throws IDBase64FormatException {
        if (id == null) {
            throw new IDBase64FormatException("ID is null.");
        }

        int idLength;
        int domainIndex = id.indexOf("@");
        if (domainIndex > 0) {
            //For MM4 case remove the @<domainName> from the id
            idLength = domainIndex;
        } else {
            idLength = id.length();
        }

        if (idLength < (MSID_LENGTH + 3) ) {
            throw new IDBase64FormatException("Id length is too short: " + id);
        }

        int charAsNum;
        int index = 0;
        int rem = -1;
        char[] idInBase64 = id.substring(1, idLength - 2).toCharArray();
        int lastIdIndex = idInBase64.length - 1;

        StringBuilder idInBase16 = new StringBuilder();

        while (index < lastIdIndex) {
            charAsNum = base64ToNumber(idInBase64[index]);
            if (rem != -1) {
                idInBase16.append(alphabet16[(rem << 2) | (charAsNum >> 4)]);
                idInBase16.append(alphabet16[charAsNum & 15]);
                rem =-1;
            } else {
                rem = charAsNum & 3; // take the last two bits of char2
                idInBase16.append(alphabet16[charAsNum >> 2]);
            }
            index ++;
        }

        charAsNum =  base64ToNumber(idInBase64[lastIdIndex]);

        if ( rem != -1 ) {
            idInBase16.append(alphabet16[(rem << 2) | (charAsNum >> 2)]);
        } else {
            idInBase16.append(alphabet16[charAsNum >> 2]);
        }
        // the last two bits of the last char contains info if the MSIDs are internals or externals
        return buildMessageInfoFromId(idInBase16.toString(), charAsNum & 3, isMessageId);
    }

    private static String encodeInBase64(MSA msa, String msaId, String id) throws IDBase64FormatException {
        StringBuilder encodedId = new StringBuilder();
        int index = 0;
        int rem = -1;
        StringBuilder idInBase16 = new StringBuilder();
        idInBase16.append(msa.getId());
        idInBase16.append(msaId);
        idInBase16.append(id);
        char[] idInBase16Array = idInBase16.toString().toCharArray();
        int idLength = idInBase16Array.length;

        while (index < idLength) {
            char base64Char;
            if (rem != -1) {
                base64Char = alphabet64[(rem << 4) | base16ToNumber(idInBase16Array[index])];
                index ++;
                rem = -1;
            } else {
                int char1AsNum = base16ToNumber(idInBase16Array[index]);
                index ++;
                if (index < idLength) {
                    int char2AsNum = base16ToNumber(idInBase16Array[index]);
                    index ++;
                    //                        alphabet64[move char1 to left by 2 bits and add first two bits of char2]
                    base64Char = alphabet64[(char1AsNum << 2 ) | (char2AsNum >> 2)];
                    rem = char2AsNum & 3; // take the last two bits of char2
                } else {
                    rem = char1AsNum;
                    break;
                }
            }
            encodedId.append(base64Char);
        }
        if (rem != -1) {
            int lastCharAsNum = rem << 2;
            if (msa.isInternal()) {
                lastCharAsNum = lastCharAsNum | 1;
            }
            encodedId.append(alphabet64[lastCharAsNum]);
        } else {
            throw new IDBase64FormatException("Wrong Id format: " + idInBase16);
        }
        return encodedId.toString();
    }

    /*
     * Creates MessageInfo object from a given messageId or recipientId.
     * The format for the id is:  MSID + MSGID1 + MSGID2
     *                                                 |                    |                 |
     *                                            16 bits         6 bits         6 bits
     *  Example: 749cc4e434adedf3af229fe21b61
     * @param id
     * @param msidCode
     * @param isMessageId
     * @return MessageInfo
     */
    private static MessageInfo  buildMessageInfoFromId(String id, int mcdCode, boolean isMessageId) throws IDBase64FormatException {
        int msgidLength = (id.length() - MSID_LENGTH) /2;
        int msgId1StartIndex = MSID_LENGTH + msgidLength;
        if (msgidLength == 0) {
            throw new IDBase64FormatException("Id length is too short: " + id);
        }
        try {
            String msgId1 = id.substring(MSID_LENGTH, MSID_LENGTH + msgidLength);
            String msgId2 = id.substring(msgId1StartIndex, msgId1StartIndex + msgidLength);
            if (isMessageId) {
                return new MessageInfo(
                        new MSA(id.substring(0,MSID_LENGTH), mcdCode == 1),
                        null,
                        msgId1,
                        msgId2);
            } else {
                return new MessageInfo(
                        null,
                        new MSA(id.substring(0,MSID_LENGTH), mcdCode == 1),
                        msgId2,
                        msgId1);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IDBase64FormatException("Wrong Id format: " + id, e);
        }
    }

    private static int base16ToNumber(char hexChar) throws IDBase64FormatException {
        if (hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar <= 'f') {
            return hexChar - 'a' + 10;
        } else {
            throw new IDBase64FormatException("Character [" + hexChar+ "] is not in Base16 alphabet");
        }
    }

    private static  int base64ToNumber(char base64Char) throws IDBase64FormatException {
        if (base64Char == alphabet64[62]) {
            return 62;
        } else if (base64Char == alphabet64[63]) {
            return 63;
        } else if (base64Char >= 'A' && base64Char <= 'Z') {
            return base64Char - 'A';
        } else if (base64Char >= 'a' && base64Char <= 'z') {
            return base64Char - 'a' + 26;
        } else if (base64Char >= '0' && base64Char <= '9' ) {
            return base64Char - '0' + 52;
        } else {
            throw new IDBase64FormatException("Character [" + base64Char + "] is not in Base64 alphabet");
        }
    }

    public static void main(String[] args) {
        try {

            String OMSGID = "1ea1f8";
            String RMSGID = "582eb7";
            String RMSID = "6543f88cc9ce2c90";
            String OMSID = "450db63623d1ceeb";
            long encodeTime = 0;
            long decodeTime = 0;
            long count = 0;
            for (int i1 = 0; i1 < 16; i1++) {
                for (int i2 = 0; i2 < 16; i2++) {
                    for (int i3 = 0; i3 < 16; i3++) {
                        for (int i4 = 0; i4 < 16; i4++) {
                            for (int i5 = 0; i5 < 16; i5++) {
                                String tempOMSID = "43f1233623d" +
                                    alphabet16[i1] +
                                    alphabet16[i2] +
                                    alphabet16[i3] +
                                    alphabet16[i4] +
                                    alphabet16[i5];
                                MessageInfo origMsgInfo = new MessageInfo(new MSA(tempOMSID), new MSA(RMSID), OMSGID,  RMSGID);
                                long startTime = System.currentTimeMillis();
                                String messageId64 = IDBase64Gen.encodeMessageId(origMsgInfo.omsa, origMsgInfo.omsgid, origMsgInfo.rmsgid, "00", 'm');
                                long endTime = System.currentTimeMillis();
                                encodeTime = encodeTime + (endTime - startTime);
                                startTime = System.currentTimeMillis();
                                MessageInfo decodedMsgInfo = IDBase64Gen.decodeMessageId(messageId64);
                                endTime = System.currentTimeMillis();
                                decodeTime = decodeTime + (endTime - startTime);
                                count ++;
                            }
                        }
                    }
                }
            }
            System.out.println("TEST ID counts : " + count);
            System.out.println("TEST total time: " + (encodeTime + decodeTime));
            System.out.println("TEST total encode time: " + (encodeTime ));
            System.out.println("TEST total decode time: " + (decodeTime));

            MessageInfo origMsgInfo = new MessageInfo(new MSA(OMSID), null, OMSGID,  RMSGID);
            String messageId16 = origMsgInfo.omsa + origMsgInfo.omsgid + origMsgInfo.rmsgid;
            String messageId64 = IDBase64Gen.encodeMessageId(origMsgInfo.omsa, origMsgInfo.omsgid, origMsgInfo.rmsgid, "00", 'm');
            System.out.println("*****************************");
            MessageInfo decodedMsgInfo = IDBase64Gen.decodeMessageId(messageId64);
            if ((origMsgInfo.toString().equals(decodedMsgInfo.toString())) &&
                    (origMsgInfo.omsa.isInternal() == decodedMsgInfo.omsa.isInternal())) {
                System.out.println("MessageID OK");
            } else {
                System.out.println("MessageID FAILED");

            }
            System.out.println("*****************************");
            System.out.println(messageId16);
            System.out.println(messageId64);
            System.out.println(origMsgInfo);
            System.out.println(decodedMsgInfo);

            origMsgInfo = new MessageInfo(null, new MSA(RMSID), OMSGID,  RMSGID);
            String recipientId16 = origMsgInfo.rmsa + origMsgInfo.rmsgid + origMsgInfo.omsgid;
            String recipientId64 = IDBase64Gen.encodeRecipientId(origMsgInfo.rmsa, origMsgInfo.rmsgid, origMsgInfo.omsgid, "00", 'm');
            decodedMsgInfo = IDBase64Gen.decodeRecipientId(recipientId64);
            System.out.println("*****************************");
            if ((origMsgInfo.toString().equals(decodedMsgInfo.toString())) &&
                    (origMsgInfo.rmsa.isInternal() == decodedMsgInfo.rmsa.isInternal())) {
                System.out.println("Recipient OK");
            } else {
                System.out.println("Recipient FAILED");

            }
            System.out.println("*****************************");
            System.out.println(recipientId16);
            System.out.println(recipientId64);
            System.out.println(origMsgInfo);
            System.out.println(decodedMsgInfo);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}


