package gui;
import java.awt.Color;
import java.awt.event.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.text.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public final class ChatWindow extends JInternalFrame
{

	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

	private JScrollPane paneOutput;
	private JScrollPane paneInput;

	private JTextPane txtOutput;
	private JTextArea txtInput;
	private MainWindow mw;
	private String chatWith;

	public ChatWindow(String title,MainWindow mw)
	{
		this.mw = mw;
		chatWith = title;
		setTitle("Chat with: "+title);
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

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);

		txtInput.addKeyListener(keyListener);
		txtInput.grabFocus();

		DefaultCaret caret = (DefaultCaret)txtOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		addInternalFrameListener(new InternalFrameAdapter(){
			public void internalFrameClosing(InternalFrameEvent e) {
				mw.closeChat(chatWith);
			}
		});


	}

	private void append(String msg, Color c)
	{
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
		append("["+sdf.format(timestamp)+"] "+sender+": "+message ,c);

	}


	KeyListener keyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent e)  {
			// event when someone press enter
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				// Shift + Enter = new Line
				if (e.isShiftDown()) {
					txtInput.append("\n");
					return;
				}
				// Cancel empty message
				if (txtInput.getText().trim().equals("")) {
					txtInput.setText("");
					return;
				}

				String text = txtInput.getText();
				//addMessage(user,text,Color.black);
				mw.sendPm(chatWith,text);
				// Reset the input text area.
				txtInput.setText("");
			}
		}
	};


}
