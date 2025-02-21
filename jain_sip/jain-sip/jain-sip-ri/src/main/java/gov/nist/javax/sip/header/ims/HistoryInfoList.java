package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

/**
 * List of History-Info headers.
 * @author Mats Hägg
 */
public class HistoryInfoList extends SIPHeaderList  {


    public Object clone() {
        HistoryInfoList retval = new HistoryInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }
    /**
     * Constructor.
     */
    public HistoryInfoList() {
        super(HistoryInfo.class, HistoryInfoHeader.NAME);

    }




}
