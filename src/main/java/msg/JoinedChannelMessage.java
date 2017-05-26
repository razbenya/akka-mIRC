package msg;

import scala.Serializable;

/**
 * Created by RazB on 23/05/2017.
 */
public class JoinedChannelMessage implements Serializable {
    String _channel;

    public JoinedChannelMessage(String _channel) {
        this._channel = _channel;
    }

    public String toString(){
        return _channel;
    }
}
