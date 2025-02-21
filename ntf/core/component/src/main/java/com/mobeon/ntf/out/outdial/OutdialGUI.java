/*
 * OutdialGUI.java
 *
 * Created on den 12 maj 2005, 14:39
 */

package com.mobeon.ntf.out.outdial;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.*;

import com.mobeon.common.commands.*;

/**
 *
 * @author  MNIFY
 */
public class OutdialGUI extends JFrame{
    
    private DisplayCanvas canvas;
    private ArrayList<State> states;
    private ArrayList<Transition> transitions;
    
    private DisplayObject selected = null;
    
    private Point previousDragPoint = null;
    private Point addPoint = null;
    private Point toArrowPoint = null;
    
    private int dragButton = 0;
    
    private static Color stateFillColor = new Color(255, 202, 202);
    private static Color transitionFillColor = new Color(202, 255, 202);
    
    private static Color stateSelectedFillColor = new Color(130, 202, 202);
    private static Color transitionSelectedFillColor = new Color(202, 130, 202);
    
    private JPopupMenu addMenu = new JPopupMenu();
    
    /** Creates a new instance of OutdialGUI */
    public OutdialGUI() {
        initComponents();
        //initStatesAndTransistions();
        states = new ArrayList<State>();
        transitions = new ArrayList<Transition>();
        setSize(1000,500);
        canvas.setSize(1000, 350);
        setVisible(true);
        loadFromFile();
        canvas.repaint();
    }
    
    private DisplayObject findAndSelectObject( Point p, boolean drag ) {
        if( drag && selected != null ) {
            return selected;
        } else if( drag ) {
            return null;
        }
        if( selected != null ) {
            selected.selected = false;
            selected = null;
        }
        DisplayObject object = findDisplayObject(p);
        if( object != null ) {
            selected = object;
            object.selected = true;
        }
        return object;
    }
    
    private DisplayObject findDisplayObject( Point p ) {
        for( int i=0;i<states.size();i++ ) {
            State s = states.get(i);
            if( s.within(p) ) {
                return s;
            }
        }
        for( int i=0;i<transitions.size();i++ ) {
            Transition t = transitions.get(i);
            if( t.within(p) ) {
                return t;
            }
        }
        return null;
    }
    
    private State findStateWithNumber( int number ) {
        for( int i=0;i<states.size();i++ ) {
            State s = states.get(i);
            if( s.number == number ) {
                return s;
            }
        }
        return null;
    }
    
   
    
    private void loadFromFile() {
        String outdialFile = "H:/outdial-default.cfg";
        /*JFileChooser fileChooser = new JFileChooser();
        int retCode = fileChooser.showOpenDialog(this);
        if( retCode == JFileChooser.APPROVE_OPTION ) {
            outdialFile = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        */
        try {
            BufferedInputStream bis =
                        new BufferedInputStream(new FileInputStream(outdialFile));
            Properties props = new Properties();
            props.load(bis);
            CommandHandler handler = new CommandHandler(props);
            
            int maxStates = handler.getNoStates();
            states = new ArrayList<State>(maxStates);
            int width = canvas.getSize().width;
            int stateDiff = width / maxStates;
            for( int i=0;i<maxStates;i++ ) {
                State s = new State();
                s.number = i;
                s.y = 20;
                s.x = 20 + i*stateDiff;
                states.add(s);
            }
            State defaultState = new State();
            defaultState.number = -2;
            defaultState.y = 220;
            defaultState.x = 300;
            defaultState.label = "default";
            states.add(defaultState);
            
            State endState = new State();
            endState.y = 220;
            endState.x = 700;
            endState.label = "End";
            endState.number = -1;
            states.add(endState);
            
                        
            com.mobeon.common.commands.State[] commandStates = handler.getStates();
            for( int i=0;i<commandStates.length+1;i++ ) {
                com.mobeon.common.commands.State s = null;
                State fromState = null;
                if( i >= commandStates.length ) {
                    s = handler.getDefaultState();
                    fromState = defaultState;
                } else {
                    s = commandStates[i];
                    fromState = findStateWithNumber(i);
                }
                if( fromState == null ) {
                    continue;
                }
                HashMap<Integer, Command> map = s.getTransitions();
                Iterator<Integer> iter = map.keySet().iterator();
                int transitionCount = 0;
                while( iter.hasNext() ) {
                    Integer code = iter.next();
                    Command cmd = map.get(code);
                    State nextState = findStateWithNumber(cmd.getNextState());
                    if( nextState == null ) {
                        continue;
                    }
                    String actions = "";
                    if( cmd != null ) {
                        Collection<Object> operations = cmd.getOperations();
                        Iterator<Object> opIter = operations.iterator();
                        while( opIter.hasNext() ) {
                            Operation op = (Operation) opIter.next();
                            actions += op.getOpname() + "; ";
                        }
                    }
                    Transition t = new Transition();
                    t.code = code.intValue();
                    t.actions = actions;
                    t.fromState = fromState;
                    t.toState = nextState;
                    t.x = (fromState.x + nextState.x) /2 - 20;
                    t.y = fromState.y + 40 + 25*transitionCount++;
                    transitions.add(t);
                }
            }
                    
        } catch(Exception e) {
            System.out.println("Failed to read file, " + e.toString() );
            e.printStackTrace();
        }
    }
    
