package msg;


import scala.Serializable;

/**
 * Created by RazB on 22/05/2017.
 */
public class UserNameMessage implements Serializable {
    String _channel;
    String _name;

    public UserNameMessage(String channel, String name){
        _channel = channel;
        _name = name;
    }

    public String getChannel(){
        return _channel;
    }

    public String toString(){
        return _name;
    }
}
