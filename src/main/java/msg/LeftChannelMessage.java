package msg;

import scala.Serializable;

/**
 * Created by RazB on 23/05/2017.
 */
public class LeftChannelMessage implements Serializable {
    String _channel;

    public LeftChannelMessage(String _channel) {
        this._channel = _channel;
    }

    public String toString(){
        return _channel;
    }
}
