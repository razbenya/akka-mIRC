package actors;

import akka.actor.*;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import msg.JoinMessage;
import msg.Messages;

/**
 * Created by RazB on 23/05/2017.
 */
public class ChannelHandler extends AbstractActor {
    Router chanelServer = new Router(new BroadcastRoutingLogic());
    public Receive createReceive() {
        return receiveBuilder()
                .match(JoinMessage.class, m -> {
                    ActorRef channel = getContext().actorOf(Props.create(Channel.class), m.toString());
                    channel.forward(m,getContext());
                    getContext().watch(channel);
                    chanelServer = chanelServer.addRoutee(channel);
                })
                .match(Terminated.class, m -> {
                   chanelServer = chanelServer.removeRoutee(sender());
                })
                .matchEquals(Messages.CHAN, m -> {
                   chanelServer.route(m, sender());
                })
                .build();
    }
}
