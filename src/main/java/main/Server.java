package main;

import akka.actor.*;
import actors.ChannelHandler;
import actors.UserHandler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Server {

    public static void main(String[] args) {
        Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 2553).
                withFallback(ConfigFactory.load());
        ActorSystem system = ActorSystem.create("ServerSystem",conf);
        system.actorOf(Props.create(UserHandler.class), "UserHandler");
        system.actorOf(Props.create(ChannelHandler.class),"ChannelHandler");
    }
}
