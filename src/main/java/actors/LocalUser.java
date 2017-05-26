package actors;

import akka.actor.*;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import gui.LoginWindow;
import gui.MainWindow;
import msg.*;
import scala.concurrent.Await;
import scala.concurrent.Future;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


/**
 * Created by RazB & Neta on 18/05/2017.
 */

public class LocalUser extends AbstractActor {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    private MainWindow mw;
    private LoginWindow lw;
    private ActorRef remote;
    private String _name;
    private ActorSelection handler;
    private HashMap<String,Modes> modesInChannel;

    public enum Modes  {
        BANNED, REGULAR, VOICED, OPERATOR, OWNER;
    }

    @Override
    public Receive createReceive() {
        modesInChannel = new HashMap<>();
        return receiveBuilder()
                .matchEquals(Messages.LOGINSUCC, m-> {
                    remote = sender();
                    _name = sender().path().name();
                    lw.close();
                    SwingUtilities.invokeLater(() -> {
                        mw = new MainWindow(modesInChannel,_name,remote,getContext());
                        mw.setVisible(true);
                    });

                })
                .match(PrivateMessage.class, m -> {
                    mw.reciveWhisper(sender().path().name(),m.toString());
                })
                .match(TitleMessage.class, m -> {
                    mw.setChannelTitle(sender().path().name() , m.getTitle());
                })
                .match(LeftChannelMessage.class, m-> {
                    HandleMessage(m.toString(),"*** parts: "+sender().path().name()+"\n",Color.GREEN);
                    mw.removeUser(sender().path().name(),m.toString());
                })
                .match(JoinedChannelMessage.class, m -> {
                    HandleMessage(m.toString(), "*** joins: "+sender().path().name()+"\n",Color.MAGENTA);
                    mw.addUser(m.toString(),sender().path().name());
                })
                .matchEquals(Messages.CHAN, m->{
                    mw.addChanel(sender().path().name());
                })
                .match(ChannelMessage.class, m -> {
                    HandleMessage(m.getChannelName(), sender().path().name()+": "+m.toString(),Color.BLACK);
                })
                .match(UserNameMessage.class, m -> {
                    mw.removeUser(sender().path().name(),m.getChannel());
                    mw.addUser(m.getChannel(),m.toString());
                })
                .match(RequestNameMessage.class, m -> {
                        String userName;
                        switch(modesInChannel.get(m.toString())){
                            case VOICED:
                                userName = "+" + _name;
                                break;
                            case OPERATOR:
                                userName = "@"+_name;
                                break;
                            case OWNER:
                                userName = "@@"+_name;
                                break;
                            default:
                                userName = _name;
                                break;
                        }
                    sender().tell(new UserNameMessage(m.toString(),userName), remote);
                })
                .matchEquals(Messages.BECOMEOWNER, m -> {
                    modesInChannel.put(sender().path().name(),Modes.OWNER);
                })
                .match(KickMessage.class, m -> {
                    mw.gotKicked(m.getChannel(), sender().path().name());
                })
                .match(KickedMessage.class, m-> {
                    HandleMessage(m.getChannelName(), "*** "+ sender().path().name() +" kicked by "+m.getKicker()+"\n",Color.RED);
                    mw.removeUser(sender().path().name(), m.getChannelName());
                })
                .match(BanMessage.class, m -> {
                    mw.gotBanned(m.getChannel(), sender().path().name());
                })
                .match(BannedMessage.class, m-> {
                    HandleMessage(m.getChannelName(), "*** "+ sender().path().name() +" banned by "+m.getBanner()+"\n",Color.RED);
                    mw.removeUser(sender().path().name(), m.getChannelName());
                })
                .match(ChangeModeMessage.class, m -> {
                    mw.modeChanged(m.getChannelName(), m.getMod(), m.getPremoteFlag());
                })
                .build();
    }


    public void HandleMessage(String channel, String message,Color c){
        Timestamp  timestamp = new Timestamp(System.currentTimeMillis());
        mw.addChanelMessase(channel, "["+sdf.format(timestamp)+"] "+ message,c);
    }

    @Override
    public void preStart() {
        handler = getContext().actorSelection("akka.tcp://ServerSystem@127.0.0.1:2553/user/UserHandler");
        Timeout t = new Timeout(5, TimeUnit.SECONDS);
        AskableActorSelection asker = new AskableActorSelection(handler);
        Future<Object> fut = asker.ask(new Identify(1), t);
        ActorIdentity ident = null;
        try {
            ident = (ActorIdentity) Await.result(fut, t.duration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!ident.ref().isEmpty()) {
            lw = new LoginWindow(getContext(), self());
            lw.setVisible(true);
        } else{
            System.exit(1);
        }
    }


}
