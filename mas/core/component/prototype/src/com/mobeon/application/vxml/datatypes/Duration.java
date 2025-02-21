package com.mobeon.application.vxml.datatypes;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:51:57 PM
 */
public class Duration
{
    public static final Mesurement SECONDS = new Mesurement("s");
    public static final Mesurement MILLISECONDS = new Mesurement("ms");

    private int length;
    private Mesurement mesurement;

    public Duration(int length, Mesurement mesurement)
    {
        this.length = length;
        this.mesurement = mesurement;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public Mesurement getMesurement()
    {
        return mesurement;
    }

    public void setMesurement(Mesurement mesurement)
    {
        this.mesurement = mesurement;
    }


    public static final class Mesurement
    {
        private String mesurement;

        private Mesurement(String mesurement)
        {
            this.mesurement = mesurement;
        }

    }
}
