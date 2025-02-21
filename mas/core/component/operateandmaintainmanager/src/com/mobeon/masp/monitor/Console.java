package com.mobeon.masp.monitor;

import jcurses.system.*;
import jcurses.widgets.*;
import jcurses.util.*;
import jcurses.event.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

/*
* Copyright (c) $today.year Mobeon AB. All Rights Reserved.
*/

//public class Console extends jcurses.widgets.Window implements  jcurses.event.ItemListener, jcurses.event.ActionListener, jcurses.event.ValueChangedListener, WindowListener, jcurses.widgets.WidgetsConstants {
public class Console extends jcurses.widgets.Window implements  WindowListener, jcurses.widgets.WidgetsConstants {

    ILogger log;

    // Widgets
    private ChangeLabel keyDesc = null;
    private ChangeLabel currPageDesc = null;
    private ChangeLabel statusBar = null;
    private ChangeLabel statusBar2 = null;
    private ChangeLabel columnDescription = null;
    private BorderPanel bp;
    private Boolean pageChange = true;

    // row and number widgets
    private  Hashtable<Integer, ChangeLabel> labelWidgets;          // all row widgets
    private  Hashtable<Integer, ChangeLabel> pageRowWidgets;        // Row widgets displayed
    private  Hashtable<Integer, ChangeLabel> pageNumberWidgets;     // Number widgets displayed

    // Default manager
    private DefaultLayoutManager manager;

    // Variables
    private Integer height;                 // The heigth of this window
    private Integer width;                  // The width of this window
    private Integer rowWith = 1;
    private PageLayout currentPageLayout;   // Current pageLayout object to talk to.
    private Integer top=5;                  // Where to start frame from top.
    private Integer left=4;                 // Where to start frame from left.
    private Integer noOfChannels;           // No of channels.
    private Integer noOfChannelsPage;       // No of channels to fit on a page.
    private Integer currentPage = 1;        // Current page 1-x
    private Integer currentRow = 1;         // Current row 1-x
    private boolean exit = false;           // If monitor should be terminated or not.
    private boolean reconnect = false;      // Shuld retry connection
    private Integer connectionStatus;      // Status of conn
    private String connectionMsg;          // Conn message


    // boolena variables
    private boolean isBottom;
    private boolean isTop;

    // Stores row data
    private Hashtable<Integer, String> rowData;

    // Layouts
    private  Vector<PageLayout> pageLayout;



    // Constructor
    public Console(int width, int height,String text) {

        super(width,height, true, text);

        log = ILoggerFactory.getILogger(Console.class);

        this.height = height;
        this.width = width;
        this.addListener((WindowListener)this);

        //_l1 = new jcurses.widgets.Label("Pos  ID  Service SessInit ConType ConState Direction OAct IAct   ANI     DNIS     RDNIS    FarECP");
        //statusBar = new com.mobeon.masp.monitor.ChangeLabel("", 3,50 , new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        keyDesc = new ChangeLabel("", 3,50 , new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        currPageDesc = new ChangeLabel("", 3,50 , new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        statusBar = new ChangeLabel("", 3,50 , new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        statusBar2 = new ChangeLabel("", 3,50 , new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        bp = new BorderPanel(this.width -7,130 );
        columnDescription = new ChangeLabel("",1,this.top-2 , new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));

        manager = new DefaultLayoutManager();
        jcurses.widgets.Panel panel = getRootPanel();
        panel.setLayoutManager(manager);

        manager.addWidget(keyDesc,3,this.height-3,50,1,ALIGNMENT_LEFT, ALIGNMENT_TOP);
        manager.addWidget(currPageDesc,3,1,50,1,ALIGNMENT_LEFT, ALIGNMENT_TOP);
        manager.addWidget(statusBar,3,this.height-5,50,1,ALIGNMENT_LEFT, ALIGNMENT_TOP);
        manager.addWidget(statusBar2,3,this.height-4,50,1,ALIGNMENT_LEFT, ALIGNMENT_TOP);

        // create new table containing all layouts.
        pageLayout = new Vector<PageLayout>();

        labelWidgets = new Hashtable<Integer, ChangeLabel>();          // all row widgets
        pageRowWidgets = new Hashtable<Integer, ChangeLabel>();        // Row widgets displayed
        pageNumberWidgets = new Hashtable<Integer, ChangeLabel>();     // Number widgets displayed

        keyDesc.setText("Text here");

