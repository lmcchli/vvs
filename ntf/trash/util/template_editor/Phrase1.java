import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.InsetsUIResource;
import java.util.*;
import java.util.Vector;
import java.io.*;
import java.lang.*;
import java.awt.geom.*;
import se.abcxyz.moip.ntf.mail.UserMailbox;
import se.abcxyz.moip.ntf.text.TextCreator;
import se.abcxyz.moip.ntf.userinfo.UserInfo;
import NotificationProcess.NotificationEmail;
import se.abcxyz.moip.ntf.userinfo.NotificationFilter;
import java.text.SimpleDateFormat;

//--------------------------------------------------------------------------
public class Phrase1 extends JFrame implements ActionListener, MouseListener
//--------------------------------------------------------------------------
{
    private String pName;
    private String pValue;
    private String newPName;
    private String newPValue;
    private String message = "";
    private String tagString = "";
    private String msg = "";
    private String msg1 = "";
    private String msg2 = "";
    private String tag = "";
    private String previewString1 = null;
    String warningString = "";
    private int subtemplateInt = 0;
    private Properties prop;
    private Properties mailProp;
    private boolean ok = false;
    private File file = new File("/home/loke/ejoemma/sommarjobb/java/empty.phr");
    
    //--------------
    public Phrase1()
    //--------------
    {
        prop = new Properties();
        mailProp = new Properties();
        menuListener = new MenuListener();
        countMessages = new CountListener();
        templateListener = new TemplateListener();
        tagSizeBtnListener1 = new TagSizeBtnListener1();
        tagSizeBtnListener2 = new TagSizeBtnListener2();
        tagSizeBtnListener3 = new TagSizeBtnListener3();
        mailListener = new MailListener();
        simpleFileFilter = new SimpleFileFilter();
        testUser = new TestUser();
        initComponents();
    } // Phrase1
    
    //---------------------------
    private void initComponents()
    //---------------------------
    {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        helpMenu = new JMenu("Help");
        saveMenuItem = new JMenuItem("Save");
        saveAsMenuItem = new JMenuItem("Save As");
        openMenuItem = new JMenuItem("Open");
        exitMenuItem = new JMenuItem("Exit");
        helpMenuItem = new JMenuItem("Help");
        
        fileChooser = new JFileChooser();
        helpOptionPane = new JOptionPane();
        textPane = new JTextPane();
        
        backgroundPanel = new JPanel();
        templatePanel = new JPanel();
        subtemplatePanel = new JPanel();
        mailPanel = new JPanel();
        inboxPanel = new JPanel();
        previewPanel = new JPanel();
        textPanel = new JPanel();
        
        deleteBtn = new JButton();
        previewBtn = new JButton();
        clearBtn = new JButton();
        mailStoreBtn = new JButton();
        mailDeleteBtn = new JButton();
        tagCountBtn = new JButton();
        tagSizeBtn1 = new JButton();
        tagSizeBtn2 = new JButton();
        tagSizeBtn3 = new JButton();
        
        mailLabel = new JLabel();
        phraseLabel = new JLabel();
        newEmailLabel = new JLabel();
        inboxLabel = new JLabel();
        fromLabel = new JLabel();
        subjectLabel = new JLabel();
        previewLabel = new JLabel();
        msgSizeLabel = new JLabel();
        textLabel = new JLabel();
        attachmentsLabel = new JLabel();
        subtemplateHeader = new JLabel();
        subtemplateLabel1 = new JLabel();
        subtemplateLabel2 = new JLabel();
        subtemplateLabel3 = new JLabel();
        newVoiceLabel = new JLabel();
        totNewMsgLabel = new JLabel();
        newFaxLabel = new JLabel();
        warningLabel1 = new JLabel();
        
        combo1 = new JComboBox();
        combo2 = new JComboBox();
        combo3 = new JComboBox();
        combo4 = new JComboBox();
        combo5 = new JComboBox();
        combo6 = new JComboBox();
        combo7 = new JComboBox();
        combo8 = new JComboBox();
        
        checkBox = new JCheckBox();
        
        textField1 = new JTextField();
        textField2 = new JTextField();
        textField3 = new JTextField();
        textField4 = new JTextField();
        textField5 = new JTextField();
        textField6 = new JTextField();
        textField8 = new JTextField();
        subtemplate1 = new JTextField();
        subtemplate2 = new JTextField();
        subtemplate3 = new JTextField();
        
        textArea1 = new JTextArea();
        textArea2 = new JTextArea();
        
        getContentPane().setLayout(new BorderLayout());
        
        setTitle("DEMO SMS Configurator");
        setBackground(new Color(200, 200, 200));
        setForeground(new Color(255, 255, 204));
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                System.exit(0);
            }
        });
        
        backgroundPanel.setLayout(null);
        backgroundPanel.setBackground(new Color(255, 255, 204));
        
        //----TEMPLATE----
        templatePanel.setLayout(null);
        templatePanel.setOpaque(false);
        templatePanel.setBorder(BorderFactory.createTitledBorder("TEMPLATE EDITOR"));
        
        deleteBtn.addActionListener(this);
        deleteBtn.setMargin(new Insets(2,2,2,2));
        deleteBtn.setText("DELETE");
        deleteBtn.setToolTipText("Delete selected template");
        templatePanel.add(deleteBtn);
        deleteBtn.setBounds(20, 30, 60, 25);
                
        // phrases
        combo7.addActionListener(templateListener);
        combo7.setEditable(true);
        combo7.setToolTipText("Choose template from the list or type a new one");
        getProps(file);
        templatePanel.add(combo7);
        combo7.setBounds(90, 30, 130, 20);
        
        clearBtn.addActionListener(this);
        clearBtn.setMargin(new Insets(2,2,2,2));
        clearBtn.setText("CLEAR");
        clearBtn.setToolTipText("Clear the phrase textfield");
        templatePanel.add(clearBtn);
        clearBtn.setBounds(20, 60, 60, 25);
        
        // phrase
        textField8.setEditable(true);
        textField8.addMouseListener(this);
        textField8.setText("");
        textField8.setToolTipText("Type or modify the templates phrase");
        templatePanel.add(textField8);
        textField8.setBounds(90, 60, 425, 20);
        
        // tags to choose between
        combo8.addActionListener(this);
        combo8.setToolTipText("Insert tag from the list into the phrase");
        combo8.addItem("--Insert tag--");
        combo8.addItem("NUM_NEW_TOT");
        combo8.addItem("NUM_NEW_FAX");
        combo8.addItem("NUM_NEW_EMAIL");
        combo8.addItem("NUM_NEW_VOICE");
        combo8.addItem("STATUS");
        combo8.addItem("DEPOSIT_TYPE");
        combo8.addItem("TIME");
        combo8.addItem("DATE");
        combo8.addItem("SUBJECT");
        combo8.addItem("FROM");
        combo8.addItem("MSG_SIZE");
        combo8.addItem("NUM_ATTACHMENTS");
        combo8.addItem("E-mail_TEXT");
        combo8.addItem("NUM_NEW_TOT_TEXT");
        combo8.addItem("NUM_NEW_FAX_TEXT");
        combo8.addItem("NUM_NEW_EMAIL_TEXT");
        combo8.addItem("NUM_NEW_VOICE_TEXT");
        combo8.addItem("QUOTA_TEXT");
        combo8.setSelectedIndex(0);
        templatePanel.add(combo8);
        combo8.setBounds(525, 60, 170, 20);
        
        //----TEXTPANEL----
        textPanel.setBackground(Color.white);
        textPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        textPanel.setOpaque(true);
        textPane = createTextPane("", "");
        textPanel.setBounds(90, 60, 425, 20);
        textPanel.add(textPane);
        //templatePanel.add(textPanel);
        
        // ----SUBTEMPLATE----
        subtemplatePanel.setLayout(null);
        subtemplatePanel.setOpaque(false);
        subtemplatePanel.setBorder(BorderFactory.createTitledBorder("SUBTEMPLATE: No tag with subtemplates is choosen."));
        
        subtemplatePanel.add(subtemplateLabel1);
        subtemplateLabel1.setBounds(20, 30, 190, 20);
        
        subtemplatePanel.add(subtemplate1);
        subtemplate1.setBounds(220, 30, 230, 20);
        
        tagCountBtn.addActionListener(this);
        tagCountBtn.setMargin(new Insets(2,2,2,2));
        tagCountBtn.setText("Insert COUNT");
        tagCountBtn.setToolTipText("Insert tag COUNT into the subtemplate");
        tagCountBtn.setVisible(false);
        subtemplatePanel.add(tagCountBtn);
        tagCountBtn.setBounds(460, 30, 100, 25);
        
        tagSizeBtn1.addActionListener(tagSizeBtnListener1);
        tagSizeBtn1.setMargin(new Insets(2,2,2,2));
        tagSizeBtn1.setText("Insert SIZE");
        tagSizeBtn1.setToolTipText("Insert tag SIZE into the subtemplate");
        tagSizeBtn1.setVisible(false);
        subtemplatePanel.add(tagSizeBtn1);
        tagSizeBtn1.setBounds(460, 30, 100, 25);
        
        subtemplatePanel.add(subtemplateLabel2);
        subtemplateLabel2.setBounds(20, 60, 190, 20);
        
        subtemplatePanel.add(subtemplate2);
        subtemplate2.setBounds(220, 60, 230, 20);
        
        tagSizeBtn2.addActionListener(tagSizeBtnListener2);
        tagSizeBtn2.setMargin(new Insets(2,2,2,2));
        tagSizeBtn2.setText("Insert SIZE");
        tagSizeBtn2.setToolTipText("Insert tag SIZE into the subtemplate");
        tagSizeBtn2.setVisible(false);
        subtemplatePanel.add(tagSizeBtn2);
        tagSizeBtn2.setBounds(460, 60, 100, 25);
        
        subtemplatePanel.add(subtemplateLabel3);
        subtemplateLabel3.setBounds(20, 90, 190, 20);
        
        subtemplatePanel.add(subtemplate3);
        subtemplate3.setBounds(220, 90, 230, 20);
        
        tagSizeBtn3.addActionListener(tagSizeBtnListener3);
        tagSizeBtn3.setMargin(new Insets(2,2,2,2));
        tagSizeBtn3.setText("Insert SIZE");
        tagSizeBtn3.setToolTipText("Insert tag SIZE into the subtemplate");
        tagSizeBtn3.setVisible(false);
        subtemplatePanel.add(tagSizeBtn3);
        tagSizeBtn3.setBounds(460, 90, 100, 25);
        
        templatePanel.add(subtemplatePanel);
        subtemplatePanel.setBounds(110, 100, 580, 130);
        
        backgroundPanel.add(templatePanel);
        templatePanel.setBounds(0, 0, 715, 250);
        
        //-----MAIL----
        mailPanel.setLayout(null);
        mailPanel.setOpaque(false);
        mailPanel.setBorder(BorderFactory.createTitledBorder("MAIL EXAMPLE"));
        
        combo1.addItem("--Status--");
        combo1.addItem("Normal");
        combo1.addItem("Urgent");
        combo1.setSelectedIndex(0);
        combo1.setToolTipText("Select status of the mail from the list");
        mailPanel.add(combo1);
        combo1.setBounds(20, 60, 85, 20);
        
        combo2.addItem("--Deposit type--");
        combo2.addItem("Voice mail");
        combo2.addItem("Fax");
        combo2.addItem("Email");
        combo2.setSelectedIndex(0);
        combo2.setToolTipText("Select deposit type of the mail from the list");
        mailPanel.add(combo2);
        combo2.setBounds(115, 60, 120, 20);
        
        combo3.addActionListener(mailListener);
        combo3.setEditable(true);
        getMailProperties();
        combo3.setToolTipText("Select stored mail from the list or type a new one");
        mailPanel.add(combo3);
        combo3.setBounds(20, 30, 115, 20);
        
        fromLabel.setText("From:");
        mailPanel.add(fromLabel);
        fromLabel.setBounds(20, 90, 80, 20);
        
        // From textField
        textField1.setEditable(true);
        mailPanel.add(textField1);
        textField1.setBounds(100, 90, 180, 20);
        
        subjectLabel.setText("Subject:");
        mailPanel.add(subjectLabel);
        subjectLabel.setBounds(20, 120, 80, 20);
        
        // Subject textField
        textField2.setEditable(true);
        mailPanel.add(textField2);
        textField2.setBounds(100, 120, 180, 20);
        
        msgSizeLabel.setText("Msg size:");
        mailPanel.add(msgSizeLabel);
        msgSizeLabel.setBounds(20, 150, 80, 20);
        
        // Message size textField
        textField5.setEditable(true);
        mailPanel.add(textField5);
        textField5.setBounds(100, 150, 180, 20);
        
        attachmentsLabel.setText("Attachments:");
        mailPanel.add(attachmentsLabel);
        attachmentsLabel.setBounds(20, 180, 80, 20);
        
        // Attachments textField6
        textField6.setEditable(true);
        mailPanel.add(textField6);
        textField6.setBounds(100, 180, 180, 20);
        
        textLabel.setText("Text:");
        mailPanel.add(textLabel);
        textLabel.setBounds(20, 210, 80, 20);
          
        // TEXT textfieled
        textArea2.setEditable(true);
        textArea2.setLineWrap(true);
        textArea2.setWrapStyleWord(true);
        JScrollPane s2 = new JScrollPane(textArea2);
        mailPanel.add(s2);
        s2.setBounds(100, 210, 290, 110);
        
        mailStoreBtn.addActionListener(mailListener);
        mailStoreBtn.setMargin(new Insets(2,2,2,2));
        mailStoreBtn.setText("STORE");
        mailStoreBtn.setToolTipText("Store mail as name given in the choose mail combobox");
        mailPanel.add(mailStoreBtn);
        mailStoreBtn.setBounds(330, 90, 60, 25);
        
        mailDeleteBtn.addActionListener(mailListener);
        mailDeleteBtn.setMargin(new Insets(2,2,2,2));
        mailDeleteBtn.setText("DELETE");
        mailDeleteBtn.setToolTipText("Delete selected mail");
        mailPanel.add(mailDeleteBtn);
        mailDeleteBtn.setBounds(330, 120, 60, 25);
        
        backgroundPanel.add(mailPanel);
        mailPanel.setBounds(0, 260, 410, 360);
        
        // ----INBOX----
        inboxPanel.setLayout(null);
        inboxPanel.setOpaque(false);
        inboxPanel.setBorder(BorderFactory.createTitledBorder("INBOX CONTENT"));
        
        inboxPanel.add(totNewMsgLabel);
        totNewMsgLabel.setBounds(20, 30, 160, 20);
        
        // number of new voice messages
        combo4.addActionListener(countMessages);
        combo4.addItem("0");
        combo4.addItem("1");
        combo4.addItem("2");
        combo4.addItem("3");
        combo4.addItem("4");
        combo4.setSelectedIndex(0);
        inboxPanel.add(combo4);
        combo4.setBounds(20, 60, 40, 20);
        
        inboxPanel.add(newVoiceLabel);
        newVoiceLabel.setBounds(60, 60, 150, 20);
        
        // number of new fax messages
        combo5.addActionListener(countMessages);
        combo5.addItem("0");
        combo5.addItem("1");
        combo5.addItem("2");
        combo5.addItem("3");
        combo5.addItem("4");
        combo5.setSelectedIndex(0);
        inboxPanel.add(combo5);
        combo5.setBounds(20, 90, 40, 20);
        
        inboxPanel.add(newFaxLabel);
        newFaxLabel.setBounds(60, 90, 150, 20);
        
        // number of new emails
        combo6.addActionListener(countMessages);
        combo6.addItem("0");
        combo6.addItem("1");
        combo6.addItem("2");
        combo6.addItem("3");
        combo6.addItem("4");
        combo6.setSelectedIndex(0);
        inboxPanel.add(combo6);
        combo6.setBounds(20, 120, 40, 20);
        
        inboxPanel.add(newEmailLabel);
        newEmailLabel.setBounds(60, 120, 150, 20);
        
        checkBox.setText("Inbox full");
        checkBox.setOpaque(false);
        inboxPanel.add(checkBox);
        checkBox.setBounds(20, 150, 80, 20);

        backgroundPanel.add(inboxPanel);
        inboxPanel.setBounds(420, 260, 295, 190);
        
        //----PREVIEW----
        previewPanel.setLayout(null);
        previewPanel.setOpaque(false);
        previewPanel.setBorder(BorderFactory.createTitledBorder("PREVIEW"));
        
        previewBtn.addActionListener(this);
        previewBtn.setMargin(new Insets(2,2,2,2));
        previewBtn.setText("PREVIEW");
        previewBtn.setToolTipText("Preview the selected template");
        previewPanel.add(previewBtn);
        previewBtn.setBounds(20, 30, 70, 25);
        
        textArea1.setEditable(false);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        JScrollPane s3 = new JScrollPane(textArea1);
        previewPanel.add(s3);
        s3.setBounds(20, 60, 300, 50);
        
        previewPanel.add(warningLabel1);
        warningLabel1.setVisible(false);
        warningLabel1.setBounds(330, 45, 250, 50);
        
        backgroundPanel.add(previewPanel);
        previewPanel.setBounds(0, 630, 715, 130);
        
        getContentPane().add(backgroundPanel, BorderLayout.CENTER);
        
        //----MENUBAR----
        setJMenuBar(menuBar);
        
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);
        
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        fileMenu.add(saveMenuItem);
        saveMenuItem.addActionListener(menuListener);
        
        saveAsMenuItem.setMnemonic(KeyEvent.VK_A);
        fileMenu.add(saveAsMenuItem);
        saveAsMenuItem.addActionListener(menuListener);
        
        fileMenu.addSeparator();
        
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        fileMenu.add(openMenuItem);
        openMenuItem.addActionListener(menuListener);
        
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(menuListener);
        
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(helpMenuItem);
        helpMenuItem.addActionListener(menuListener);
        
        setSize(740,820);
    } // initComponents
    
    //----------------------------------------
    public void actionPerformed(ActionEvent e)
    //----------------------------------------
    {
        if(combo8.getSelectedIndex() != 0 && combo8.getSelectedItem() != null)
        {
            int cursor = textField8.getCaretPosition();
            msg = textField8.getText();
            msg1 = msg.substring(0, cursor);
            msg2 = msg.substring(cursor);
            tag = (String)combo8.getSelectedItem();
            textField8.setText(msg1 + "__" + tag + "__ " + msg2);
            combo8.setSelectedIndex(0);
        }
        else if(e.getActionCommand().equals("Insert COUNT"))
        {
            int cursor2 = subtemplate1.getCaretPosition();
            msg = subtemplate1.getText();
            msg1 = msg.substring(0, cursor2);
            msg2 = msg.substring(cursor2);
            subtemplate1.setText(msg1 + "__COUNT__ " + msg2);
        }
        else if(e.getActionCommand().equals("DELETE"))
            delete();
        else if(e.getActionCommand().equals("PREVIEW"))
            preview();
        else if(e.getActionCommand().equals("CLEAR"))
            textField8.setText("");
    } // actionPerformed
    
    //-------------------------------
    public JTextPane createTextPane(String text1, String text2)
    //-------------------------------
    {
        JTextPane textPane = new JTextPane();
        Document doc = textPane.getDocument();
        
        String[] initString = {text1, text2};
        String[] initStyles = {"regular", "yellow"};
        
        initStylesForTextPane(textPane);
        
        try
	{
            for (int i=0; i < initString.length; i++)
            {
                doc.insertString(doc.getLength(), initString[i],
                textPane.getStyle(initStyles[i]));
            }
	}
	catch (BadLocationException ble)
	{
		System.err.println("Couldn't insert initial text.");
	}
	return textPane;
    } // createTextPane
    
    //---------------------------------------------------
    public void initStylesForTextPane(JTextPane textPane)
    //---------------------------------------------------
    {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = textPane.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = textPane.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = textPane.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        
        s = textPane.addStyle("yellow", regular);
        StyleConstants.setBackground(s, Color.blue);
    } // initStylesForTextPane
    
    //-----------------------------
    public void getProps(File file)
    //-----------------------------
    {
        prop.clear();
        
        // reads the properties list
        try
        {
            FileInputStream in = new FileInputStream(file);
            prop.load(in);
            in.close();
        }
        
        catch (FileNotFoundException fnfe)
        {
            System.err.println("Caught FileNotFoundException: " + fnfe.getMessage());
        }
        
        catch (IOException e)
        {
            System.err.println("Caught IOException: " + e.getMessage());
        }
        
        String pName;
        Enumeration enum = prop.propertyNames();
        combo7.removeAllItems();
        combo7.addItem("--Select template--");
        
        // prints the property names in the combobox
        while(enum.hasMoreElements())
        {
            pName = enum.nextElement().toString();
            printPName(pName);
            if(ok  == true)
            {
                combo7.addItem(pName);
            }
        }
    } // getProps
    
    //-----------------------------
    public void getMailProperties()
    //-----------------------------
    {
        // reads the mail property list
        try
        {
            FileInputStream in = new FileInputStream("/home/loke/ejoemma/sommarjobb/java/mail.phr");
            mailProp.load(in);
            in.close();
        }
        
        catch (FileNotFoundException fnfe)
        {
            System.err.println("Caught FileNotFoundException: " + fnfe.getMessage());
        }
        
        catch (IOException e)
        {
            System.err.println("Caught IOException: " + e.getMessage());
        }
        
        String pName;
        Enumeration enum = mailProp.propertyNames();
        combo3.addItem("--Choose mail--");
        // prints the property names in the combobox
        while(enum.hasMoreElements())
        {
            pName = enum.nextElement().toString();
            combo3.addItem(pName);
        }
    } // getMailProperties
    
    //-------------------------
    public void storeTemplate()
    //-------------------------
    {
        if(combo7.getSelectedIndex() != 0)
        {
             prop.setProperty((String)combo7.getSelectedItem(), textField8.getText());
        }
        
        if(subtemplateInt == 1) // __NUM_NEW_TOT_TEXT__
        {
            prop.setProperty("NumNewTotText", subtemplate1.getText());
            prop.setProperty("NumNewTotText0", subtemplate2.getText());
            prop.setProperty("NumNewTotText1", subtemplate3.getText());
        }
        else if(subtemplateInt == 2) // __NUM_NEW_VOICE_TEXT__
        {
            prop.setProperty("NumNewVoiceText", subtemplate1.getText());
            prop.setProperty("NumNewVoiceText0", subtemplate2.getText());
            prop.setProperty("NumNewVoiceText1", subtemplate3.getText());
        }
        else if(subtemplateInt == 3) // __NUM_NEW_FAX_TEXT__
        {
            prop.setProperty("NumNewFaxText", subtemplate1.getText());
            prop.setProperty("NumNewFaxText0", subtemplate2.getText());
            prop.setProperty("NumNewFaxText1", subtemplate3.getText());
        }
        else if(subtemplateInt == 4) //__NUM_NEW_EMAIL_TEXT__
        {
            prop.setProperty("NumNewEmailText", subtemplate1.getText());
            prop.setProperty("NumNewEmailText0", subtemplate2.getText());
            prop.setProperty("NumNewEmailText1", subtemplate3.getText());
        }
        else if(subtemplateInt == 5) // __MSG_SIZE__
        {
            prop.setProperty("VoiceSize", subtemplate1.getText());
            prop.setProperty("FaxSize", subtemplate2.getText());
            prop.setProperty("EmailSize", subtemplate3.getText());
        }
        else if(subtemplateInt == 6) // __QUOTA_TEXT__
        {
            prop.setProperty("QuotaText", subtemplate1.getText());
        }
    } // storeTemplate
    
    //---------------------
    public void storeMail()
    //---------------------
    {
        int status = combo1.getSelectedIndex();
        int depositType = combo2.getSelectedIndex();
        String msg = (String)combo3.getSelectedItem();
        String from = textField1.getText();
        String subject = textField2.getText();
        String size = textField5.getText();
        String attachment = textField6.getText();
        String text = textArea2.getText();
        
        String mailPropMsg = status + ":" + depositType + ":" + from + ":"
        + subject + ":" + size + ":" + attachment + ":" + text;
        
        if(!mailProp.containsKey(msg) && combo3.getSelectedIndex() != 0)
        {
            combo3.insertItemAt(msg,1);
        }
        
        if(combo3.getSelectedIndex() != 0)
        {
            mailProp.setProperty(msg, mailPropMsg);
        }
        
        try
        {
            FileOutputStream out = new FileOutputStream("/home/loke/ejoemma/sommarjobb/java/mail.phr");
            mailProp.store(out, "---No Comment---");
            out.close();
        }
        
        catch (FileNotFoundException fnfe)
        {
            System.err.println("Caught FileNotFoundException: " + fnfe.getMessage());
        }
        
        catch (IOException ioe)
        {
            System.err.println("Caught IOException: " + ioe.getMessage());
        }
    } // storeMail
    
    //----------------------
    public void mailDelete()
    //----------------------
    {
        String mailToDelete = (String)combo3.getSelectedItem();
        int indexToDelete = combo3.getSelectedIndex();
        if(indexToDelete != 0)
        {
            mailProp.remove(mailToDelete);
            combo3.removeItemAt(indexToDelete);
            combo3.setSelectedIndex(0);
        }
    } // mailDelete
    
    //-------------------
    public void preview()
    //-------------------
    {
        storeMail();
        storeTemplate();
        save(file);
        
        String content = (String)combo7.getSelectedItem();
        String depositType = "";
        String status = "";
        int mailStatus = combo1.getSelectedIndex();
        int mailDepositType = combo2.getSelectedIndex();
        int newVoice = combo4.getSelectedIndex();
        int newFax = combo5.getSelectedIndex();
        int newEmail = combo6.getSelectedIndex();
        int size = Integer.parseInt(textField5.getText());
        int numAttachments = Integer.parseInt(textField6.getText());
        boolean overQuota = false;
        if(checkBox.isSelected())
        {
            overQuota = true;
        }
        
        String attachment = "";
        String boundry = "\r\nJag_jobbar_pa_mobeon\r\n";
        
        if(mailDepositType == 1) // voice
        {
            depositType = "multipart/voice-message";
            attachment = "Content-Type: AUDIO/wav\r\n"
            + "Content-Description: Abcxyz voice message (" + size + " seconds)\r\n"
            + "\r\n"
            + "Silly text\r\n";
        }
        else if(mailDepositType == 2) // fax
        {
            depositType = "multipart/fax-message";
            attachment = "Content-Type: Image/tiff\r\n"
            + "\r\n"
            + "Silly text\r\n";
        }
        else if(mailDepositType == 3) // email
        {
            if(numAttachments == 0)
                depositType = "text/plain";
            else if(numAttachments >  0)
            {
                depositType = "multipart/mix";
                attachment = "Content-Type: text/plain\r\n"
                + textArea2.getText() + "\r\n"
                + "\r\n";
            }
        }
        else
        {
            depositType = "text/plain";
        }
       
        if(mailStatus == 2)
        {
            status = "x-priority : 1"; // urgent
        }
        else
        {
            status = "x-priority : 3"; // normal
        }
        
        String header = "From: " + textField1.getText() + "\r\n"
        + "Subject: " + textField2.getText() + "\r\n"
        + "X-Priority: " + status + "\r\n"        
        + "Content-Type: " + depositType + "; boundary=Jag_jobbar_pa_mobeon"
        + "\r\n";
        
        String body = boundry;
        
        // creates all attachments but the last one
        for(int i = 0; i < numAttachments-1; i++)
        {
            body = body + attachment + boundry;
        }
        
        // adjust the size of the last attachment
        if(mailDepositType == 3) // email
        {
            int mailLength = header.length() + body.length();
            while(mailLength < size - body.length())
            {
                body = body + textArea2.getText() + "\n";
                mailLength = header.length() + body.length();
            }
            while(mailLength < size)
            {
                body = body + "fill\n";
                mailLength = header.length() + body.length();
            }
            body = body + boundry;
        }
        else
        {
            body = body + attachment + boundry;
        }
        
        NotificationEmail email = new NotificationEmail(header + body);
        
        textArea1.setText(TextCreator.get().generateText(
            new UserMailbox(newVoice,newFax,newEmail,overQuota), email, new TestUser(),content));
        
        int characters = textArea1.getText().length();
        if(characters > 160 && characters < 321)
        {
            warningString = "<html>WARNING!<br>The message will be sent in two parts.";
            warningLabel1.setForeground(Color.red);
            warningLabel1.setText(warningString);
            warningLabel1.setVisible(true);
        }
        else if(characters > 320)
        {
            warningString = "<html>WARNING!<br>The message will be sent in several parts.";
            warningLabel1.setText(warningString);
            warningLabel1.setVisible(true);}
        else
        {
            warningString = "";
            warningLabel1.setVisible(false);
        }
    } // preview
    
    //------------------
    public void delete()
    //------------------
    {
        // delete the selected item from the combobox and from the properties list
        String itemToDelete = (String)combo7.getSelectedItem();
        int index = combo7.getSelectedIndex();
        if(index != 0)
        {
            prop.remove(itemToDelete);
            combo7.removeItemAt(index);
            combo7.setSelectedIndex(0);
        }
        
        save(file);
        textArea1.setText("");
        
    } // delete
    
    //-------------------------
    public void save(File file)
    //-------------------------
    {
        // saves a .phr file
        try
        {
            FileOutputStream out = new FileOutputStream(file);
            prop.store(out, "---No Comment---");
            out.close();
        }
        
        catch (FileNotFoundException fnfe)
        {
            System.err.println("Caught FileNotFoundException: " + fnfe.getMessage());
        }
        
        catch (IOException ioe)
        {
            System.err.println("Caught IOException: " + ioe.getMessage());
        }
        
        se.abcxyz.moip.ntf.text.Phrases.refresh();
    } // save
    
    //-------------------------
    public void countMessages()
    //-------------------------
    {
        String countString;
        int countVoice = 0;
        int countFax = 0;
        int countEmail = 0;
        int totalMessages = 0;
        
        countVoice = combo4.getSelectedIndex();
        if (countVoice < 0) countVoice = 0;
        countFax = combo5.getSelectedIndex();
        if (countFax < 0) countFax = 0;
        countEmail = combo6.getSelectedIndex();
        if (countEmail < 0) countEmail = 0;
        
        totalMessages = countVoice+countFax+countEmail;
        
        if(countVoice >=2)
            newVoiceLabel.setText(" new Voice messages");
        if(countVoice == 1)
            newVoiceLabel.setText(" new Voice message");
        if(countVoice == 0)
            newVoiceLabel.setText(" no new Voice messages");
        
        if(countFax >=2)
            newFaxLabel.setText(" new Fax messages");
        if(countFax == 1)
            newFaxLabel.setText(" new Fax message");
        if(countFax == 0)
            newFaxLabel.setText(" no new Fax messages");
        
        if(countEmail >=2)
            newEmailLabel.setText(" new Email messages");
        if(countEmail == 1)
            newEmailLabel.setText(" new Email message");
        if(countEmail == 0)
            newEmailLabel.setText(" no new Email messages");
        
        if(totalMessages >= 2)
            totNewMsgLabel.setText("You have " + totalMessages + " new messages");
        if(totalMessages == 1)
            totNewMsgLabel.setText("You have " + totalMessages + " new message");
        if(totalMessages == 0)
            totNewMsgLabel.setText("You have no new messages");
    } // countMessages
    
    //--------------------------------------
    public boolean printPName(String pName)
    //--------------------------------------
    {
        ok = true;
        
        if(pName.equals("urgent"))
            ok = false;
        else if(pName.equals("normal"))
            ok = false;
        else if(pName.equals("email"))
            ok = false;
        else if(pName.equals("fax"))
            ok = false;
        else if(pName.equals("voice"))
            ok = false;
        else if(pName.equals("mailquotaexceeded"))
            ok = false;
        else if(pName.equals("FaxSize"))
            ok = false;
        else if(pName.equals("EmailSize"))
            ok = false;
        else if(pName.equals("VoiceSize"))
            ok = false;
        else if(pName.equals("NumNewTotText"))
            ok = false;
        else if(pName.equals("NumNewFaxText"))
            ok = false;
        else if(pName.equals("NumNewEmailText"))
            ok = false;
        else if(pName.equals("NumNewVoiceText"))
            ok = false;
        else if(pName.equals("QuotaText"))
            ok = false;
        else if(pName.equals("NumNewTotText0"))
            ok = false;
        else if(pName.equals("NumNewFaxText0"))
            ok = false;
        else if(pName.equals("NumNewVoiceText0"))
            ok = false;
        else if(pName.equals("NumNewTotText1"))
            ok = false;
        else if(pName.equals("NumNewFaxText1"))
            ok = false;
        else if(pName.equals("NumNewEmailText1"))
            ok = false;
        else if(pName.equals("NumNewVoiceText1"))
            ok = false;
        
        return(ok);
    }
    
    //--------------------------------------
    public boolean isSubtemplate(String tag)
    //--------------------------------------
    {
        boolean isSubtemplate = false;
        
        if(tag.equals("__NUM_NEW_TOT_TEXT__"))
            isSubtemplate = true;
        else if(tag.equals("__NUM_NEW_VOICE_TEXT__"))
            isSubtemplate = true;
        else if(tag.equals("__NUM_NEW_FAX_TEXT__"))
            isSubtemplate = true;
        else if(tag.equals("__NUM_NEW_EMAIL_TEXT__"))
            isSubtemplate = true;
        else if(tag.equals("__SIZE__"))
            isSubtemplate = true;
        else if(tag.equals("__COUNT__"))
            isSubtemplate = true;
        else if(tag.equals("__QUOTA_TEXT__"))
            isSubtemplate = true;
        else
            isSubtemplate = false;
        return isSubtemplate;
    } // isSubtemplate
    //-------------------------------------
    public void subtemplates(String tag)
    //-------------------------------------
    {
        if(tag.equals("__NUM_NEW_TOT_TEXT__"))
        {
            subtemplateInt = 1;
            showSubtemplates(tag);
            subtemplateLabel1.setText("Number of new messages");
            subtemplateLabel2.setText("No new messages");
            subtemplateLabel3.setText("One new message");
            subtemplate1.setText(prop.getProperty("NumNewTotText"));
            subtemplate2.setText(prop.getProperty("NumNewTotText0"));
            subtemplate3.setText(prop.getProperty("NumNewTotText1"));
        }
        else if(tag.equals("__NUM_NEW_VOICE_TEXT__"))
        {
            subtemplateInt = 2;
            showSubtemplates(tag);
            subtemplateLabel1.setText("Number of new voice messages");
            subtemplateLabel2.setText("No new vocie messages");
            subtemplateLabel3.setText("One new voice message");
            subtemplate1.setText(prop.getProperty("NumNewVoiceText"));
            subtemplate2.setText(prop.getProperty("NumNewVoiceText0"));
            subtemplate3.setText(prop.getProperty("NumNewVoiceText1"));
        }
        else if(tag.equals("__NUM_NEW_FAX_TEXT__"))
        {
            subtemplateInt = 3;
            showSubtemplates(tag);
            subtemplateLabel1.setText("Number of new fax messages");
            subtemplateLabel2.setText("No new fax messages");
            subtemplateLabel3.setText("One new fax message");
            subtemplate1.setText(prop.getProperty("NumNewFaxText"));
            subtemplate2.setText(prop.getProperty("NumNewFaxText0"));
            subtemplate3.setText(prop.getProperty("NumNewFaxText1"));
        }
        else if(tag.equals("__NUM_NEW_EMAIL_TEXT__"))
        {
            subtemplateInt = 4;
            showSubtemplates(tag);
            subtemplateLabel1.setText("Number of new email messages");
            subtemplateLabel2.setText("No new email messages");
            subtemplateLabel3.setText("One new email message");
            subtemplate1.setText(prop.getProperty("NumNewEmailText"));
            subtemplate2.setText(prop.getProperty("NumNewEmailText0"));
            subtemplate3.setText(prop.getProperty("NumNewEmailText1"));
        }
        else if(tag.equals("__MSG_SIZE__"))
        {
            subtemplateInt = 5;
            showSubtemplates(tag);
            subtemplateLabel1.setText("Voice size");
            subtemplateLabel2.setText("Fax size");
            subtemplateLabel3.setText("Email size");
            subtemplate1.setText(prop.getProperty("VoiceSize"));
            subtemplate2.setText(prop.getProperty("FaxSize"));
            subtemplate3.setText(prop.getProperty("EmailSize"));
        }
        else if(tag.equals("__QUOTA_TEXT__"))
        {
            subtemplateInt = 6;
            subtemplatePanel.setBorder(BorderFactory.createTitledBorder("SUBTEMPLATE: " + tag));
            subtemplateLabel1.setText("Over quota");
            subtemplate1.setBackground(Color.white);
            subtemplate1.setText(prop.getProperty("QuotaText"));
        }
        else
        {
            hideSubtemplates();
            subtemplateInt = 0;
        }
    } // subtemplates
    
    //--------------------------------------
    public void showSubtemplates(String tag)
    //--------------------------------------
    {
        subtemplatePanel.setBorder(BorderFactory.createTitledBorder("Subtemplate: " + tag));
        subtemplate1.setBackground(Color.white);
        subtemplate2.setBackground(Color.white);
        subtemplate3.setBackground(Color.white);
        
        if(tag.equals("__MSG_SIZE__"))
        {
            tagSizeBtn1.setVisible(true);
            tagSizeBtn2.setVisible(true);
            tagSizeBtn3.setVisible(true);
        }
        else
        {
            tagCountBtn.setVisible(true);
        }
        
        subtemplatesChanged();
    } // showSubtemplates
    
    //----------------------------
    public void hideSubtemplates()
    //----------------------------
    {
        subtemplatesChanged();
        tagCountBtn.setVisible(false);
        tagSizeBtn1.setVisible(false);
        tagSizeBtn2.setVisible(false);
        tagSizeBtn3.setVisible(false);
        subtemplatePanel.setBorder(BorderFactory.createTitledBorder("Subtemplate: No tag with subtemplates is choosen."));
        subtemplateLabel1.setText("");
        subtemplateLabel2.setText("");
        subtemplateLabel3.setText("");
        subtemplate1.setText("");
        subtemplate1.setBackground(Color.lightGray);
        subtemplate2.setText("");
        subtemplate2.setBackground(Color.lightGray);
        subtemplate3.setText("");
        subtemplate3.setBackground(Color.lightGray);
    } // hideSubtemplates
    
    //----------------------
    public boolean changes()
    //----------------------
    {
        boolean change = true;
        
        int status = combo1.getSelectedIndex();
        int depositType = combo2.getSelectedIndex();
        String msg = (String)combo3.getSelectedItem();
        String from = textField1.getText();
        String subject = textField2.getText();
        String size = textField5.getText();
        String attachment = textField6.getText();
        String text = textArea2.getText();
        
        String mailPropMsg = status + ":" + depositType + ":" + from + ":" + subject + ":"
                             + size + ":" + attachment + ":" + text;
        
        String templateChoice = (String)combo7.getSelectedItem();
        String templatePhrase = textField8.getText();
        
        if(templatePhrase.equals(prop.getProperty(templateChoice)))
        {
            change = false;
        }
        if(mailPropMsg.equals(mailProp.getProperty(msg)))
        {
            change = false;
        }
        if(!subtemplatesChanged())
        {
            change = false;
        }
        else
        {
            change = true;
        }
        
        return change;
    } // changes
    
    //----------------------------------
    public boolean subtemplatesChanged()
    //----------------------------------
    {
        boolean change = true;
        
        if(subtemplateInt == 1) // __NUM_NEW_TOT_TEXT__
        {
            if( subtemplate1.getText().equals(prop.getProperty("NumNewTotText")) &&
                subtemplate2.getText().equals(prop.getProperty("NumNewTotText0")) &&
                subtemplate3.getText().equals(prop.getProperty("NumNewTotText1")))
            {
                change = false;
            }
        }
        else if(subtemplateInt == 2) // __NUM_NEW_VOICE_TEXT__
        {
            if( subtemplate1.getText().equals(prop.getProperty("NumNewVoiceText")) &&
                subtemplate2.getText().equals(prop.getProperty("NumNewVoiceText0")) &&
                subtemplate3.getText().equals(prop.getProperty("NumNewVoiceText1")))
            {
                change = false;
            }
        }
        else if(subtemplateInt == 3) // __NUM_NEW_FAX_TEXT__
        {
            if( subtemplate1.getText().equals(prop.getProperty("NumNewFaxText")) &&
                subtemplate2.getText().equals(prop.getProperty("NumNewFaxText0")) &&
                subtemplate3.getText().equals(prop.getProperty("NumNewFaxText1")))
            {
                change = false;
            }
        }
        else if(subtemplateInt == 4) //__NUM_NEW_EMAIL_TEXT__
        {
            if( subtemplate1.getText().equals(prop.getProperty("NumNewEamilText")) &&
                subtemplate2.getText().equals(prop.getProperty("NumNewEmailText0")) &&
                subtemplate3.getText().equals(prop.getProperty("NumNewEmailText1")))
            {
                change = false;
            }
        }
        else if(subtemplateInt == 5) // __MSG_SIZE__
        {
            if( subtemplate1.getText().equals(prop.getProperty("VoiceSize")) &&
                subtemplate2.getText().equals(prop.getProperty("FaxSize")) &&
                subtemplate3.getText().equals(prop.getProperty("EmailSize")))
            {
                change = false;
            }
        }
        else if(subtemplateInt == 6) // __QUOTA_TEXT__
        {
            if(subtemplate1.getText().equals(prop.getProperty("QuotaText")))
            {
                change = false;
            }
        }
        
        return change;
    } // subtemplatesChanged()
    
    //------------------------------------
    public static void main(String[] args)
    //------------------------------------
    {
        Phrase1 frame = new Phrase1();
        frame.show();
    } // main
    
    //------------------------------------------------------------
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent)
    //------------------------------------------------------------
    {
        String msg = textField8.getText();
        String tag = "";
        int cursor = textField8.getCaretPosition();
        int tagStart = msg.indexOf("__");
        int tagEnd = 0;
        
        while(tagStart < msg.lastIndexOf("__"))
        {
            tagEnd = msg.indexOf("__", tagStart + 2);
            
            if(cursor > tagStart && cursor < tagEnd + 2)
            {
                tag = msg.substring(tagStart, tagEnd + 2);
                subtemplates(tag);
                break;
            }
            else
            {
                hideSubtemplates();
            }
            
            msg = msg.substring(tagEnd+2);
            cursor = cursor - (tagEnd + 2);
            
            if(cursor < 0)
            {
                break;
            }
            
            tagStart = msg.indexOf("__");
        }
    } // mouseClicked
    
    public void mousePressed(MouseEvent event){}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    
    //--------------------------------------------------
    private class MenuListener implements ActionListener
    //--------------------------------------------------
    {
        //----------------------------------------
        public void actionPerformed(ActionEvent e)
        //----------------------------------------
        {
            if(e.getActionCommand().equals("Save"))
            {
                storeTemplate();
                storeMail();
                save(file);
            }
            else if(e.getActionCommand().equals("Save As"))
            {
                fileChooser.addChoosableFileFilter(simpleFileFilter);
                fileChooser.setAcceptAllFileFilterUsed(false);
                File currentDirectory = new File("/home/loke/ejoemma/sommarjobb/java/");
                fileChooser.setCurrentDirectory(currentDirectory);
                int returnVal = fileChooser.showSaveDialog(Phrase1.this);
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    file = fileChooser.getSelectedFile();
                    storeTemplate();
                    save(file);
                }
            }
            else if(e.getActionCommand().equals("Open"))
            {
                save(file);
                fileChooser.addChoosableFileFilter(simpleFileFilter);
                fileChooser.setAcceptAllFileFilterUsed(false);
                File currentFile = new File("/home/loke/ejoemma/sommarjobb/java/");
                fileChooser.setCurrentDirectory(currentFile);
                int returnVal = fileChooser.showOpenDialog(Phrase1.this);
                if(returnVal == JFileChooser.APPROVE_OPTION)
                {
                    file = fileChooser.getSelectedFile();
                    getProps(file);
                    textArea1.setText("");
                }
            }
            else if(e.getActionCommand().equals("Exit"))
            {
                if(changes())
                {
                    int  selectedValue = JOptionPane.showInternalConfirmDialog(getContentPane(), "Save changes before exiting?", "Exit",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    
                    if(selectedValue == JOptionPane.YES_OPTION)
                    {
                        storeMail();
                        storeTemplate();
                        save(file);
                        System.exit(0);
                    }
                    else if(selectedValue == JOptionPane.NO_OPTION)
                    {
                        System.exit(0);
                    }
                }
                else
                {
                    System.exit(0);
                }
            }
            else if(e.getActionCommand().equals("Help"))
            {
                helpOptionPane.showMessageDialog(null, "Version 1.0", "About DEMO SMS Configurator",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } // actionPerformed
    } // MenuListener
    
    //---------------------------------------------------
    private class CountListener implements ActionListener
    //---------------------------------------------------
    {
        public void actionPerformed(ActionEvent e)
        {
            countMessages();
        }
    } // CountListener
    
    //------------------------------------------------------
    private class TemplateListener implements ActionListener
    //------------------------------------------------------
    {
        //----------------------------------------
        public void actionPerformed(ActionEvent e)
        //----------------------------------------
        {
            String tag = "";
            String msg = "";
            int startIndex = 0;
            int tagStart = 0;
            int tagEnd = 0;
            
            hideSubtemplates();
            newPName = (String)combo7.getSelectedItem(); // key attribute
            newPValue = textField8.getText();
            
            // if not already contained in the property list
            if(!prop.containsKey(newPName) && combo7.getSelectedIndex() != 0)
            {
                combo7.insertItemAt(newPName,1);
                prop.setProperty(newPName, newPValue);
                save(file);
            }
            
            pValue = prop.getProperty(newPName);
            textField8.setText(pValue);
            msg = textField8.getText();
            
            if(msg.indexOf("__") > 0)
            {
                
                textField8.setText("");
                
                tagStart = msg.indexOf("__");
                
                while(tagStart < msg.lastIndexOf("__"))
                {
                    tagEnd = msg.indexOf("__", tagStart + 2);
                    tag = msg.substring(tagStart, tagEnd + 2);
                    if(isSubtemplate(tag))
                    {
                        System.out.println("I isSubtemplate true");
                        createTextPane(msg.substring(0, tagStart), msg.substring(tagStart, tagEnd + 2));
                        //htmlString = htmlString + msg.substring(0, tagStart) +
                        //"<b><i>" + msg.substring(tagStart, tagEnd + 2) + "</i></b>";
                        
                    }
                    else
                    {
                        System.out.println("I isSubtemplate false");
                        
                       // htmlString = htmlString + msg.substring(0, tagStart) +
                        //"<b>" + msg.substring(tagStart, tagEnd + 2) + "</b>";
                    }
                    msg = msg.substring(tagEnd + 2);
                    tagStart = msg.indexOf("__");
                }
                //htmlString = htmlString + msg.substring(tagEnd + 2);
                
                
                textField8.setText(pValue);
            }
            else
            {
                textField8.setText(pValue);
            }
        } // actionPerformed
    } // TemplateListener
    
    //---------------------------------------------------------
    private class TagSizeBtnListener1 implements ActionListener
    //---------------------------------------------------------
    {
        //----------------------------------------
        public void actionPerformed(ActionEvent e)
        //----------------------------------------
        {
            int cursor = subtemplate1.getCaretPosition();
            msg = subtemplate1.getText();
            msg1 = msg.substring(0, cursor);
            msg2 = msg.substring(cursor);
            subtemplate1.setText(msg1 + "__SIZE__ " + msg2);
        }
    } // TagSizeBtnListener1
    
    //---------------------------------------------------------
    private class TagSizeBtnListener2 implements ActionListener
    //---------------------------------------------------------
    {
        //----------------------------------------
        public void actionPerformed(ActionEvent e)
        //----------------------------------------
        {
            int cursor = subtemplate2.getCaretPosition();
            msg = subtemplate2.getText();
            msg1 = msg.substring(0, cursor);
            msg2 = msg.substring(cursor);
            subtemplate2.setText(msg1 + "__SIZE__ " + msg2);
        }
    } // TagSizeBtnListener2
    
    //---------------------------------------------------------
    private class TagSizeBtnListener3 implements ActionListener
    //---------------------------------------------------------
    {
        //----------------------------------------
        public void actionPerformed(ActionEvent e)
        //----------------------------------------
        {
            int cursor = subtemplate3.getCaretPosition();
            msg = subtemplate3.getText();
            msg1 = msg.substring(0, cursor);
            msg2 = msg.substring(cursor);
            subtemplate3.setText(msg1 + "__SIZE__ " + msg2);
        }
    } // TagSizeBtnListener3
    
    //--------------------------------------------------
    private class MailListener implements ActionListener
    //--------------------------------------------------
    {
        //----------------------------------------
        public void actionPerformed(ActionEvent e)
        //----------------------------------------
        {
            if(e.getActionCommand().equals("STORE"))
            {
                storeMail();
            }
            
            else if(e.getActionCommand().equals("DELETE"))
            {
                mailDelete();
            }
            
            // updates the textfields with text from the property file
            String key = (String)combo3.getSelectedItem();
            
            // if not already contained in the mail property list
            if(!mailProp.containsKey(key) && combo3.getSelectedIndex() != 0)
            {
                storeMail();
            }
            
            String msg = mailProp.getProperty(key);
            int startIndex = 0;
            
            if(combo3.getSelectedIndex() != 0)
            {
                // status
                startIndex = msg.indexOf(":");
                combo1.setSelectedIndex(Integer.parseInt(msg.substring(0,startIndex)));
                msg = msg.substring(startIndex+1);
                
                // depositType
                startIndex = msg.indexOf(":");
                combo2.setSelectedIndex(Integer.parseInt(msg.substring(0,startIndex)));
                msg = msg.substring(startIndex+1);
                
                // from
                startIndex = msg.indexOf(":");
                textField1.setText(msg.substring(0,startIndex));
                msg = msg.substring(startIndex + 1);
                
                // subject
                startIndex = msg.indexOf(":");
                textField2.setText(msg.substring(0,startIndex));
                msg = msg.substring(startIndex + 1);
                
                // size
                startIndex = msg.indexOf(":");
                textField5.setText(msg.substring(0,startIndex));
                msg = msg.substring(startIndex + 1);
                
                // attachment
                startIndex = msg.indexOf(":");
                textField6.setText(msg.substring(0,startIndex));
                msg = msg.substring(startIndex + 1);
                
                // text
                textArea2.setText(msg);
            }
            else
            {
                combo1.setSelectedIndex(0);
                combo2.setSelectedIndex(0);
                textField1.setText("");
                textField2.setText("");
                textField3.setText("");
                textField5.setText("");
                textField6.setText("");
                textArea2.setText("");
            }
        }// actionPerformed
    }// MailListener
    
    //----------------------------------------
    private class TestUser implements UserInfo
    //----------------------------------------
    {
        String[] services= {""};
        public String getFullId() {return "";}
	public String getMail() {return "";}
	public String getTelephoneNumber() {return "";}
	public NotificationFilter getFilter() {return null;}
	public String getMmsCenterId() {return "";}
	public int getNotifExpTime() {return 0;}
	public String getNotifNumber() {return "";}
	public int getNumberingPlan() {return 0;}
	public String getPreferredDateFormat() {return "";}
	public String getPreferredTimeFormat() {return "";}
	public String getSmscId() {return "";}
	public int getTypeOfNumber() {return 0;}
        public boolean isMwiUser() {return true;}
	public boolean isMailboxDelivery() {return false;}
	public boolean isAdministrator() {return false;}
	public String getPreferredLanguage()
        {
            String language = file.getPath();
            int lastDot = language.lastIndexOf(".");
            int lastSlash = language.lastIndexOf("/", lastDot);
            language = language.substring(lastSlash+1, lastDot);
            
            return(language);
        }
	public String getLogin() {return "testuser";}
	public String getWapGatewayId() {return "";}
	public boolean isOutdialUser() {return false;}
	public boolean hasMailType(int type) {return false;}
        public boolean isNotifTypeDisabled(int type) { return false; }
	public String[] getServices() {return services;}
        
        //-----------------------------------------
        public boolean isBusinessTime(Calendar cal)
        //-----------------------------------------
        {
            //Hardcoded business times Mon-Fri, 8-17
	    if (cal.get(cal.DAY_OF_WEEK) == cal.SATURDAY
		|| cal.get(cal.DAY_OF_WEEK) == cal.SUNDAY) {
		return false;
	    }

	    int time= cal.get(cal.HOUR_OF_DAY)*100 + cal.get(cal.MINUTE);

	    return time >= 800 && time < 1700;
	}
        
        //--------------------------------
        public String getUsersDate(Date d)
        //--------------------------------
        {
            String dateString;
            SimpleDateFormat fmt;
            
            fmt= new SimpleDateFormat("yyyyMMdd");
            
            return fmt.format(d);
        }
        
        //--------------------------------
        public String getUsersTime(Date d)
        //--------------------------------
        {
            SimpleDateFormat fmt;
            fmt= new SimpleDateFormat("HH:mm");
                       
            return fmt.format(d);
        }
    }
          
    //-----------------------------------------------------------------------
    private class SimpleFileFilter extends javax.swing.filechooser.FileFilter
    //-----------------------------------------------------------------------
    {
        //------------------------------
        public boolean accept(File file)
	//------------------------------
        {
            if(file.isDirectory())
            {
                return true;
            }
            
            String extension = getExtension(file);
            if(extension != null)
            {
                if(extension.equalsIgnoreCase("phr"))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            return false;
        } // accept
        
        //------------------------------------
        private String getExtension(File file)
        //------------------------------------
        {
            String ext = null;
            String s = file.getName();
            int i = s.lastIndexOf(".");
            
            if(i>0 && i<s.length()-1)
            {
                ext = s.substring(i+1).toLowerCase();
            }
            
            return ext;
        } // getExtension
        
        //----------------------------
        public String getDescription()
        //----------------------------
        {
            return "Phrase files";
        } // getDescription
    } // SimpleFileFilter
 
    private JButton deleteBtn;
    private JButton previewBtn;
    private JButton clearBtn;
    private JButton mailStoreBtn;
    private JButton mailDeleteBtn;
    private JButton tagCountBtn;
    private JButton tagSizeBtn1;
    private JButton tagSizeBtn2;
    private JButton tagSizeBtn3;
    private JFileChooser fileChooser;
    private JCheckBox checkBox;
    private JComboBox combo1;
    private JComboBox combo2;
    private JComboBox combo3;
    private JComboBox combo4;
    private JComboBox combo5;
    private JComboBox combo6;
    private JComboBox combo7;
    private JComboBox combo8;
    private JLabel dateLabel;
    private JLabel fromLabel;
    private JLabel inboxLabel;
    private JLabel mailLabel;
    private JLabel newEmailLabel;
    private JLabel newFaxLabel;
    private JLabel newVoiceLabel;
    private JLabel phraseLabel;
    private JLabel previewLabel;
    private JLabel subjectLabel;
    private JLabel totNewMsgLabel;
    private JLabel msgSizeLabel;
    private JLabel attachmentsLabel;
    private JLabel textLabel;
    private JLabel subtemplateHeader;
    private JLabel subtemplateLabel1;
    private JLabel subtemplateLabel2;
    private JLabel subtemplateLabel3;
    private JLabel warningLabel1;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem saveMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem helpMenuItem;
    private JTextPane textPane;
    private JOptionPane helpOptionPane;
    private JOptionPane exitOptionPane;
    private JPanel backgroundPanel;
    private JPanel templatePanel;
    private JPanel subtemplatePanel;
    private JPanel mailPanel;
    private JPanel inboxPanel;
    private JPanel previewPanel;
    private JPanel textPanel;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField8;
    private JTextField testTextField;
    private JTextField subtemplate1;
    private JTextField subtemplate2;
    private JTextField subtemplate3;
    private MenuListener menuListener;
    private CountListener countMessages;
    private TemplateListener templateListener;
    private TagSizeBtnListener1 tagSizeBtnListener1;
    private TagSizeBtnListener2 tagSizeBtnListener2;
    private TagSizeBtnListener3 tagSizeBtnListener3;
    private MailListener mailListener;
    private SimpleFileFilter simpleFileFilter;
    private TestUser testUser;
}