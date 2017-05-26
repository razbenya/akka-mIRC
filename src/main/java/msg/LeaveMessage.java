package msg;

import scala.Serializable;

/**
 * Created by RazB on 19/05/2017.
 */
public class LeaveMessage implements Serializable {
    private String _channelName;

    public LeaveMessage(String _channelName) {
        this._channelName = _channelName;
    }

    @Override
    public String toString() {
        return _channelName;
    }
}
