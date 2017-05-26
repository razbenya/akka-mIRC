package msg;


import scala.Serializable;

/**
 * Created by RazB on 22/05/2017.
 */
public class RequestNameMessage implements Serializable {
    String _channel;

    public RequestNameMessage(String channel){
        _channel = channel;
    }

    public String toString(){
        return _channel;
    }
}
