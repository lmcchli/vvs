package com.mobeon.masp.chargingaccountmanager;

import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.serializer.DateSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.xml.sax.SAXException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Own implementation of TypeFactory used to set anoteher date format than the usual one.
 * <p/>
 * Ex. on date in the xmlrpc spec: 20080130T16:36:30
 *
 * Ex. on date from this class: 20080130T16:36:30-0100
 *
 * @author emahagl
 */
public class DateTypeFactory extends TypeFactoryImpl {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZ");

    public DateTypeFactory(XmlRpcController pController) {
        super(pController);
    }

    private DateFormat newFormat() {
        return simpleDateFormat;
    }

    public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
        if (pObject instanceof Date) {
            return new DateSerializer(newFormat());
        } else {
            return super.getSerializer(pConfig, pObject);
        }
    }
}