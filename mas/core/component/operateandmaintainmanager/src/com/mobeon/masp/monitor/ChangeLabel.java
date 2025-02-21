package com.mobeon.masp.monitor;

import jcurses.widgets.Widget;
import jcurses.util.Rectangle;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

import jcurses.widgets.Label;
import jcurses.widgets.Window;
import jcurses.widgets.Widget;
import jcurses.util.Rectangle;
import jcurses.system.CharColor;
import jcurses.system.Toolkit;
import jcurses.event.ActionListener;
import jcurses.event.ActionEvent;

import java.util.StringTokenizer;

public class ChangeLabel extends Widget {

    private String _label = null;
    private String _empty_label = null;
    private int x;
    private int y;
//    private int oldx;
//    private int oldy;
    private Rectangle size;
//    private Rectangle oldSize;


    private static CharColor __labelDefaultColors = new CharColor(CharColor.WHITE, CharColor.BLACK);

    public CharColor getDefaultColors() {
        return __labelDefaultColors;
    }

    public void setText(String label) {
        _label = label;
//        oldx = x;
//        oldy = y;
//        oldSize = size;
//        size = getPreferredSize();
    }

    /**
    *  The constructor
    *
    * @param label label's text
    * param colors label's colors
    */
    public ChangeLabel(String label, CharColor colors, int x, int y, Rectangle size) {
        if (label!=null) {
            _label = label;
        } else {
            _label = "";
        }
        setColors(colors);

        this.x = x;
        this.y = y;
        this.size = size;
//        this.oldx = x;
//        this.oldy = y;
//        this.oldSize = size;
    }


    /**
    *  The constructor
    *
    * @param label label's text
    */
    public ChangeLabel(String label, int x, int y, Rectangle size) {
        this(label, null, x, y, size);
    }

    protected int getX() {
        return x;
    }

    protected int getY() {
        return y;
    }

    protected Rectangle getSize() {
        return size;
    }

 //   protected Rectangle getOldSize() {
 //       return oldSize;
 //   }

    protected Rectangle getPreferredSize() {
        if (_label.indexOf("\n") == -1) {
            return new Rectangle(_label.length(),1);
        } else {
            StringTokenizer tokenizer = new StringTokenizer(_label,"\n");
            int width=0;
            int height=0;
            while (tokenizer.hasMoreElements()) {
                String token = tokenizer.nextToken();
                height++;
                if (token.length()>width) {
                    width = token.length();
                }
            }
            height = (height == 0)?1:height;
            return new Rectangle(width, height);
        }
    }


    protected void doPaint() {

 //       Rectangle oldrect = (Rectangle)getOldSize().clone();
 //       oldrect.setLocation(getAbsoluteX(), getAbsoluteY());
 //       Toolkit.printString("",oldrect, getColors());

        Rectangle rect = (Rectangle)getSize().clone();
        rect.setLocation(getAbsoluteX(), getAbsoluteY());
        Toolkit.printString(_label,rect, getColors());
    }


    protected void doRepaint() {
        doPaint();
    }

    public void repaint(){
        super.repaint();
    }


/*    public myLabel(String s) {
        super(s);
        Rectangle rect = new Rectangle(s.length() ,1);
        //super.setSize(super.getPreferredSize() );
        super.setSize(rect);
        //super.setSize(super.getPreferredSize(););

    }


    public void setSize(Rectangle rect){
        super.setSize(rect);

    }

    public void setWindow(Window window){
        super.setWindow(window);

    }


    public void repaint(){
        super.repaint();
    }

    public void setX(int x){
            super.setX(x);
    }

    public void setY(int y){
            super.setY(y);
    }

    public void doRepaint(){
        super.doRepaint();
    }

    public void doPaint(){
        super.doPaint();
    }
  */

}
