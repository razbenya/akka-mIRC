package main;

import actors.LocalUser;

public class Client {
    public static void main(String[] args) {
        akka.Main.main(new String[] { LocalUser.class.getName() });
    }
}
