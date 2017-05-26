package msg;

import scala.Serializable;

/**
 * Created by RazB on 19/05/2017.
 */
public class BanMessage implements Serializable {
    private String _channelName;

    public BanMessage(String _channelName) {
        this._channelName = _channelName;
    }

    public String getChannel() {
        return _channelName;
    }
}
