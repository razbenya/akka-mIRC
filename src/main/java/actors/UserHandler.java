package actors;

import akka.actor.*;
import msg.*;


/**
 * Created by RazB on 19/05/2017.
 */
public class UserHandler extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LoginMessage.class, m -> {
                    ActorRef remote = getContext().actorOf(Props.create(RemoteUser.class,sender()),m.toString());
                    remote.tell(Messages.LOGINSUCC,sender());
                })
                .matchAny(m ->{
                    sender().tell(m,self());
                })
                .build();
    }
}