    private void initComponents() {
        canvas = new DisplayCanvas();

        getContentPane().setLayout(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                System.exit(2);
            }
            
            
        });
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension d = getSize();
                canvas.setSize(d.width, d.height-150);
            }
        });

        canvas.setBackground(new java.awt.Color(255, 255, 255));
        canvas.addMouseListener(canvas);
        canvas.addMouseMotionListener(canvas);
        getContentPane().add(canvas);
        
        JMenuItem stateItem = new JMenuItem("Add state");
        stateItem.addActionListener(new addStateAction());
        addMenu.add(stateItem);
        JMenuItem transitionItem = new JMenuItem("Add transition");
        transitionItem.addActionListener(new addTransitionAction());
        addMenu.add(transitionItem);
        
        addMenu.pack();
        
        pack();
    }
    
   
    
    private class addStateAction implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            // add state
            State state = new State();
            state.x = addPoint.x;
            state.y = addPoint.y;
            state.number = states.size()+1;
            state.label = "" + state.number;
            states.add(state);
            System.out.println("Adding state");
        }
        
    }
    
     private class addTransitionAction implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            // add transition
            Transition t = new Transition();
            t.x = addPoint.x;
            t.y = addPoint.y;
            transitions.add(t);
            
            
            System.out.println("Adding transition");
        }
        
    }
    
    private class DisplayCanvas extends JPanel implements MouseListener, MouseMotionListener {
        Image offscreen;
        Dimension offscreensize;
        Graphics offgraphics;
        
        public void paint(Graphics g) {
            Dimension d = getSize();
            if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
                offscreen = createImage(d.width, d.height);
                offscreensize = d;
                if (offgraphics != null) {
                    offgraphics.dispose();
                }
                offgraphics = offscreen.getGraphics();
                Font font = new Font("Verdana", Font.BOLD, 12 );
                offgraphics.setFont(font);
                
                
            }
            offgraphics.setColor(Color.WHITE);
            offgraphics.fillRect(0,0, d.width, d.height);
            offgraphics.setColor(Color.BLACK);
            
            FontMetrics fontMetrics = offgraphics.getFontMetrics();
            for( int i=0;i<states.size();i++ ) {
                State state = states.get(i);
                state.draw(offgraphics, fontMetrics);
            }
            for(int i=0;i<transitions.size();i++ ) {
                Transition t = transitions.get(i);
                t.draw(offgraphics, fontMetrics);
            }
            
            if( selected != null && toArrowPoint != null ) {
                drawArrow(offgraphics, selected.fromConnectPoint.x, selected.fromConnectPoint.y, toArrowPoint.x, toArrowPoint.y, 12, 6 );
            }
            
            g.drawImage(offscreen, 0, 0, null);
        }
        
        public void mouseClicked(MouseEvent e) {
            Point pressedPoint = e.getPoint();
            System.out.println("Mouse pressed at " + pressedPoint );
            DisplayObject object = findAndSelectObject(pressedPoint, false);
            if( object == null && e.getButton() == MouseEvent.BUTTON3 ) {
                System.out.println("Add popup");
                addPoint = pressedPoint;
                //addMenu.setLocation(addPoint);
                addMenu.show(this, addPoint.x, addPoint.y);
            } else if( object != null && e.getButton() == MouseEvent.BUTTON3 ) {
               object.showPopup();
            } else {
                repaint();
            }
        }
        
        public void mouseEntered(MouseEvent e) {
        }
        
        public void mouseExited(MouseEvent e) {
        }
        
        public void mousePressed(MouseEvent e) {
            if( dragButton == MouseEvent.NOBUTTON ) {
                dragButton = e.getButton();
            }
            System.out.println("Button " + e.getButton()  + " pressedn ");
        }
        
        public void mouseReleased(MouseEvent e) {
            if( e.getButton() == MouseEvent.BUTTON3 && toArrowPoint != null ) {
                DisplayObject object = findDisplayObject(e.getPoint());
                if( object != null ) {
                    if( object instanceof Transition ) {
                        if( selected instanceof State ) {
                            // from state to transition.
                            ((Transition) object).fromState = (State) selected;
                        }
                    } else if( object instanceof State ) {
                        if( selected instanceof State ) {
                            // state to state
                            Transition t = new Transition();
                            t.x = (selected.x + object.x) / 2;
                            t.y = (selected.y + object.y) / 2;
                            t.toState = (State) object;
                            t.fromState = (State) selected;
                            transitions.add(t);
                        } else if( selected instanceof Transition ) {
                            // transition to state
                            ((Transition) selected).toState = (State)object;
                        }
                    }
                }
                
            }
            
            dragButton = 0;
            toArrowPoint = null;
            previousDragPoint = null;
            repaint();
        }
        
        public void mouseDragged(MouseEvent e) {
            Point draggedPoint = e.getPoint();
            System.out.println("Mouse dragged at " + draggedPoint);
            System.out.println("Button is " + e.getButton() );
            if( dragButton == MouseEvent.BUTTON1 ) {
                boolean drag = true;
                if( previousDragPoint == null ) {
                    drag = false;
                    previousDragPoint = draggedPoint;
                }
                
                DisplayObject object = findAndSelectObject(draggedPoint, drag);
                if( object != null ) {
                    System.out.println("Moving object");
                    object.x += draggedPoint.x - previousDragPoint.x;
                    object.y += draggedPoint.y - previousDragPoint.y;
                    
                }
            } else if ( dragButton == MouseEvent.BUTTON3 ) {
                System.out.println("Dragging arrow");
                if( toArrowPoint == null ) {
                    DisplayObject object = findAndSelectObject(draggedPoint, false);
                    if( object != null ) {
                        toArrowPoint = draggedPoint;
                    }
                } else if( selected != null ) {
                    toArrowPoint = draggedPoint;
                }  
            }
            repaint();
            previousDragPoint = draggedPoint;
        }
        
        public void mouseMoved(MouseEvent e) {
        }
        
    }
    
    private class DisplayObject {
        int x;
        int y;
        
        int height;
        int width;
        
        Point fromConnectPoint = new Point();
        Point toConnectPoint = new Point();
        
        public void calculateConnectionPoint() {
            fromConnectPoint.x = x+width;
            toConnectPoint.x = x;
            
            fromConnectPoint.y = y+height/2;
            toConnectPoint.y = y+height/2;
        }
        
        boolean selected = false;
        
        public void showPopup() {}
        
        public boolean within(Point p) {
            if( p.x >= x && p.x <= x+width &&
                p.y >= y && p.y <= y+height ) {
                    return true;
            } else {
                return false;
            }
        }
        
    }
    
    private class State extends DisplayObject {
                
        
        int number;
        String label = "";
        
        public void showPopup() {
            final JDialog dialog = new JDialog();
            dialog.getContentPane().setLayout(new GridLayout(3,1));
            
            final JTextField numberText = new JTextField("" + number, 20);
            JPanel numberPanel = new JPanel();
            numberPanel.setLayout(new FlowLayout(FlowLayout.RIGHT) );
            numberPanel.add(new JLabel("Number: "));
            numberPanel.add(numberText);
            
            final JTextField name = new JTextField(label, 20);
            JPanel namePanel = new JPanel();
            namePanel.setLayout(new FlowLayout(FlowLayout.RIGHT) );
            namePanel.add(new JLabel("Name: "));
            namePanel.add(name);
            
            JButton okButton = new JButton("Ok");
            okButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    label = name.getText();
                    try {
                        int nr = Integer.parseInt(numberText.getText());
                        number = nr;
                    } catch(Exception e) {}
                    dialog.dispose();
                    canvas.repaint();
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    dialog.dispose();
                    canvas.repaint();
                }
            });
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    delete();
                    dialog.dispose();
                    canvas.repaint();
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout() );
            buttonPanel.add(deleteButton);
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            
            dialog.getContentPane().add(numberPanel);
            dialog.getContentPane().add(namePanel);
            dialog.getContentPane().add(buttonPanel);
            dialog.setModal(true);
            dialog.setLocation(x, y);
            dialog.pack();
            dialog.show();
            
            
            //panel.show();
        }
        
        private void delete() {
            for( int i=0;i<transitions.size();i++ ) {
                Transition t = transitions.get(i);
                if( t.fromState == this ) {
                    t.fromState = null;
                }
                if( t.toState == this ) {
                    t.toState = null;
                }
            }
            states.remove(this);
        }
        
        public void draw(Graphics g, FontMetrics fm) {
            //label = "" + number;
            String text = "" + number;
            if( label.length() > 0 ) {
                text = label;
            }
            height = fm.getHeight() + 20;
            width = fm.stringWidth(text) +20;
            if( width < height ) {
                width = height;
            }
            calculateConnectionPoint();
            if( selected ) {
                g.setColor(stateSelectedFillColor);
            } else {
                g.setColor(stateFillColor);
            }
            g.fillOval(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, width, height);
            
            g.drawString(text, x+width/2 - fm.stringWidth(text)/2, y + height/2 + fm.getAscent()/2 );
           
        }
    }
    
    private class Transition extends DisplayObject {
        
        int code;
        String actions;
        
        State fromState;
        State toState;
        
        public void showPopup() {
            final JDialog dialog = new JDialog();
            dialog.getContentPane().setLayout(new GridLayout(3,1));
            
            final JTextField codeText = new JTextField("" + code, 20);
            JPanel codePanel = new JPanel();
            codePanel.setLayout(new FlowLayout(FlowLayout.RIGHT) );
            codePanel.add(new JLabel("Code: "));
            codePanel.add(codeText);
            
            final JTextField actionText = new JTextField(actions, 20);
            JPanel actionPanel = new JPanel();
            actionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT) );
            actionPanel.add(new JLabel("Actions: "));
            actionPanel.add(actionText);
            
            JButton okButton = new JButton("Ok");
            okButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    actions = actionText.getText();
                    try {
                        int nr = Integer.parseInt(codeText.getText());
                        code = nr;
                    } catch(Exception e) {}
                    dialog.dispose();
                    canvas.repaint();
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    dialog.dispose();
                    canvas.repaint();
                }
            });
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    delete();
                    dialog.dispose();
                    canvas.repaint();
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout() );
            buttonPanel.add(deleteButton);
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            
            dialog.getContentPane().add(codePanel);
            dialog.getContentPane().add(actionPanel);
            dialog.getContentPane().add(buttonPanel);
            dialog.setModal(true);
            dialog.setLocation(x, y);
            dialog.pack();
            dialog.show();
        
        }
        
        private void delete() {
            transitions.remove(this);
        }
        
        public void draw(Graphics g, FontMetrics fm) {
            String displayString = code + "/ " + actions;
            height = fm.getHeight() + 4;
            width = fm.stringWidth(displayString) +10;
            calculateConnectionPoint();
            if( selected ) {
                g.setColor(transitionSelectedFillColor);
            } else {
                g.setColor(transitionFillColor);
            }
            g.fillRect(x,y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x,y, width, height);
            g.drawString(displayString, x+5,  y+height/2 + fm.getAscent()/2 );
            
            if( toState != null ) {
                drawArrow(g, fromConnectPoint.x, fromConnectPoint.y, toState.toConnectPoint.x,
                    toState.toConnectPoint.y, 12,4);
            }
             if( fromState != null ) {   
                g.drawLine(toConnectPoint.x, toConnectPoint.y,
                    fromState.fromConnectPoint.x, fromState.fromConnectPoint.y );
                
            }
        }
    }
    
    private void drawArrow(Graphics g,int x1,int y1,int x2,int y2,int headLength,int headwidth){
        double theta;
        double theta2;
        int deltaX;
        int deltaY;
        int lengthdeltaX;
        int lengthdeltaY;
        int widthdeltaX;
        int widthdeltaY;
        
        deltaX=(x2-x1);
        deltaY=(y2-y1);
        theta=Math.atan((double)(deltaY)/(double)(deltaX));
        if(deltaX<0.0) {
            theta2=theta+Math.PI;
        }
        else {
            theta2=theta;
        }
        lengthdeltaX =-(int)(Math.cos(theta2)*headLength);
        lengthdeltaY =-(int)(Math.sin(theta2)*headLength);
        widthdeltaX =(int)(Math.sin(theta2)*headwidth);
        widthdeltaY =(int)(Math.cos(theta2)*headwidth);
        
        g.drawLine(x1,y1,x2,y2);
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
      
      xPoints[0] = x2;
      yPoints[0] = y2;
      xPoints[1] = x2 + lengthdeltaX + widthdeltaX;
      yPoints[1] = y2 + lengthdeltaY - widthdeltaY;
      xPoints[2] = x2 + lengthdeltaX - widthdeltaX;
      yPoints[2] = y2 + lengthdeltaY + widthdeltaY;
      
      g.fillPolygon(xPoints, yPoints, 3);
      //g.drawLine(x2,y2,x2+lengthdeltaX+widthdeltaX,y2+lengthdeltaY-widthdeltaY);
      //g.drawLine(x2,y2,x2+lengthdeltaX-widthdeltaX,y2+lengthdeltaY+widthdeltaY);
    }
  
    public static void main(String[] args) {
        new OutdialGUI().show();
    }
}
