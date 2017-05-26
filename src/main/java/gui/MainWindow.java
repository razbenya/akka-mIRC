package gui;

import actors.LocalUser;
import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import msg.*;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.awt.Color;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class MainWindow extends JFrame{
	private static final String VALID = "-_.*$+:@&=,!~';.ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private HashMap<Character,Boolean> validMap;
	private static final long serialVersionUID = 9015345335719534824L;
	private JDesktopPane desktop;
	private JScrollPane paneChanelsList;
	private JList<String> chanelsList;
	private DefaultListModel<String> chanels;
	private HashMap<String,ChatWindow>  usersChat;
	private ActorSelection handler;
	private ActorRef remote;
	private HashMap<String,ChannelWindow> channelWindows;
	private ActorContext _context;
	private HashMap<String, LocalUser.Modes> _modesInChannel;
	private SystemWindow sys;


	public MainWindow(HashMap<String, LocalUser.Modes> modesInChannel, String title, ActorRef remote, ActorContext context){
		super(title);
		_modesInChannel = modesInChannel;
		setLayout(null);
		_context = context;
		handler = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler");
		this.remote = remote;
		channelWindows = new HashMap<>();
		usersChat = new HashMap<>();
		chanels = new DefaultListModel<>();
		chanelsList = new JList<>(chanels);
		desktop = new JDesktopPane();
		setResizable(false);
		paneChanelsList = new JScrollPane(chanelsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(desktop);
		add(paneChanelsList);
		chanelsList.addMouseListener(doubleClick);
		setSize(1000,650);
		paneChanelsList.setBounds(5,5,190,610);
		paneChanelsList.setBackground(Color.BLUE);
		desktop.setBounds(200,5,785,610);
		desktop.setBackground(Color.LIGHT_GRAY);
		openSystemWindow();
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(winListener);
		setLocationRelativeTo(null);
		initValidMap();
	}

	private void initValidMap(){
		validMap = new HashMap<>();
		for(int i=0; i<VALID.length(); i++){
			validMap.put(VALID.charAt(i),true);
		}
	}


	public void openSystemWindow(){
		sys = new SystemWindow("System",this);
		sys.setVisible(true);
		desktop.add(sys);
		try {
			sys.setSelected(true);
			sys.setClosable(false);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}

	//only call when window chat is open !
	public void sendPm(String user,String text){
		usersChat.get(user).addMessage(getTitle(),text,Color.black);
		//handler.tell(new PrivateMessage(text,user),remote);
		ActorSelection contact = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler/"+user);
		contact.tell(new PrivateMessage(text),remote);
	}


	MouseListener doubleClick = new MouseAdapter() {
		public void mouseClicked(MouseEvent evt) {
			JList<?> list = (JList<?>)evt.getSource();
			if (evt.getClickCount() == 2) {
				int index = list.locationToIndex(evt.getPoint());
				if(index<0)
					return;
				String name = chanels.getElementAt(index);
				joinChannel(name);
			}
		}
	};

	public void addChanel(String chanelName){
		chanels.addElement(chanelName);
	}


	public void closeChat(String user) {
		usersChat.remove(user);
	}

	public void reciveWhisper(String user,String message) {
		if(usersChat.get(user) == null) {
			ChatWindow window = new ChatWindow(user, this);
			window.setVisible(true);
			desktop.add(window);
			try {
				window.setSelected(true);
				window.setClosable(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			usersChat.put(user,window);
		}

		usersChat.get(user).addMessage(user,message,Color.black);
	}

	public void sendWhisper(String user,String text){
		if(usersChat.get(user) == null) {
			ChatWindow window = new ChatWindow(user, this);
			window.setVisible(true);
			desktop.add(window);
			try {
				window.setSelected(true);
				window.setClosable(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			usersChat.put(user,window);
		}
		if(!text.equals("")){
			sendPm(user,text);
		}
	}

	public void getChannelList() {
		handler.tell(msg.Messages.CHAN,remote);
	}

	private void openChannelWindow(String channelName){
		ChannelWindow window = new ChannelWindow(channelName,this);
		channelWindows.put(channelName,window);
		desktop.add(window);
		try {
			window.setSelected(true);
			window.setClosable(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}


	public void joinChannel(String channelName) {
		if(!valid(channelName)) {
			sys.append("oops channel name contain illegal character\n", Color.red);
			return;
		}
		if(!_modesInChannel.containsKey(channelName))
			_modesInChannel.put(channelName, LocalUser.Modes.REGULAR);

		sys.append("trying to join "+channelName+", please wait..\n", Color.ORANGE);

		if(_modesInChannel.get(channelName).ordinal() == LocalUser.Modes.BANNED.ordinal()){
			sys.append("You are banned from this channel !\n",Color.RED);
			return;
		}
		if(!channelWindows.containsKey(channelName)) {
			openChannelWindow(channelName);
			ActorSelection sel = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/"+channelName);
			Timeout t = new Timeout(5, TimeUnit.SECONDS);
			AskableActorSelection asker = new AskableActorSelection(sel);
			Future<Object> fut = asker.ask(new Identify(1), t);
			ActorIdentity ident = null;
			try {
				ident = (ActorIdentity) Await.result(fut, t.duration());
			} catch (Exception e) { e.printStackTrace(); }
			if(ident.ref().isEmpty()) {
				_modesInChannel.put(channelName, LocalUser.Modes.OWNER);
				ActorSelection handler = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler");
				handler.tell(new JoinMessage(channelName),remote);
			}
			else{
				_modesInChannel.put(channelName, LocalUser.Modes.REGULAR);
				sel.tell(new JoinMessage(channelName),remote);
			}
		}
	}


	WindowListener winListener = new WindowAdapter()
	{
		public void windowClosing(WindowEvent e)
		{
			_context.system().terminate();
		}
	};

	public void clearChannelList() {
		chanels.clear();
	}

	public void addUser(String channelName, String user) {
		if(channelWindows.get(channelName)!=null)
			channelWindows.get(channelName).addMember(user);
	}

	public void addChanelMessase(String channel, String message, Color c) {
		if(channelWindows.get(channel)!=null)
			channelWindows.get(channel).addMessage(message, c);
	}

	public void sendChannelMessage(String name, String message) {
		ActorSelection channel = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/"+name);
		channel.tell(new ChannelMessage(message,name),remote);
	}

	public void leaveChannel(String name) {
		ActorSelection channel = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/"+name);
		channel.tell(Messages.LEAVE,remote);
		sys.append("leaving "+name+"..\n", Color.GREEN);
	}

	public void closeChannelWindow(String channelName){
		if(channelWindows.get(channelName) != null){
			try {
				channelWindows.get(channelName).setClosed(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeChannelWindow(String name) {
		channelWindows.remove(name);
	}

	public void removeUser(String user, String channelName) {
		if(channelWindows.get(channelName) != null){
			channelWindows.get(channelName).removeMember(user);
		}
	}

	public void setChannelTitle(String channelName, String title) {
		if(channelWindows.get(channelName) != null){
			channelWindows.get(channelName).setTitle(channelName+" - "+title);
			channelWindows.get(channelName).addMessage("Channel topic changed to: "+title +"\n",Color.orange);
		}
	}

	public void sendTitleMessage(String channelName, String title) {
		if(_modesInChannel.get(channelName) != null
				&& _modesInChannel.get(channelName).ordinal() >= LocalUser.Modes.VOICED.ordinal()) {
			ActorSelection channel = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/" + channelName);
			channel.tell(new TitleMessage(title), remote);
		}
	}

	public void sendKickMessage(String userName, String channel) {
		if(channelWindows.containsKey(channel)) {
			if (_modesInChannel.get(channel) != null
					&& _modesInChannel.get(channel).ordinal() >= LocalUser.Modes.OPERATOR.ordinal()) {
				ActorSelection user = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler/" + userName);
				user.tell(new KickMessage(channel), remote);
			}
		}
	}

	public void gotKicked(String channel, String kickedBy) {
		if(channelWindows.get(channel) != null){
			channelWindows.get(channel).changeLeaveFlag();
			ActorSelection channelActor = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/" + channel);
			channelActor.tell(new KickedMessage(kickedBy, channel), remote);
			closeChannelWindow(channel);
			sys.append("You got kicked from "+channel+" by "+kickedBy+"\n",Color.RED);
		}
	}

	public void sendBanMessage(String userName, String channel) {
		if(channelWindows.containsKey(channel)) {
			if (_modesInChannel.get(channel) != null
					&& _modesInChannel.get(channel).ordinal() >= LocalUser.Modes.OPERATOR.ordinal()) {
				ActorSelection user = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler/" + userName);
				user.tell(new BanMessage(channel), remote);

			}
		}
	}

	public void gotBanned(String channel, String bannedBy) {
		_modesInChannel.put(channel, LocalUser.Modes.BANNED);
		if(channelWindows.get(channel) != null){
			channelWindows.get(channel).changeLeaveFlag();
			ActorSelection channelActor = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/" + channel);
			channelActor.tell(new BannedMessage(bannedBy, channel), remote);
			closeChannelWindow(channel);
			sys.append("You got banned from "+channel+" by "+bannedBy+"\n",Color.RED);
		}
	}

	public void promoteUser(String channel, String mod, String userName) {
		if(channelWindows.containsKey(channel)) {
			if (_modesInChannel.get(channel) != null
					&& _modesInChannel.get(channel).ordinal() >= LocalUser.Modes.OPERATOR.ordinal()) {
				ActorSelection user = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler/" + userName);
				if (mod.equals("v"))
					user.tell(new ChangeModeMessage(channel, LocalUser.Modes.VOICED, 1), remote);
				else if (mod.equals("op"))
					user.tell(new ChangeModeMessage(channel, LocalUser.Modes.OPERATOR, 1), remote);
			}
		}
	}

	public void modeChanged(String channelName, LocalUser.Modes mod, int premoteFlag) {
		if(channelWindows.get(channelName) != null){
			if(premoteFlag == 0){
				if((mod == LocalUser.Modes.VOICED && _modesInChannel.get(channelName) != LocalUser.Modes.OPERATOR)
						|| (mod == LocalUser.Modes.REGULAR && _modesInChannel.get(channelName) != LocalUser.Modes.VOICED))
					return;
			}
			else if(premoteFlag == 1){
				if(_modesInChannel.get(channelName) == LocalUser.Modes.OWNER)
					return;
			}
			_modesInChannel.put(channelName,mod);
			ActorSelection channelActor = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/" + channelName);
			String userName;
			switch(mod){
				case VOICED:
					userName = "+" + getTitle();
					break;
				case OPERATOR:
					userName = "@"+getTitle();
					break;
				case OWNER:
					userName = "@@"+getTitle();
					break;
				default:
					userName = getTitle();
					break;
			}
			channelActor.tell(new UserNameMessage(channelName,userName), remote);
			sys.append("Your Mode in "+channelName+" changed to: "+mod+"\n",Color.orange);
		}
	}

	public void demoteUser(String channel, String mod, String userName) {
		if(channelWindows.containsKey(channel)) {
			if (_modesInChannel.get(channel) != null
					&& _modesInChannel.get(channel).ordinal() >= LocalUser.Modes.OPERATOR.ordinal()) {
				ActorSelection user = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler/" + userName);
				if (mod.equals("v"))
					user.tell(new ChangeModeMessage(channel, LocalUser.Modes.REGULAR, 0), remote);
				else if (mod.equals("op"))
					user.tell(new ChangeModeMessage(channel, LocalUser.Modes.VOICED, 0), remote);
			}
		}
	}

	public void disbandChannel(String channel) {
		if(channelWindows.containsKey(channel)){
			if (_modesInChannel.get(channel) == LocalUser.Modes.OWNER){
				ActorSelection channelActor = _context.actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/ChannelHandler/" + channel);
				channelActor.tell(Messages.DISBAND, remote);
			}
		}
	}

	public boolean valid(String name) {
		for(int i = 0; i < name.length(); i++){
			if(!validMap.containsKey(name.charAt(i)))
				return false;
		}
		return true;
	}

}