        log.debug("Creating Console");


    }

    public PageLayout pageLayout(){
        return currentPageLayout;
    }

    public void setNoOfChannels(Integer channels){
        this.noOfChannels = channels;
    }

    public void setNoOfChannelsPage(Integer channels){
        this.noOfChannelsPage = channels;
    }

    public void init() {
        //manager.addWidget(bp,left-1,top-1,this.width,this.height-top-3 ,ALIGNMENT_LEFT, ALIGNMENT_TOP);
        manager.addWidget(bp,left-1,top-1,this.width,this.height-top-4 ,ALIGNMENT_LEFT, ALIGNMENT_TOP);
        manager.addWidget(columnDescription,left,top-2,this.width,this.height-top-3 ,ALIGNMENT_LEFT, ALIGNMENT_TOP);

        // Creates all widgets to be filled with data.
        for (int i = 1; i < (noOfChannels+1); i++) {
            addRow(i);   // add widgets
        }

        // creates all number widgets
        // Used to be pageSize
        for (int i = 1; i < (noOfChannelsPage+1); i++) {
             addNumber(i);
        }



        update();
        //columnDescription.setText(currentPageLayout.getColumnDescription(););
        //setCurrentColumnLayout(layout);  // set correct headerdescription text
        showPage();   // layout page
        //show();     // show window
    }

    public void setRow(Integer pos, String text){
        // set data to lables.
        if(labelWidgets.containsKey(pos)){
            Integer pageTop = noOfChannelsPage*currentPage+currentRow - noOfChannelsPage;
            Integer pageBottom = noOfChannelsPage*currentPage+currentRow;
            ChangeLabel label = labelWidgets.get(pos);
            label.setText(text);
            if ( pos >= pageTop && pos <= pageBottom )
            {
                label.repaint();
            }
        }
    }

    public void update(){
        // Get data from current layout object and
        // update page with data
        //Integer pos;
        rowData = currentPageLayout.getData(pageChange);                    // get data and update rows
        pageChange = false;

        // lopp thrue result ad set rows.
        for (Iterator<Map.Entry<Integer, String>> iterator = rowData.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry row =  iterator.next();
            setRow((Integer)row.getKey(),(String)row.getValue());
        }
    }

    public void addLayoutPage (PageLayout pl){
        currentPageLayout = pl;
        pageLayout.add(pl);
    }

    public void addRow(Integer pos) {
        // Creates widgets to be filled with data
        Integer pageTop = (noOfChannelsPage*currentPage - noOfChannelsPage)-1;
        Integer pageBottom = noOfChannelsPage*currentPage;
        ChangeLabel label = new ChangeLabel("", left, pos+(top-1), new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        labelWidgets.put(pos,label);
    }

    public void addNumber(Integer pos) {
        // Creates widgets to be filled with data
        ChangeLabel label = new ChangeLabel("", 1, (pos)+(top-1), new Rectangle(Console.this.getRectangle().getWidth()-left-3,1));
        pageNumberWidgets.put(pos,label);
        manager.addWidget(label,1,pos+(top-1),Console.this.getRectangle().getWidth()-left-3,rowWith,ALIGNMENT_LEFT, ALIGNMENT_TOP);
    }


    public void windowChanged(WindowEvent windowEvent) {
        if (windowEvent.getType() == WindowEvent.CLOSING) {
            exit=true;
            windowEvent.getSourceWindow().close();
        }
    }

    private void setCurrentPageLayout(Integer layout) {
      // switch layout.
      Integer noOfLayouts = pageLayout.size();
      if ((layout+1) <= noOfLayouts ) {
        currentPageLayout = pageLayout.get(layout);
        showPage();
      }
    }


    protected void doPageUp(){
          if (!isTop){
              if(currentPage != 1)
              {
                currentPage--;
              }
              else
              {
                  currentRow=0;
              }
          }
          else
          {
              //currentRow=0;
          }
    }

    protected void doPageDown(){
        if(!isBottom){
            currentPage++;
        }
    }

    protected void doRowUp(){
        if(!isTop){
            currentRow--;
            if(currentRow < 0)
            {
                currentRow=noOfChannelsPage-1;
                doPageUp();
            }
        }
        else
        {
            currentRow=0;
        }
    }

    protected void doRowDown(){
        Integer pageTop = (noOfChannelsPage*currentPage)+currentRow - noOfChannelsPage;

        if(pageTop + noOfChannelsPage > noOfChannels-1) {
        }
        else
        {
            currentRow++;
            if (currentRow > noOfChannelsPage-1)
            {
                currentPage++;
                currentRow=0;
            }
        }
    }

    protected void handleInput(InputChar ch) {
        // chatch all keypress. To capture page up and page down
        // all others is sent to the super class to be processed
        // in its own widget as events.

        // These keys is passed thru to be handeld by Console or Layout.
        // if not in filter the key is passed to the window(Widgets).
        boolean filter = (ch.getCode() == 338) ||  // page up
                         (ch.getCode() == 339) ||  // page down
                         (ch.getCode() == 258) ||  // down arrow
                         (ch.getCode() == 259) ||  // up arrow
                         (ch.getCode() == 265) ||  // F1  page 1
                         (ch.getCode() == 266) ||  // F2  page 2
                         (ch.getCode() == 267) ||  // F3  page 3
                         (ch.getCode() == 268) ||  // F4  page 4
                         (ch.getCode() == 269) ||  // F5  First layout
                         (ch.getCode() == 270) ||  // F6  Second layout
                         (ch.getCode() == 273);    // F9  Reconnect
        if (!filter) {
            super.handleInput(ch);
        } else {
            pageChange=true;
            if (ch.getCode() == 269 ) {     // F5
                setCurrentPageLayout(0);
                showPage();
            }else
            if (ch.getCode() == 270 ) {     // F6
                setCurrentPageLayout(1);
                showPage();
            } else
            if (ch.getCode() == 338 ) {     // PG DOWN
                doPageDown();
                showPage();
            } else
            if (ch.getCode() == 339 ) {     // PG UP
                doPageUp();
                showPage();
            } else
            if (ch.getCode() == 259 ) {     // ARROW UP
                doRowUp();
                showPage();
            } else
            if (ch.getCode() == 258 ) {     // ARROW DOWN
                doRowDown();
                showPage();
            } else
            if (ch.getCode() == 273 ) {     // F9 Reconnect
                reconnect = true;
            } else
            {
                // send keypress to layout
                currentPageLayout.keypress(ch.getCode());
                showPage();
            }
        }
    }

    protected Integer getPageTop(){
        isBottom = false;
        isTop = false;

        Integer pageTop = ((noOfChannelsPage*currentPage)+currentRow - noOfChannelsPage)-1;

        // Calculate if is on top or bottom..
        if(pageTop >= noOfChannels - noOfChannelsPage) {
            pageTop = noOfChannels - noOfChannelsPage;
            isBottom = true;
        }

        if (pageTop < 0){
            pageTop=0;
            isTop = true;
        }

        return pageTop;
    }


    protected void showPage(){
        Integer size;
        // prints the current page widgets on window
        Integer pageTop = getPageTop();

        // Remove widgets from layout
        for (ChangeLabel wid : pageRowWidgets.values()) {
            wid.setText("");
            wid.repaint();
        }
        show();

        for (ChangeLabel wid : pageRowWidgets.values()) {
            manager.removeWidget(wid);
        }

        // Change numbers
        for (int i = 1;i<(noOfChannelsPage+1);i++)
        {
            Integer number = pageTop +i;
            ChangeLabel label = pageNumberWidgets.get(i);
            label.setText(number.toString() );
        }

        // Layout new widgets to be displayed
        for (int i = 1;i < (noOfChannelsPage+1);i++)
        {
            ChangeLabel label = labelWidgets.get(i+pageTop);
            pageRowWidgets.put(i,label);
            manager.addWidget(label,left,(i+top)-1,Console.this.getRectangle().getWidth()-left-3,rowWith,ALIGNMENT_LEFT, ALIGNMENT_TOP);
        }

        // get columndescription from current layout.
        columnDescription.setText(currentPageLayout.getColumnDescription());

        // get key description from current layout.
        keyDesc.setText("ESC=Exit, F9=Reconnect, F5=Connection , F6=Statistic ,"+currentPageLayout.getKeyDescription());

        // get page description from current layout.
        currPageDesc.setText(currentPageLayout.getPageDescription());

        // Show the new layout
        show();

        currPageDesc.repaint();
        keyDesc.repaint();
        columnDescription.repaint();

    }

    public boolean exit(){
        return exit;
    }

    public boolean reconnect() {
        Boolean tmpReconnect;
        tmpReconnect = reconnect;
        reconnect = false;

        return tmpReconnect;
    }

    public void connectionStatus(Integer status, String message){
        connectionStatus = status;
        connectionMsg = message;
        statusBar.setText("Connection status:"+message);
    }
}
