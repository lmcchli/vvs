package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;

class SMPPTestClientGuiBuilder {
    private DataSmTester shortMessageTester = null;
    private CharsetTester charsetTester = null;
    private BindingTester bindingTester = null;

    SMPPTestClientGuiBuilder(DataSmTester shortMessageTester, CharsetTester charsetTester, BindingTester bindingTester) {
        this.shortMessageTester = shortMessageTester;
        this.charsetTester = charsetTester;
        this.bindingTester = bindingTester;
    }

    /**
     * Responsible for creating the JFrame and adding all the components to it.
     */
    void createGui() {
        // Initialise the JFrame and set it's properties.
        JFrame jFrame = new JFrame();
        jFrame.setTitle("SMPP Test Client");
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
        JPanel jPanelForDataSmTests = initializeJPanelForDataSmTests();
        JPanel jPanelForCharsetTests = initializeJPanelForCharsetTests();
        JPanel jPanelForBindingTests = initializeJpanelForBindingTests();
        Container pane = jFrame.getContentPane();
        pane.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        // The first row holds the "data_sm tester JPanel" and the "charset tester JPanel".
        JPanel row1 = new JPanel();
        row1.setLayout(new GridLayout(1, 2));
        row1.add(jPanelForDataSmTests);
        row1.add(jPanelForCharsetTests);

        // The second row holds the "binding tester JPanel".
        JPanel row2 = new JPanel();
        row2.setLayout(new GridLayout(1, 1));
        row2.add(jPanelForBindingTests);

        // Add the first and second row JPanels to the main content pane so that it will be displayed.
        gridBagConstraints.gridy = 0;
        pane.add(row1, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        pane.add(row2, gridBagConstraints);
        jFrame.setSize(600, 400);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

    /**
     *Responsible for creating a JPanel with all of the components required to run the data_sm tests.
     * @return A JPanel with all of the swing components required to run the data_sm tests.
     */
    private JPanel initializeJPanelForDataSmTests() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(5, 1));
        jPanel.setBorder(BorderFactory.createTitledBorder("Data_Sm Tests"));

        // Initialise button for sending text mail.
        JButton textMailJButton = new JButton("Send data_sm for text mail");
        textMailJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                shortMessageTester.sendDataSmForTextMail();
            }
        });

        // Initialise button for sending incoming call notification.
        JButton incomingCallNotifiationJButton = new JButton("Send data_sm for incoming call notification");
        incomingCallNotifiationJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                shortMessageTester.sendDataSmForIncomingCallNotification();
            }
        });

        // Initialise button for sending new message notification.
        JButton messageNotificationJButton = new JButton("Send data_sm for message_notification");
        messageNotificationJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                shortMessageTester.sendDataSmForMessageNotification();
            }
        });

        // Initialise button for sending read notification.
        JButton readNotificationJButton = new JButton ("Send data_sm for read_notification");
        readNotificationJButton.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent event){
                shortMessageTester.sendDataSmForReadMessageNotification();
            }
        });
        
        // Initialise button for sending read notification.
        JButton fmcJButton = new JButton ("Send submit_sm for FMC");
        fmcJButton.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent event){
                shortMessageTester.sendSubmitSmForFMC();          
            }
        });
        
        
        // Add all the buttons to the "main data_sm tester JPanel".
        jPanel.add(textMailJButton);
        jPanel.add(incomingCallNotifiationJButton);
        jPanel.add(messageNotificationJButton);
        jPanel.add(readNotificationJButton);
        jPanel.add(fmcJButton);
        return jPanel;
    }

    /**
     *Responsible for creating a JPanel with all of the components required to run the charset tests.
     * @return A JPanel with all of the swing components required to run the charset tests.
     */
    private JPanel initializeJPanelForCharsetTests() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(3, 1));
        jPanel.setBorder(BorderFactory.createTitledBorder("Charset Tests"));

        // Initialise button for sending data_sm which uses the en.chpr template for the message_payload.
        JButton enDotCphrJButton = new JButton("Send data_sm using the en.cphr");
        enDotCphrJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                charsetTester.sendDataSmUsingEnDotCphr();
            }
        });

        // Initialise button for sending data_sm which uses the ja.chpr template for the message_payload.
        JButton jaDoCphrJButton = new JButton("Send data_sm using the ja.cphr");
        jaDoCphrJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                charsetTester.sendDataSmUsingJaDotCphr();
            }
        });

        // Initialise button for sending data_sm which uses the ja-c-shiftjis.chpr template for the message_payload.
        JButton shiftJisJButton = new JButton("Send data_sm using the ja-c-shiftjis.cphr");
        shiftJisJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                charsetTester.sendDataSmUsingShiftJisCharset();
            }
        });

        // Add all the buttons to the "main charset_tester JPanel".
        jPanel.add(enDotCphrJButton);
        jPanel.add(jaDoCphrJButton);
        jPanel.add(shiftJisJButton);
        return jPanel;
    }

    /**
     *Responsible for creating a JPanel with all of the components required to run the binding tests.
     * @return A JPanel with all of the swing components required to run the binding tests.
     */
    private JPanel initializeJpanelForBindingTests() {
        Map<String, Map<String, String>> shortMessageInstances = Config
                .getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        Iterator<String> instanceItr = null;
        String[] connectionsAllowed = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder("Binding Tests"));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        // Initialise button for binding to all SMSCs.
        JButton bindJButton = new JButton("Bind to all SMSCs");
        bindJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.bindToAllSMSCs();
            }
        });

        // Initialise button for unbinding from all SMSCs.
        JButton unbindJButton = new JButton("Unbind from all SMSCs");
        unbindJButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.unbindFromAllSMSCs();
            }
        });

        // Initialise JPanel for binding to a selected SMSC.
        JPanel jPanelBindingToSingleSMSC = new JPanel();
        jPanelBindingToSingleSMSC.setLayout(new GridLayout(1, 2));
        final JComboBox jComboBoxBindDropDown = new JComboBox();
        instanceItr = shortMessageInstances.keySet().iterator();
        while (instanceItr.hasNext()) {
            String instanceName = instanceItr.next();
            jComboBoxBindDropDown.addItem(instanceName);
        }
        JButton jButtonBindtoSingleSMSC = new JButton("Bind");
        jButtonBindtoSingleSMSC.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.bindToSMSC((String) jComboBoxBindDropDown.getSelectedItem());
            }
        });
        jPanelBindingToSingleSMSC.add(jComboBoxBindDropDown, gridBagConstraints);
        jPanelBindingToSingleSMSC.add(jButtonBindtoSingleSMSC, gridBagConstraints);

        // Initialise JPanel for unbinding from a selected SMSC.
        JPanel jPanelUnbindingFromSingleSMSC = new JPanel();
        jPanelUnbindingFromSingleSMSC.setLayout(new GridLayout(1, 2));
        final JComboBox jComboBoxUnbindDropDown = new JComboBox();
        instanceItr = shortMessageInstances.keySet().iterator();
        while (instanceItr.hasNext()) {
            String instanceName = instanceItr.next();
            jComboBoxUnbindDropDown.addItem(instanceName);
        }
        JButton jButtonUnbindtoSingleSMSC = new JButton("Unbind");
        jButtonUnbindtoSingleSMSC.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.unbindFromSMSC((String) jComboBoxUnbindDropDown.getSelectedItem());
            }
        });
        jPanelUnbindingFromSingleSMSC.add(jComboBoxUnbindDropDown, gridBagConstraints);
        jPanelUnbindingFromSingleSMSC.add(jButtonUnbindtoSingleSMSC, gridBagConstraints);

        // Initialise JPanel for setting the minimum and maximum transmitter connections.
        JPanel jPanelMinAndMaxTransmitterConnections = new JPanel();
        jPanelMinAndMaxTransmitterConnections.setBorder(BorderFactory.createTitledBorder("Transmitter Connections"));
        jPanelMinAndMaxTransmitterConnections.setLayout(new GridLayout(1, 4));
        final JComboBox jComboBoxMinTransmitterConnections = new JComboBox();
        final JComboBox jComboBoxMaxTransmitterConnections = new JComboBox();
        for (String noOfConnections : connectionsAllowed) {
            jComboBoxMinTransmitterConnections.addItem(noOfConnections);
            jComboBoxMaxTransmitterConnections.addItem(noOfConnections);
        }
        JButton jButtonSetMinTransmitterConnections = new JButton("Set Min");
        jButtonSetMinTransmitterConnections.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.setMinTransmitterConnections((String) jComboBoxMinTransmitterConnections.getSelectedItem());
            }
        });
        JButton jButtonSetMaxTransmitterConnections = new JButton("Set Max");
        jComboBoxMaxTransmitterConnections.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.setMaxTransmitterConnections((String) jComboBoxMaxTransmitterConnections.getSelectedItem());

            }
        });
        jPanelMinAndMaxTransmitterConnections.add(jComboBoxMinTransmitterConnections, gridBagConstraints);
        jPanelMinAndMaxTransmitterConnections.add(jButtonSetMinTransmitterConnections, gridBagConstraints);
        jPanelMinAndMaxTransmitterConnections.add(jComboBoxMaxTransmitterConnections, gridBagConstraints);
        jPanelMinAndMaxTransmitterConnections.add(jButtonSetMaxTransmitterConnections, gridBagConstraints);

        // Initialise JPanel for setting the minimum and maximum receiver connections.
        JPanel jPanelMinAndMaxReceiverConnections = new JPanel();
        jPanelMinAndMaxReceiverConnections.setBorder(BorderFactory.createTitledBorder("Receiver Connections"));
        jPanelMinAndMaxReceiverConnections.setLayout(new GridLayout(1, 4));
        final JComboBox jComboBoxMinReceiverConnections = new JComboBox();
        final JComboBox jComboBoxMaxReceiverConnections = new JComboBox();
        for (String noOfConnections : connectionsAllowed) {
            jComboBoxMinReceiverConnections.addItem(noOfConnections);
            jComboBoxMaxReceiverConnections.addItem(noOfConnections);
        }
        JButton jButtonSetMinReceiverConnections = new JButton("Set Min");
        jButtonSetMinReceiverConnections.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.setMinReceiverConnections((String) jComboBoxMinReceiverConnections.getSelectedItem());
            }
        });
        JButton jButtonSetMaxReceiverConnections = new JButton("Set Max");
        jButtonSetMaxReceiverConnections.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                bindingTester.setMaxReceiverConnections((String) jComboBoxMaxReceiverConnections.getSelectedItem());
            }
        });
        jPanelMinAndMaxReceiverConnections.add(jComboBoxMinReceiverConnections, gridBagConstraints);
        jPanelMinAndMaxReceiverConnections.add(jButtonSetMinReceiverConnections, gridBagConstraints);
        jPanelMinAndMaxReceiverConnections.add(jComboBoxMaxReceiverConnections, gridBagConstraints);
        jPanelMinAndMaxReceiverConnections.add(jButtonSetMaxReceiverConnections, gridBagConstraints);

        // Add all the sub JPanels to the "main binding tester JPanel".
        gridBagConstraints.gridy = 0;
        jPanel.add(bindJButton, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        jPanel.add(unbindJButton, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        jPanel.add(jPanelBindingToSingleSMSC, gridBagConstraints);
        gridBagConstraints.gridy = 3;
        jPanel.add(jPanelUnbindingFromSingleSMSC, gridBagConstraints);
        gridBagConstraints.gridy = 4;
        jPanel.add(jPanelMinAndMaxTransmitterConnections, gridBagConstraints);
        gridBagConstraints.gridy = 5;
        jPanel.add(jPanelMinAndMaxReceiverConnections, gridBagConstraints);
        return jPanel;
    }
}
