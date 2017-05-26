package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import msg.Messages;

/**
 * Created by RazB on 20/05/2017.
 */
public class RemoteUser extends AbstractActor  {
    private ActorRef _local;
    private String _name;

    public RemoteUser(ActorRef local){
        this._local = local;
        getContext().watch(local);
        _name = self().path().name();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(Messages.LOGINSUCC, m-> {
                    sender().tell(m,self());
                })
                .match(Terminated.class, m -> {
                    getContext().stop(self());
                })
                .matchAny(m -> {
                    _local.forward(m,getContext());
                }).build();
    }
}
