package gui;

import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import msg.LoginMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class LoginWindow extends JFrame{

	private static final long serialVersionUID = 2L;
	private static final String VALID = "-_.*$+:@&=,!~';.ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private HashMap<Character,Boolean> validMap;
	private JTextField nickInput;
	private JButton enter;
	private JScrollPane paneNick;
	private JLabel label;
	private ActorContext _context;
	private ActorRef _local;
	//private JFrame parent;

	public LoginWindow(ActorContext context,ActorRef local){
		_context = context;
		_local = local;
		//super("LogIn");
		setTitle("login");
		//this.parent = parent;
		label = new JLabel("NickName:");
		setAlwaysOnTop(true);
		//setTitle("LogIn");

		setLayout(null);
		setLocationRelativeTo(null);
		enter = new JButton("Enter");
		nickInput = new JTextField();
		setResizable(false);
		setSize(285,150);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		paneNick = new JScrollPane(nickInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		paneNick.setBounds(72,20, 200, 35);
		label.setBounds(7, 18, 100, 35);
		enter.setBounds(90,65, 100, 30);
		add(paneNick);
		add(enter);
		add(label);

		enter.addActionListener(buttonClick);
		getRootPane().setDefaultButton(enter);
		initValidMap();
		setVisible(true);
	}

	private void initValidMap(){
		validMap = new HashMap<>();
		for(int i=0; i<VALID.length(); i++){
			validMap.put(VALID.charAt(i),true);
		}
	}



	public boolean valid(String name) {
		if (name.startsWith("@") || name.startsWith("+"))
			return false;
		for(int i = 0; i < name.length(); i++){
			if(!validMap.containsKey(name.charAt(i)))
				return false;
		}
		return true;
	}

	private void send(){
		String nick = nickInput.getText();
		edButton(false);
		nickInput.setText("");
		if(!nick.equals("") && valid(nick)){
			//_handler.tell(new LoginMessage(nick),_local);
			ActorSelection sel = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler/"+nick);
			Timeout t = new Timeout(5, TimeUnit.SECONDS);
			AskableActorSelection asker = new AskableActorSelection(sel);
			Future<Object> fut = asker.ask(new Identify(1), t);
			ActorIdentity ident = null;
			try {
				ident = (ActorIdentity) Await.result(fut, t.duration());
			} catch (Exception e) { e.printStackTrace(); }
			if(ident.ref().isEmpty()) {
				ActorSelection handler = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler");
				handler.tell(new LoginMessage(nick),_local);
			}
			else{
				setTitle("choose different nickname");
				edButton(true);
			}
		}else{
			setTitle("choose different nickname");
			edButton(true);
		}
	}

	public void edButton(boolean enable){
		enter.setEnabled(enable);
	}


	ActionListener buttonClick = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			send();
		}
	};

	public void close(){
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}



}
