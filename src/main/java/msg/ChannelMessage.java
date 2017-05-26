package msg;

import scala.Serializable;

/**
 * Created by RazB on 22/05/2017.
 */
public class ChannelMessage implements Serializable {
    private String _message;
    private String _channelName;

    public ChannelMessage(String _message, String channelName) {
        this._message = _message;
        this._channelName = channelName;
    }

    public String getChannelName(){
        return _channelName;
    }

    public String toString(){
        return _message;
    }
}
