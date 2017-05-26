package msg;

import scala.Serializable;

/**
 * Created by RazB on 19/05/2017.
 */
public class KickMessage implements Serializable {
    private String _channelName;

    public KickMessage(String _channelName) {
        this._channelName = _channelName;
    }

    public String getChannel() {
        return _channelName;
    }
}
