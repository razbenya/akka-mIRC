package msg;


import scala.Serializable;

/**
 * Created by RazB on 18/05/2017.
 */
public class KickedMessage implements Serializable {
    private String _kickedBy;
    private String _channelName;

    public KickedMessage(String kickedBy, String channelName) {
        _kickedBy = kickedBy;
        _channelName = channelName;
    }

    public String getKicker(){
        return _kickedBy;
    }

    public String getChannelName() {
        return _channelName;
    }
}