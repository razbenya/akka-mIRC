package actors;

import akka.actor.AbstractActor;
import akka.actor.Terminated;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import msg.*;

/**
 * Created by RazB on 18/05/2017.
 */
public class Channel extends AbstractActor {

    String _name;
    String title;
    Router channel = new Router(new BroadcastRoutingLogic());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JoinMessage.class,m-> {
                    if(channel.routees().isEmpty()){
                        _name = m.toString();
                        title = "";
                        sender().tell(Messages.BECOMEOWNER,self());
                    }
                    channel.route(new JoinedChannelMessage(_name),sender());
                    channel = channel.addRoutee(sender());
                    getContext().watch(sender());
                    if(!title.equals("")){
                        sender().tell(new TitleMessage(title),self());
                    }
                    channel.route(new RequestNameMessage(_name),sender());
                })
                .match(UserNameMessage.class, m -> {
                    channel.route(m, sender());
                })
                .matchEquals(Messages.DISBAND, m -> {
                    channel.route(new KickMessage(self().path().name()),sender());
                    getContext().stop(self());
                })
                .match(TitleMessage.class, m -> {
                    this.title = m.getTitle();
                    channel.route(m, self());
                })
                .match(KickedMessage.class, m -> {
                    channel = channel.removeRoutee(sender());
                    channel.route(m, sender());
                })
                .match(BannedMessage.class, m -> {
                    channel = channel.removeRoutee(sender());
                    channel.route(m, sender());
                })
                .matchEquals(Messages.LEAVE, m -> {
                   // userModes.remove(sender().path().name());
                    channel = channel.removeRoutee(sender());
                    channel.route(new LeftChannelMessage(_name),sender());
                })
                .matchEquals(Messages.USERLIST, m -> {
                    channel.route(new RequestNameMessage(_name),sender());
                })
                .matchEquals(Messages.CHAN, m -> {
                    sender().tell(m , self());
                })
                .match(ChannelMessage.class, m -> {
                    channel.route(m,sender());
                })
                .match(Terminated.class, m -> {
                    channel = channel.removeRoutee(sender());
                }).build();
    }

}
