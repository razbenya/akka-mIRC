package gui;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.*;

public final class ChannelWindow extends JInternalFrame  {


    private static final long serialVersionUID = 1L;
    private JScrollPane paneMemberList;
    private JScrollPane paneOutput;
    private JScrollPane paneInput;
    private JList<String> memberList;
    private JTextPane txtOutput;
    private JTextArea txtInput;
    private DefaultListModel<String> model;
    private MainWindow mw;
    private String _name;


    private int leaveFlag = 1;

    public ChannelWindow(String title, MainWindow mw) {

        this.mw = mw;
        setTitle(title);

        setLayout(null);

        _name = title;

        model = new DefaultListModel<String>();
        memberList = new JList<String>(model);
        memberList.setCellRenderer(new ListRenderer());
        paneMemberList = new JScrollPane(memberList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(paneMemberList);
        paneMemberList.setBounds(465, 10, 130, 310);

        txtOutput = new JTextPane();
        paneOutput = new JScrollPane(txtOutput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(paneOutput);
        paneOutput.setBounds(10, 10, 450, 310);

        txtInput = new JTextArea();
        paneInput = new JScrollPane(txtInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(paneInput);
        paneInput.setBounds(10, 325, 585, 40);


        txtOutput.setEditable(false);
        setSize(610, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        txtInput.addKeyListener(keyListener);
        memberList.addMouseListener(doubleClick);

        DefaultCaret caret = (DefaultCaret)txtOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        txtInput.grabFocus();

        append("now talking on: "+_name+"\n" , Color.orange);
        //setVisible(true);
        show();


        addInternalFrameListener(new InternalFrameAdapter(){
            public void internalFrameClosing(InternalFrameEvent e) {
                mw.removeChannelWindow(_name);
                if(leaveFlag == 1)
                    mw.leaveChannel(_name);
            }

        });



    }

    public void changeLeaveFlag(){
        leaveFlag = 0;
    }


    private void append(String msg, Color c)  {

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        int len = txtOutput.getDocument().getLength();
        StyledDocument doc = txtOutput.getStyledDocument();
        try {
            doc.insertString(len, msg, aset);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    //adding member to the list
    public void addMember(String memberName) {
        if(!model.contains(memberName)){
            SwingUtilities.invokeLater(() -> {
                        model.addElement(memberName);
                    }
            );
        }
    }

    //removing user from the list
    public void removeMember(String memberName) {
        model.removeElement(memberName);
        model.removeElement("@@"+memberName);
        model.removeElement("@"+memberName);
        model.removeElement("+"+memberName);

    }

    //adding message to the chat window
    public void addMessage(String message,Color c) {
        append(message,c);
    }


    KeyListener keyListener = new KeyAdapter()
    {
        public void keyReleased(KeyEvent e)
        {

            // event when someone press enter
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
            {
                // Shift + Enter = new Line
                if (e.isShiftDown())
                {
                    txtInput.append("\n");
                    return;
                }

                // Cancel empty message
                if (txtInput.getText().trim().equals(""))
                {
                    txtInput.setText("");
                    return;
                }
                String message = txtInput.getText();

                if(!message.startsWith("/")){
                    mw.sendChannelMessage(_name,message);
                }
                else if(message.startsWith("/leave")) {
                    mw.closeChannelWindow(_name);
                }
                else if(message.startsWith("/title ")){
                    String _title = message.substring(7).trim();
                    if(!title.equals(""))
                        mw.sendTitleMessage(_name,_title);
                }
                else if(message.startsWith("/kick ")){
                    if(message.length() > 6){
                        String userName = message.split(" ")[1].trim();
                        mw.sendKickMessage(userName,_name);
                    }
                }
                else if(message.startsWith("/ban ")){
                    if(message.length() > 5){
                        String userName = message.split(" ")[1].trim();
                        mw.sendBanMessage(userName,_name);
                    }
                }
                else if(message.startsWith("/add ")){
                    String[] splited = message.split(" ");
                    if(splited.length >= 3){
                        String mod = splited[1].trim();
                        String userName = splited[2].trim();
                        mw.promoteUser(_name, mod, userName);
                    }
                }
                else if(message.startsWith("/remove ")){
                    String[] splited = message.split(" ");
                    if(splited.length >= 3){
                        String mod = splited[1].trim();
                        String userName = splited[2].trim();
                        mw.demoteUser(_name, mod, userName);
                    }
                }
                else if(message.startsWith("/disband")){
                    mw.disbandChannel(_name);
                }
                else if(message.startsWith("/help")){
                    append("Supported command list: \n",Color.BLUE);
                    append("/help - reciving list of supported commands\n",Color.BLACK);
                    append("double click on <userName> - start a private chat with <userName> \n",Color.BLACK);
                    append("/leave - leave channel \n",Color.BLACK);
                    append("VOICED Command: \n",Color.BLUE);
                    append("/title <title> - changing the title to <title>\n",Color.BLACK);
                    append("OPERATOR Commands: \n",Color.BLUE);
                    append("/kick <userName> - kick <userName> from <channelName>\n",Color.BLACK);
                    append("/ban <userName> - ban <userName> from <channelName> \n",Color.BLACK);
                    append("/add v <userName> - prpmoting <userName> to VOICED\n",Color.BLACK);
                    append("/add op <userName> - promoting <userName> to OPERATOR\n",Color.BLACK);
                    append("/remove v <userName> - demoting <userName> to REGULAR\n",Color.BLACK);
                    append("/remove op <userName> - demoting <userName> to VOICED(from op)\n",Color.BLACK);
                    append("OWNER Commands: \n",Color.BLUE);
                    append("/disband - kicking all user from the channel and delete the channel \n",Color.BLACK);
                }
                else{
                    append("unsupported command ! \n",Color.red);
                    append("send /help for command list \n",Color.black);
                }

                // Reset the input text area.
                txtInput.setText("");

            }
        }
    };



    MouseListener doubleClick = new MouseAdapter() {
        public void mouseClicked(MouseEvent evt) {
            JList<?> list = (JList<?>)evt.getSource();
            if (evt.getClickCount() == 2) {
                int index = list.locationToIndex(evt.getPoint());
                if(index<0)
                    return;
                String name = model.getElementAt(index);
                if(name.startsWith("@@")){
                    name = name.substring(2);
                }
                if(name.startsWith("@") || name.startsWith("+")){
                    name = name.substring(1);
                }
                mw.sendWhisper(name,"");
                //parant.openChatWindow("chat with: "+name,true);
            }
        }
    };
}
