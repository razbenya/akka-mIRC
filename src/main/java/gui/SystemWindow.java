package gui;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.*;

public final class SystemWindow extends JInternalFrame {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    private JScrollPane paneOutput;
    private JScrollPane paneInput;

    private JTextPane txtOutput;
    private JTextArea txtInput;
    private MainWindow mw;

    public SystemWindow(String title,MainWindow mw) {
        this.mw = mw;
        setTitle(title);
        setLayout(null);

        txtOutput = new JTextPane();
        txtInput = new JTextArea();


        paneOutput = new JScrollPane(txtOutput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paneInput = new JScrollPane(txtInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(paneOutput);
        add(paneInput);

        paneOutput.setBounds(10, 10, 585, 310);
        paneInput.setBounds(10, 325, 585, 40);

        txtOutput.setEditable(false);

        setSize(610, 400);
        setResizable(false);
        //setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

        DefaultCaret caret = (DefaultCaret)txtOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        //addWindowListener(winListener);
        txtInput.addKeyListener(keyListener);
        txtInput.grabFocus();
        append("Hello "+mw.getTitle()+" Welcome to the best mirc app ever !\n", Color.RED);

    }

    public void append(String msg, Color c)  {

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


    //adding message to the chat window
    public void addMessage(String sender, String message,Color c)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        append("["+sdf.format(timestamp)+"] "+sender+" "+message ,c);

    }


    KeyListener keyListener = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            // event when someone press enter
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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

                String text = txtInput.getText();
                txtInput.setText("");
                if(text.startsWith("/w ")){
                    String user = text.split(" ")[1].trim();
                    String message = "";
                    if(text.length()> user.length()+4)
                        message = text.substring(4+user.length());
                    append("Starting chat with: "+user+"\n",Color.orange);
                    mw.sendWhisper(user,message);
                }
                else if(text.startsWith("/channels")){
                    append("loading channels list, please wait..\n" ,Color.BLUE);
                    mw.clearChannelList();
                    mw.getChannelList();
                }
                else if(text.startsWith("/join ")){
                    if(text.length() > 6){
                        String channelName = text.substring(6).trim();

                        mw.joinChannel(channelName);
                    }
                }
                else if(text.startsWith("/leave ")){
                    if(text.length() > 7){
                        String channelName = text.substring(7).trim();
                        mw.closeChannelWindow(channelName);
                    }
                }
                else if(text.startsWith("/title ")){
                    if(text.length() > 7){
                        String channelName = text.substring(7).trim().split(" ")[0];
                        String title = text.substring(8 + channelName.length());
                        mw.sendTitleMessage(channelName,title);
                    }
                }
                else if(text.startsWith("/kick ")){
                    String[] splited = text.split(" ");
                    if(splited.length >= 3){
                        String userName = splited[1].trim();
                        String channel = splited[2].trim();
                        mw.sendKickMessage(userName, channel);
                    }
                }
                else if(text.startsWith("/ban ")){
                    String[] splited = text.split(" ");
                    if(splited.length >= 3){
                        String userName = splited[1].trim();
                        String channel = splited[2].trim();
                        mw.sendBanMessage(userName, channel);
                    }
                }
                else if(text.startsWith("/add ")){
                    String[] splited = text.split(" ");
                    if(splited.length >= 4){
                        String channel = splited[1].trim();
                        String mod = splited[2].trim();
                        String userName = splited[3].trim();
                        mw.promoteUser(channel, mod, userName);
                    }
                }
                else if(text.startsWith("/remove ")){
                    String[] splited = text.split(" ");
                    if(splited.length >= 4){
                        String channel = splited[1].trim();
                        String mod = splited[2].trim();
                        String userName = splited[3].trim();
                        mw.demoteUser(channel, mod, userName);
                    }
                }
                else if(text.startsWith("/disband ")){
                    if(text.length() > 9){
                        String channelName = text.substring(9).trim();
                        mw.disbandChannel(channelName);
                    }
                }
                else if(text.startsWith("/help")){
                    append("Supported command list: \n",Color.BLUE);
                    append("/help - reciving list of supported commands\n",Color.BLACK);
                    append("/w <userName> <Message> - send a private message to <userName> \n",Color.BLACK);
                    append("/channels -  load channel list \n",Color.BLACK);
                    append("/join <channelName> - join <channelName> \n",Color.BLACK);
                    append("/leave <channelName> - leave <channelName> \n",Color.BLACK);
                    append("VOICED Command: \n",Color.BLUE);
                    append("/title <channelName> <title> - changing <channelName> title to <title>\n",Color.BLACK);
                    append("OPERATOR Commands: \n",Color.BLUE);
                    append("/kick <userName> <channelName>  - kick <userName> from <channelName>\n",Color.BLACK);
                    append("/ban <userName> <channelName> - ban <userName> from <channelName> \n",Color.BLACK);
                    append("/add <channelName> v <userName> - prpmoting <userName> to VOICED\n",Color.BLACK);
                    append("/add <channelName> op <userName> - promoting <userName> to OPERATOR\n",Color.BLACK);
                    append("/remove <channelName> v <userName> - demoting <userName> to REGULAR\n",Color.BLACK);
                    append("/remove <channelName> op <userName> - demoting <userName> to VOICED(from op)\n",Color.BLACK);
                    append("OWNER Commands: \n",Color.BLUE);
                    append("/disband <channelName> - kicking all user from the channel and delete the channel \n",Color.BLACK);
                }
                else{
                    append("unsupported command ! \n",Color.red);
                    append("send /help for command list \n",Color.black);
                }
            }
        }
    };
}
