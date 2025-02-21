package com.mobeon.smsc.management.http;

public interface HtmlConstants {
    public static final String HTML_STYLE =
        "<style type=\"text/css\">\n"
        //        + "BODY {font-family:verdana, arial,helvetica; background-color:#80A0AF; font-size:10;}\n"
        + "BODY {font-family:verdana, arial,helvetica; background-color:#003258; font-size:10;}\n"
        + "TH {font-family:verdana, arial,helvetica; background-color:#C1D1E0; font-size:12; text-align:center; vertical-align:top}\n"
        //        + "TD  {font-family:verdana, arial,helvetica; background-color:#C0C0C0; font-size:10; padding: 1px 5px 1px 5px; vertical-align:top}\n"
        + "TD  {font-family:verdana, arial,helvetica; background-color:#EAEFF5; font-size:10; padding: 1px 5px 1px 5px; vertical-align:top}\n"
        + "H1  {color:#EAEFF5}\n"
        + "</style>\n";        

    public static final String HTML_HEADER =
        "<html>\n"
        + "<script> function MM_openBrWindow(theURL,winName,features) { window.open(theURL,winName,features);  } "
        + "</script>"
        + "<head>\n"
        + HTML_STYLE
        + "<title>SMSC __ESME__</title>\n"
        + "</head>\n"
        + "<body><H1>SMSC (Mobeon simulator VERSION)</H1>";

    public static final String HTML_FOOTER =
        "</body>\n"
        + "</html>\n";
}    
